package com.wallet.auth_service.exception;

public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException() {
        super("Invalid or expired code");
    }
}
