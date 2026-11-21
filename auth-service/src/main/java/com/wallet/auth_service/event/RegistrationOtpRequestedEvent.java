package com.wallet.auth_service.event;

import java.time.Instant;
import java.util.UUID;

public record RegistrationOtpRequestedEvent(
        UUID eventId,
        String email,
        String code,
        Instant expiresAt,
        Instant occurredAt
) {
    public static final String ROUTING_KEY = "auth.registration_otp_requested";
}