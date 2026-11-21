package com.wallet.auth_service.event;

import com.wallet.auth_service.config.RabbitConfig;
import com.wallet.auth_service.exception.EventPublishException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class EventPublisher {

    private static final long CONFIRM_TIMEOUT_SECONDS = 3;
    private final RabbitTemplate rabbitTemplate;

    public void publish(String routingKey, UUID eventId, Object event) {
        CorrelationData correlation = new CorrelationData(eventId.toString());
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EVENTS_EXCHANGE, routingKey, event, correlation);
            CorrelationData.Confirm confirm = correlation.getFuture().get(CONFIRM_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!confirm.ack() || correlation.getReturned() != null) {
                throw new EventPublishException("Event not routed: " + routingKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventPublishException("Interrupted while publishing " + routingKey);
        } catch (AmqpException | ExecutionException | TimeoutException e) {
            throw new EventPublishException("Failed to publish " + routingKey);
        }
    }
}