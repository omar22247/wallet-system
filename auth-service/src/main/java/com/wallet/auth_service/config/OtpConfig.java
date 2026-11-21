package com.wallet.auth_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({OtpProperties.class, PasswordResetProperties.class})
public class OtpConfig {
}