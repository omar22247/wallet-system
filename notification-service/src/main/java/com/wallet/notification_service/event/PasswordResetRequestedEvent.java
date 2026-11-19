package com.wallet.notification_service.event;

import java.time.Instant;
import java.util.UUID;

public record PasswordResetRequestedEvent(
        UUID eventId,
        UUID userId,
        String email,
        String fullName,
        String resetLink,    
        Instant expiresAt,
        Instant occurredAt
) {}