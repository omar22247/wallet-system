package com.wallet.wallet_service.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferResponse(
        UUID journalId,
        UUID toUserId,
        String amount,
        String newBalance
) {
    public static TransferResponse of(UUID journalId, UUID toUserId,
                                      BigDecimal amount, BigDecimal newBalance) {
        return new TransferResponse(
                journalId,
                toUserId,
                amount.toPlainString(),
                newBalance.toPlainString()
        );
    }
}