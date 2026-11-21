package com.wallet.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.otp")
public record OtpProperties(
        String secret,
        Duration ttl,
        int maxAttempts,
        Duration resendCooldown,
        int maxRequestsPerHour
) {
    public OtpProperties {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("app.otp.secret must be at least 32 characters");
        }
    }
}