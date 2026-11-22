package com.wallet.notification_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RabbitConfig {

    public static final String EVENTS_EXCHANGE = "wallet.events";

    public static final String DLX = "notification.dlx";
    public static final String DLQ = "notification.dlq";
    private static final Duration DLQ_RETENTION = Duration.ofDays(7);

    public static final String PASSWORD_RESET_QUEUE = "notification.password_reset";
    public static final String PASSWORD_RESET_KEY = "auth.password_reset_requested";

    public static final String REGISTRATION_OTP_QUEUE = "notification.registration_otp";
    public static final String REGISTRATION_OTP_KEY = "auth.registration_otp_requested";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENTS_EXCHANGE, true, false);
    }


    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ)
                .ttl((int) DLQ_RETENTION.toMillis())
                .build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DLQ);
    }


    @Bean
    public Queue passwordResetQueue() {
        return withDeadLetter(PASSWORD_RESET_QUEUE);
    }

    @Bean
    public Binding passwordResetBinding() {
        return BindingBuilder.bind(passwordResetQueue()).to(eventsExchange()).with(PASSWORD_RESET_KEY);
    }

    @Bean
    public Queue registrationOtpQueue() {
        return withDeadLetter(REGISTRATION_OTP_QUEUE);
    }

    @Bean
    public Binding registrationOtpBinding() {
        return BindingBuilder.bind(registrationOtpQueue()).to(eventsExchange()).with(REGISTRATION_OTP_KEY);
    }


    private static Queue withDeadLetter(String name) {
        return QueueBuilder.durable(name)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DLQ)
                .build();
    }


    @Bean
    public MessageConverter messageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}