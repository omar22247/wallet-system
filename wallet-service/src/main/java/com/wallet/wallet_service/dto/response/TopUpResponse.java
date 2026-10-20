package com.wallet.wallet_service.dto.response;

import java.util.UUID;

public record TopUpResponse(
        UUID journalId,
        UUID walletId,
        String amount,
        String newBalance
) {
}
