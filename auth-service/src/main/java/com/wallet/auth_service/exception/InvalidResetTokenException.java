package com.wallet.auth_service.exception;

public class InvalidResetTokenException extends RuntimeException {
    public InvalidResetTokenException() { super("Invalid or expired reset link"); }
}