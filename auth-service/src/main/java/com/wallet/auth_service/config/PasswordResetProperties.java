package com.wallet.auth_service.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.password-reset")
public record PasswordResetProperties(
        @NotNull Duration ttl,
        @NotNull Duration resendCooldown,
        @NotBlank String linkBaseUrl
) {}