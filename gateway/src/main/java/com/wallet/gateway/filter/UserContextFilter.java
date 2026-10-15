package com.wallet.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.UUID;
import java.util.regex.Pattern;


@Component
public class UserContextFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID = "X-Correlation-Id";

    private static final Pattern SAFE_CORRELATION_ID = Pattern.compile("^[A-Za-z0-9-]{1,64}$");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = resolveCorrelationId(exchange.getRequest().getHeaders().getFirst(CORRELATION_ID));

        ServerWebExchange withCorrelation = exchange.mutate()
                .request(r -> r.headers(h -> h.set(CORRELATION_ID, correlationId)))
                .build();
        withCorrelation.getResponse().getHeaders().set(CORRELATION_ID, correlationId);

        return chain.filter(withCorrelation);
    }

    private static String resolveCorrelationId(String incoming) {
        if (incoming != null && SAFE_CORRELATION_ID.matcher(incoming).matches()) {
            return incoming;
        }
        return UUID.randomUUID().toString();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}