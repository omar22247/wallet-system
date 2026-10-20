package com.wallet.wallet_service.dto.response;

import com.wallet.wallet_service.entity.EntryType;
import com.wallet.wallet_service.entity.JournalEntry;
import com.wallet.wallet_service.entity.JournalType;
import com.wallet.wallet_service.entity.LedgerEntry;

import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        JournalType type,
        String direction,
        String amount,
        String description,
        Instant createdAt
) {
    public static TransactionResponse of(LedgerEntry e) {
        JournalEntry journal = e.getJournalEntry();
        return new TransactionResponse(
                e.getId(),
                journal.getType(),
                e.getEntryType() == EntryType.CREDIT ? "IN" : "OUT",
                e.getAmount().toPlainString(),
                journal.getDescription(),
                e.getCreatedAt()
        );
    }
}