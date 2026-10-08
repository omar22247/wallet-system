package com.wallet.auth_service.security;

import com.wallet.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;


@Component
@RequiredArgsConstructor
public class LoginAttemptListener {

    private   static final int MAX_FAILED_ATTEMPTS = 5;
    private  static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserRepository userRepository;

    @EventListener
    @Transactional
    public void onBadCredentials(AuthenticationFailureBadCredentialsEvent event) {
        String email = event.getAuthentication().getName().trim().toLowerCase();

        int updated = userRepository.incrementFailedAttempts(email);
        if (updated > 0) {
            userRepository.lockIfThresholdReached(email, MAX_FAILED_ATTEMPTS, Instant.now().plus(LOCK_DURATION));
        }
    }

    @EventListener
    @Transactional
    public void onSuccess(AuthenticationSuccessEvent event) {
        if (event.getAuthentication().getPrincipal() instanceof SecurityUser user && user.hasFailedAttempts()) {
            userRepository.resetFailedAttempts(user.getUsername());
        }
    }
}
