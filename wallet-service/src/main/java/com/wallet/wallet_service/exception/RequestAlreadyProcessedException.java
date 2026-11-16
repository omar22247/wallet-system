package com.wallet.wallet_service.exception;

public class RequestAlreadyProcessedException extends RuntimeException {
    public RequestAlreadyProcessedException() {
        super("This request was already processed");
    }
}