package com.wallet.wallet_service.dto.response;

import com.wallet.wallet_service.entity.EntryType;
import com.wallet.wallet_service.entity.JournalType;
import com.wallet.wallet_service.repository.projection.TransactionRow;

import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        JournalType type,
        String direction,
        String amount,
        String description,
        CounterpartyResponse counterparty,
        Instant createdAt
) {
    public static TransactionResponse of(TransactionRow row) {
        return new TransactionResponse(
                row.id(),
                row.type(),
                row.entryType() == EntryType.CREDIT ? "IN" : "OUT",
                row.amount().toPlainString(),
                row.description(),
                CounterpartyResponse.of(row.counterpartyType(), row.counterpartyUserId()),
                row.createdAt()
        );
    }
}