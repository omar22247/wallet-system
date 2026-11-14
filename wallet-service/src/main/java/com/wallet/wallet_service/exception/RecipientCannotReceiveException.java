package com.wallet.wallet_service.exception;

import java.util.UUID;

public class RecipientCannotReceiveException extends RuntimeException {

    private final UUID recipientUserId;

    public RecipientCannotReceiveException(UUID recipientUserId ) {
        super("Recipient cannot receive funds");
        this.recipientUserId = recipientUserId;
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }


}