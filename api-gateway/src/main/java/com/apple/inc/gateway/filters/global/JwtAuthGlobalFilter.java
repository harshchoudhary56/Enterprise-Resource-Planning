package com.apple.inc.gateway.filters.global;

import com.apple.inc.gateway.config.routes.RouteValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private final RouteValidator routeValidator;

    // You will need to inject your JWT utility class here to actually verify the token signature
    // private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 1. Check if the route is secured using your RouteValidator
        if (routeValidator.isSecured.test(request)) {
            return chain.filter(exchange);
        }

        // 2. Check if the Authorization header exists
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            log.warn("Missing Authorization header for secured route: {}", request.getURI().getPath());
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        // 3. Extract the token from the header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Invalid Authorization header format.");
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // 4. Validate the token
        try {
            // jwtUtil.validateToken(token);

            // Optional: You can extract user claims here and mutate the request to pass them to downstream services
            // request = exchange.getRequest()
            //        .mutate()
            //        .header("X-Logged-In-User", jwtUtil.extractUsername(token))
            //        .build();
            // exchange = exchange.mutate().request(request).build();

        } catch (Exception e) {
            log.error("JWT Token validation failed: {}", e.getMessage());
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        // 5. If everything is valid (or if the route is public), proceed to the microservice
        return chain.filter(exchange);
    }

    /**
     * Helper method to terminate the request early and return an HTTP status code.
     */
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete(); // Ends the reactive chain immediately
    }

    @Override
    public int getOrder() {
        // High precedence: we want security checks to happen before any routing/load-balancing
        return -2;
    }
}
