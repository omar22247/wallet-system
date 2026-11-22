package com.wallet.wallet_service.dto.response;

import com.wallet.wallet_service.entity.WalletType;

import java.util.UUID;

/** The other side of a transaction. {@code email} is null for system counterparties and for older transactions. */
public record CounterpartyResponse(String kind, UUID userId, String email) {

    public static CounterpartyResponse of(WalletType type, UUID userId, String email) {
        return type == WalletType.SYSTEM
                ? new CounterpartyResponse("SYSTEM", null, null)
                : new CounterpartyResponse("USER", userId, email);
    }
}
