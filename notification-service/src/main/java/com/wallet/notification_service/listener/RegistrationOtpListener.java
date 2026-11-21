package com.wallet.notification_service.listener;

import com.wallet.notification_service.config.RabbitConfig;
import com.wallet.notification_service.entity.ProcessedEvent;
import com.wallet.notification_service.event.RegistrationOtpRequestedEvent;
import com.wallet.notification_service.mail.EmailSender;
import com.wallet.notification_service.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RegistrationOtpListener {

    private static final Logger log = LoggerFactory.getLogger(RegistrationOtpListener.class);

    private static final String SUBJECT = "Your Wallet verification code";

    private final ProcessedEventRepository processedEvents;
    private final EmailSender emailSender;

    @RabbitListener(queues = RabbitConfig.REGISTRATION_OTP_QUEUE)
    @Transactional
    public void onRegistrationOtpRequested(RegistrationOtpRequestedEvent event) {
        if (processedEvents.existsById(event.eventId())) {
            log.info("Duplicate registration OTP event={} ignored", event.eventId());
            return;
        }

        processedEvents.save(ProcessedEvent.of(event.eventId()));

        Instant now = Instant.now();
        if (event.expiresAt().isBefore(now)) {
            log.warn("Registration OTP event={} expired before sending; skipped", event.eventId());
            return;
        }

        emailSender.send(event.email(), SUBJECT, body(event, now));

        log.info("Registration OTP email sent event={}", event.eventId());
    }

    private static String body(RegistrationOtpRequestedEvent event, Instant now) {
        long minutesLeft = Math.max(1, Duration.between(now, event.expiresAt()).toMinutes());
        return """
                Your Wallet verification code is:

                    %s

                It expires in %d minutes and can be used once.

                Never share this code with anyone. Wallet will never ask you for it.

                If you didn't try to create a Wallet account, you can safely ignore this email.

                — Wallet
                """.formatted(event.code(), minutesLeft);
    }
}