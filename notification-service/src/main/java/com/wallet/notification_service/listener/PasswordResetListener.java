package com.wallet.notification_service.listener;

import com.wallet.notification_service.config.RabbitConfig;
import com.wallet.notification_service.entity.ProcessedEvent;
import com.wallet.notification_service.event.PasswordResetRequestedEvent;
import com.wallet.notification_service.mail.EmailSender;
import com.wallet.notification_service.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class PasswordResetListener {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetListener.class);

    private static final String SUBJECT = "Reset your Wallet password";
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.of("Africa/Cairo"));

    private final ProcessedEventRepository processedEvents;
    private final EmailSender emailSender;

    @RabbitListener(queues = RabbitConfig.PASSWORD_RESET_QUEUE)
    @Transactional
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        if (processedEvents.existsById(event.eventId())) {
            log.info("Duplicate password reset event={} ignored", event.eventId());
            return;
        }

        processedEvents.save(ProcessedEvent.of(event.eventId()));

        if (event.expiresAt().isBefore(Instant.now())) {
            log.warn("Password reset event={} expired before sending; skipped", event.eventId());
            return;
        }

        emailSender.send(event.email(), SUBJECT, body(event));

        log.info("Password reset email sent event={} user={}", event.eventId(), event.userId());
    }

    private static String body(PasswordResetRequestedEvent event) {
        return """
                Hi %s,

                We received a request to reset your Wallet password.
                Use the link below to choose a new one:

                %s

                This link expires at %s (Cairo time) and can be used once.

                If you didn't request this, you can safely ignore this email;
                your password won't change.

                — Wallet
                """.formatted(event.fullName(), event.resetLink(), TIME.format(event.expiresAt()));
    }
}