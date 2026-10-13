package com.wallet.gateway.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.concurrent.TimeoutException;


@Component
@Order(-2)
public class GlobalErrorHandler implements WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    private final ApiResponses apiResponses;

    public GlobalErrorHandler(ApiResponses apiResponses) {
        this.apiResponses = apiResponses;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatusCode status;
        String message;

        if (ex instanceof ResponseStatusException rse) {
            status = rse.getStatusCode();
            message = status.value() == 404 ? "Resource not found" : reasonPhrase(status);
        } else if (hasCause(ex, ConnectException.class)) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Service unavailable. Please try again later.";
        } else if (hasCause(ex, TimeoutException.class)) {
            status = HttpStatus.GATEWAY_TIMEOUT;
            message = "Service did not respond in time.";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "Internal error";
        }

        if (status.is5xxServerError()) {
            log.error("Gateway error on {} {}", exchange.getRequest().getMethod(),
                    exchange.getRequest().getPath(), ex);
        }

        return apiResponses.write(exchange, status, message);
    }

    private static boolean hasCause(Throwable ex, Class<? extends Throwable> type) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (type.isInstance(t)) {
                return true;
            }
        }
        return false;
    }

    private static String reasonPhrase(HttpStatusCode code) {
        HttpStatus status = HttpStatus.resolve(code.value());
        return status != null ? status.getReasonPhrase() : "Error";
    }
}
