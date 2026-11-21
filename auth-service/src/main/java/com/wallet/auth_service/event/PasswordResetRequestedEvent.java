package com.wallet.auth_service.event;

import java.time.Instant;
import java.util.UUID;

public record PasswordResetRequestedEvent(
        UUID eventId, String email, String resetLink, Instant expiresAt, Instant occurredAt) {
    public static final String ROUTING_KEY = "auth.password_reset_requested";
}
