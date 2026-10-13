package com.wallet.gateway.security;

import com.wallet.gateway.web.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JsonAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ApiResponses apiResponses;

    public JsonAuthenticationEntryPoint(ApiResponses apiResponses) {
        this.apiResponses = apiResponses;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        String message = (ex instanceof InvalidBearerTokenException)
                ? "Invalid or expired token"
                : "Authentication required";

        exchange.getResponse().getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        return apiResponses.write(exchange, HttpStatus.UNAUTHORIZED, message);
    }
}
