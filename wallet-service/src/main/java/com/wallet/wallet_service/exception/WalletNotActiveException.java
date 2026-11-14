package com.wallet.wallet_service.exception;

import java.util.UUID;

public class WalletNotActiveException extends RuntimeException {

    private final UUID walletId;

    public WalletNotActiveException(UUID walletId) {
        super("Wallet is not active");
        this.walletId = walletId;
    }

    public UUID getWalletId() {
        return walletId;
    }
}