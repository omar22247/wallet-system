package com.wallet.auth_service.exception;

public class OtpRequestLimitException extends RuntimeException {
    public OtpRequestLimitException(String message) {
        super(message);
    }
}