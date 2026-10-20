package com.wallet.wallet_service.service;

import com.wallet.wallet_service.dto.response.PageResponse;
import com.wallet.wallet_service.dto.response.TransactionResponse;
import com.wallet.wallet_service.dto.response.WalletResponse;
import com.wallet.wallet_service.entity.Wallet;
import com.wallet.wallet_service.repository.LedgerEntryRepository;
import com.wallet.wallet_service.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final String currency;

    public WalletService(WalletRepository walletRepository,
                         LedgerEntryRepository ledgerEntryRepository,
                         @Value("${wallet.currency}") String currency) {
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.currency = currency;
    }

    public WalletResponse getMyWallet(UUID userId) {
        Wallet wallet = getOrCreate(userId);
        return WalletResponse.of(wallet, ledgerEntryRepository.balanceOf(wallet.getId()));
    }

    Wallet getOrCreate(UUID userId) {
        return walletRepository.findByUserId(userId).orElseGet(() -> {
            try {
                Wallet created = walletRepository.saveAndFlush(Wallet.forUser(userId, currency));
                log.info("Created wallet {} for user {}", created.getId(), userId);
                return created;
            } catch (DataIntegrityViolationException race) {
                log.debug("Wallet for user {} was created concurrently", userId);
                return walletRepository.findByUserId(userId).orElseThrow(() -> race);
            }
        });
    }

    public PageResponse<TransactionResponse> getMyTransactions(UUID userId, int page, int size) {
        Wallet wallet = getOrCreate(userId);
        return PageResponse.of(
                ledgerEntryRepository.findHistory(wallet.getId(), PageRequest.of(page, size)),
                TransactionResponse::of
        );
    }
}