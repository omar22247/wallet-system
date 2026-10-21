package com.wallet.wallet_service.dto.response;

import com.wallet.wallet_service.entity.WalletType;

import java.util.UUID;

public record CounterpartyResponse(String kind, UUID userId) {

    public static CounterpartyResponse of(WalletType type, UUID userId) {
        return type == WalletType.SYSTEM
                ? new CounterpartyResponse("SYSTEM", null)
                : new CounterpartyResponse("USER", userId);
    }
}