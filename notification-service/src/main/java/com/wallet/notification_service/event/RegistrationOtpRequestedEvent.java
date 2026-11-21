package com.wallet.notification_service.event;


import java.time.Instant;
import java.util.UUID;

public record RegistrationOtpRequestedEvent(
        UUID eventId,
        String email,
        String code,
        Instant expiresAt,
        Instant occurredAt
) {}