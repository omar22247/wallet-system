package com.wallet.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID = "X-Correlation-Id";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = UUID.randomUUID().toString();

        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r.headers(h -> {
                    h.set(CORRELATION_ID, correlationId);
                }))
                .build();

        mutated.getResponse().beforeCommit(() -> {
            mutated.getResponse()
                    .getHeaders()
                    .set(CORRELATION_ID, correlationId);

            return Mono.empty();
        });
        // it stores value of correlationId because may another thread handle it in back so
        // to we make it shared in threads
        Context ctx = Context.of("correlationId", correlationId);
        return chain.filter(mutated).contextWrite(ctx);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}