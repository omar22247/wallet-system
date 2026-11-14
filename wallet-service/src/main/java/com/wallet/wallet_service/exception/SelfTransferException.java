package com.wallet.wallet_service.exception;

public class SelfTransferException extends RuntimeException {
    public SelfTransferException() {
        super("Cannot transfer to your own wallet");
    }
}