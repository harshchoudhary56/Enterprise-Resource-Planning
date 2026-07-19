package com.apple.inc.gateway.config;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayErrorConfig {

    @Bean
    public ErrorWebExceptionHandler customExceptionHandler() {
        return (ServerWebExchange exchange, Throwable ex) -> {
            if (ex instanceof RuntimeException && ex.getMessage().contains("Authorization header")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
            return Mono.error(ex);
        };
    }
}