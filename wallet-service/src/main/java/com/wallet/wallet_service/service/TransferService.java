package com.wallet.wallet_service.service;

import com.wallet.wallet_service.dto.request.TransferRequest;
import com.wallet.wallet_service.dto.response.TransferResponse;
import com.wallet.wallet_service.entity.JournalEntry;
import com.wallet.wallet_service.entity.JournalType;
import com.wallet.wallet_service.entity.Wallet;
import com.wallet.wallet_service.exception.*;
import com.wallet.wallet_service.repository.JournalEntryRepository;
import com.wallet.wallet_service.repository.LedgerEntryRepository;
import com.wallet.wallet_service.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final WalletRepository walletRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final IdempotencyService idempotencyService;

    @Transactional
    public TransferResponse transfer(UUID senderId, UUID idempotencyKey, TransferRequest request) {

        UUID recipientId = request.toUserId();
        BigDecimal amount = request.amount();

        idempotencyService.validateKey(idempotencyKey);

        if (senderId.equals(recipientId)) {
            throw new SelfTransferException();
        }

        Wallet sender;
        Wallet recipient;
        if (senderId.compareTo(recipientId) < 0) {
            sender = getSenderWithLock(senderId);
            recipient = getRecipientWithLock(recipientId);
        } else {
            recipient = getRecipientWithLock(recipientId);
            sender = getSenderWithLock(senderId);
        }

        String requestHash = idempotencyService.hash("TRANSFER",
                recipientId, amount.setScale(2).toPlainString(), request.description());
        idempotencyService.ensureNotProcessed(senderId, idempotencyKey, requestHash);

        if (!sender.isActive()) {
            throw new WalletNotActiveException(sender.getId());
        }
        if (!recipient.isActive()) {
            throw new RecipientCannotReceiveException(recipientId);
        }

        BigDecimal balance = ledgerEntryRepository.balanceOf(sender.getId());
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        JournalEntry journal = JournalEntry.create(
                JournalType.TRANSFER,
                request.description() != null ? request.description() : "Transfer",
                senderId
        );
        journal.debit(sender, amount)
                .credit(recipient, amount);
        journal.assertBalanced();
        JournalEntry saved = journalEntryRepository.save(journal);

        idempotencyService.markProcessed(senderId, idempotencyKey, requestHash);

        BigDecimal newBalance = balance.subtract(amount);

        log.info("Transfer journal={} from user={} to user={} amount={}",
                saved.getId(), senderId, recipientId, amount.toPlainString());

        return TransferResponse.of(saved.getId(), recipientId, amount, newBalance);
    }

    private Wallet getSenderWithLock(UUID senderId) {
        return walletRepository.findByUserIdForUpdate(senderId)
                .orElseThrow(() -> new WalletNotFoundException(senderId));
    }

    private Wallet getRecipientWithLock(UUID recipientId) {
        return walletRepository.findByUserIdForUpdate(recipientId)
                .orElseThrow(() -> new RecipientCannotReceiveException(recipientId));
    }
}