package com.wallet.gateway.web;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class JsonErrorBodyFilter implements GlobalFilter, Ordered {

    private final ApiResponses apiResponses;

    public JsonErrorBodyFilter(ApiResponses apiResponses) {
        this.apiResponses = apiResponses;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponseDecorator response = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> setComplete() {
                HttpStatusCode status = getStatusCode();
                if (status != null && status.isError() && !isCommitted()) {
                    return apiResponses.write(this, exchange, status, messageFor(status));
                }
                return super.setComplete();
            }
        };
        return chain.filter(exchange.mutate().response(response).build());
    }

    private static String messageFor(HttpStatusCode status) {
        if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            return "Too many requests. Please try again later.";
        }
        HttpStatus resolved = HttpStatus.resolve(status.value());
        return resolved != null ? resolved.getReasonPhrase() : "Error";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
