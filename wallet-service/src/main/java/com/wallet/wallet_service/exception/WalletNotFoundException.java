package com.wallet.wallet_service.exception;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {

    private final UUID userId;

    public WalletNotFoundException(UUID userId) {
        super("Wallet not found");
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }
}