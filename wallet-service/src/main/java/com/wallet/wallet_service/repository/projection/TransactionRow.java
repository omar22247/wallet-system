package com.wallet.wallet_service.repository.projection;

import com.wallet.wallet_service.entity.EntryType;
import com.wallet.wallet_service.entity.JournalType;
import com.wallet.wallet_service.entity.WalletType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionRow(
        UUID id,
        JournalType type,
        EntryType entryType,
        BigDecimal amount,
        String description,
        WalletType counterpartyType,
        UUID counterpartyUserId,
        Instant createdAt
) {}