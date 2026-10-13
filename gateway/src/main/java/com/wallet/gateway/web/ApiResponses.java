package com.wallet.gateway.web;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;


@Component
public class ApiResponses {

    private final ObjectMapper objectMapper;

    public ApiResponses(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<Void> write(ServerWebExchange exchange, HttpStatusCode status, String message) {
        return write(exchange.getResponse(), exchange, status, message);
    }

    public Mono<Void> write(ServerHttpResponse response, ServerWebExchange exchange,
                            HttpStatusCode status, String message) {
        if (response.isCommitted()) {
            return Mono.empty();
        }

        ApiResponse<Void> body = ApiResponse.error(message, exchange.getRequest().getPath().value());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (RuntimeException e) {
            return Mono.error(e);
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
