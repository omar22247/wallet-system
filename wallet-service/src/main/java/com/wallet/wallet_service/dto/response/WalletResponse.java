package com.wallet.wallet_service.dto.response;

import com.wallet.wallet_service.entity.Wallet;
import com.wallet.wallet_service.entity.WalletStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        String currency,
        WalletStatus status,
        String balance,
        Instant createdAt
) {
    public static WalletResponse of(Wallet wallet, BigDecimal balance) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getCurrency(),
                wallet.getStatus(),
                balance.toPlainString(),
                wallet.getCreatedAt());
    }
}