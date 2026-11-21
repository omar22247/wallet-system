package com.wallet.auth_service.service;

import com.wallet.auth_service.config.PasswordResetProperties;
import com.wallet.auth_service.dto.request.ResetPasswordRequest;
import com.wallet.auth_service.entity.User;
import com.wallet.auth_service.entity.UserStatus;
import com.wallet.auth_service.event.EventPublisher;
import com.wallet.auth_service.event.PasswordResetRequestedEvent;
import com.wallet.auth_service.exception.EventPublishException;
import com.wallet.auth_service.exception.InvalidResetTokenException;
import com.wallet.auth_service.repository.UserRepository;
import com.wallet.auth_service.security.PasswordResetTokenStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenStore tokenStore;
    private final EventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetProperties props;

    public void requestReset(String rawEmail) {
        String email = rawEmail.trim().toLowerCase();
        if (email.isEmpty()) return;

        if (!tokenStore.tryAcquireCooldown(email)) return;

        Optional<User> found = userRepository.findByEmailAndStatus(email, UserStatus.ACTIVE);
        if (found.isEmpty()) return;
        User user = found.get();

        String token = tokenStore.issue(user.getId());
        Instant now = Instant.now();
        var event = new PasswordResetRequestedEvent(
                UUID.randomUUID(),
                email,
                props.linkBaseUrl() + "#token=" + token,
                now.plus(props.ttl()),
                now);

        try {
            eventPublisher.publish(PasswordResetRequestedEvent.ROUTING_KEY, event.eventId(), event);
        } catch (EventPublishException e) {
            tokenStore.revoke(user.getId());
            throw e;
        }
        log.info("Password reset requested: userId={}, eventId={}", user.getId(), event.eventId());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        UUID userId = tokenStore.consume(request.token())
                .orElseThrow(InvalidResetTokenException::new);

        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(InvalidResetTokenException::new);

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        log.info("Password reset completed: userId={}", userId);
    }
}