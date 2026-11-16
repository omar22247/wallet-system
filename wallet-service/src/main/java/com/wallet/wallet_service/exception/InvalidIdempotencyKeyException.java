package com.wallet.wallet_service.exception;

public class InvalidIdempotencyKeyException extends RuntimeException {
    public InvalidIdempotencyKeyException() {
        super("Idempotency-Key must be a random UUID (version 4)");
    }
}