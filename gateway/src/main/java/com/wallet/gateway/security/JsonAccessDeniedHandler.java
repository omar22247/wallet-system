package com.wallet.gateway.security;

import com.wallet.gateway.web.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JsonAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final ApiResponses apiResponses;

    public JsonAccessDeniedHandler(ApiResponses apiResponses) {
        this.apiResponses = apiResponses;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        return apiResponses.write(exchange, HttpStatus.FORBIDDEN, "Access denied");
    }
}

