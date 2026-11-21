package com.wallet.auth_service.event;

import com.wallet.auth_service.config.RabbitConfig;
import com.wallet.auth_service.exception.EventPublishException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class RegistrationOtpPublisher {

    private static final long CONFIRM_TIMEOUT_SECONDS = 3;

    private final RabbitTemplate rabbitTemplate;

    public void publish(RegistrationOtpRequestedEvent event) {
        CorrelationData correlation = new CorrelationData(event.eventId().toString());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EVENTS_EXCHANGE,
                    RegistrationOtpRequestedEvent.ROUTING_KEY,
                    event,
                    correlation);

            CorrelationData.Confirm confirm =
                    correlation.getFuture().get(CONFIRM_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!confirm.ack()) {
                throw new EventPublishException("RabbitMQ rejected event " + event.eventId());
            }
            if (correlation.getReturned() != null) {
                throw new EventPublishException("No queue bound for " + RegistrationOtpRequestedEvent.ROUTING_KEY);
            }
        } catch (AmqpException | ExecutionException | TimeoutException e) {
            throw new EventPublishException("Could not publish event " + event.eventId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventPublishException("Interrupted while publishing event " + event.eventId());
        }
    }
}