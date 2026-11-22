package com.wallet.auth_service.service;

import com.wallet.auth_service.config.OtpProperties;
import com.wallet.auth_service.dto.request.RegisterCompleteRequest;
import com.wallet.auth_service.dto.response.UserResponse;
import com.wallet.auth_service.entity.User;
import com.wallet.auth_service.event.EventPublisher;
import com.wallet.auth_service.event.RegistrationOtpRequestedEvent;
import com.wallet.auth_service.exception.EmailAlreadyUsedException;
import com.wallet.auth_service.exception.EventPublishException;
import com.wallet.auth_service.exception.InvalidOtpException;
import com.wallet.auth_service.repository.UserRepository;
import com.wallet.auth_service.security.OtpStore;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private final UserRepository userRepository;
    private final OtpStore otpStore;
    private final EventPublisher eventPublisher;
    private final OtpProperties otpProperties;
    private final PasswordEncoder passwordEncoder;
    public void start(String rawEmail) {
        String email = normalize(rawEmail);

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException();
        }

        String code = otpStore.issue(email);

        Instant now = Instant.now();
        RegistrationOtpRequestedEvent event = new RegistrationOtpRequestedEvent(
                UUID.randomUUID(), email, code, now.plus(otpProperties.ttl()), now);

        try {
            eventPublisher.publish(RegistrationOtpRequestedEvent.ROUTING_KEY, event.eventId(), event);
        } catch (EventPublishException e) {
            otpStore.rollbackIssue(email);
            throw e;
        }

        log.info("Registration OTP issued event={}", event.eventId());
    }

    @Transactional
    public UserResponse complete(RegisterCompleteRequest request) {
        String email = normalize(request.email());

        if (!otpStore.verify(email, request.otp())) {
            throw new InvalidOtpException();
        }

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException();
        }

       User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());

        try {
            User saved = userRepository.saveAndFlush(user);
            log.info("User registered: userId={}", saved.getId());
            return UserResponse.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyUsedException();
        }

    }

    static String normalize(String email) {
        return email.trim().toLowerCase();
    }
}