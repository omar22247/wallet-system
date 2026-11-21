package com.wallet.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class RateLimitConfig {


    @Bean
    @Primary   // it will use it when there is no default resolver

    public KeyResolver clientKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(principal -> "user:" + principal.getName())
                .switchIfEmpty(Mono.fromSupplier(() -> "ip:" + clientIp(exchange.getRequest().getRemoteAddress())));
    }
    @Bean
    public KeyResolver ipKeyResolver() {
        return RateLimitConfig::ipKey;
    }


    private static Mono<String> ipKey(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
                .filter(address -> address.getAddress() != null)
                .map(address -> "ip:" + address.getAddress().getHostAddress());
    }

    private static String clientIp(InetSocketAddress address) {
        if (address == null || address.getAddress() == null) {
            return "unknown";
        }
        return address.getAddress().getHostAddress();
    }
}
