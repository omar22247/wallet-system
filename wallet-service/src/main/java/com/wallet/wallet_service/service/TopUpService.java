package com.wallet.wallet_service.service;

import com.wallet.wallet_service.bootstrap.SystemWallets;
import com.wallet.wallet_service.dto.request.TopUpRequest;
import com.wallet.wallet_service.dto.response.TopUpResponse;
import com.wallet.wallet_service.entity.JournalEntry;
import com.wallet.wallet_service.entity.JournalType;
import com.wallet.wallet_service.entity.Wallet;
import com.wallet.wallet_service.exception.WalletNotActiveException;
import com.wallet.wallet_service.exception.WalletNotFoundException;
import com.wallet.wallet_service.repository.JournalEntryRepository;
import com.wallet.wallet_service.repository.LedgerEntryRepository;
import com.wallet.wallet_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TopUpService {

    private static final Logger log = LoggerFactory.getLogger(TopUpService.class);

    private final WalletRepository walletRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final IdempotencyService idempotencyService;

    @Transactional
    public TopUpResponse topUp(UUID adminId, UUID idempotencyKey, TopUpRequest request) {

        idempotencyService.validateKey(idempotencyKey);

        Wallet target = walletRepository.findByUserIdForUpdate(request.userId())
                .orElseThrow(() -> new WalletNotFoundException(request.userId()));

        String requestHash = idempotencyService.hash("TOP_UP",
                request.userId(), request.amount().setScale(2).toPlainString(), request.description());
        idempotencyService.ensureNotProcessed(adminId, idempotencyKey, requestHash);

        if (!target.isActive()) {
            throw new WalletNotActiveException(target.getId());
        }

        Wallet funding = walletRepository.findBySystemCode(SystemWallets.FUNDING)
                .orElseThrow(() -> new IllegalStateException("FUNDING wallet is missing"));

        String description = request.description() != null ? request.description() : "Top-up";

        JournalEntry journal = JournalEntry.move(
                JournalType.TOP_UP, description, adminId,
                funding, target, request.amount());
        journalEntryRepository.save(journal);

        idempotencyService.markProcessed(adminId, idempotencyKey, requestHash);

        log.info("Top-up journal={} wallet={} amount={} by={}",
                journal.getId(), target.getId(), request.amount().toPlainString(), adminId);

        return new TopUpResponse(
                journal.getId(),
                target.getId(),
                request.amount().setScale(2).toPlainString(),
                ledgerEntryRepository.balanceOf(target.getId()).toPlainString());
    }
}