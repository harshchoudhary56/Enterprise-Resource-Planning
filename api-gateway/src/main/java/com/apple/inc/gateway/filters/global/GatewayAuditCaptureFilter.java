package com.apple.inc.gateway.filters.global;

import com.apple.inc.gateway.dto.ApiRecordData;
import com.apple.inc.gateway.services.ApiRecordPersistenceService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayAuditCaptureFilter implements GlobalFilter, Ordered {

    private final ApiRecordPersistenceService mongoPersistenceService;

    @Value("${spring.profiles.active:unknown}")
    private String environment;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String crid = UUID.randomUUID().toString();
        Instant startTime = Instant.now();
        ServerHttpRequest request = exchange.getRequest();

        // --- 1. Extract standard metadata ---
        String httpMethod = request.getMethod().name();
        String requestUri = request.getURI().getPath();
        String queryParams = request.getURI().getQuery();
        String clientIp = extractClientIp(request);
        String requestHeaders = extractSafeHeaders(request.getHeaders());

        // --- 2. Decorate Request Body ---
        StringBuilder requestBody = new StringBuilder();
        ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(request) {

            @NonNull
            @Override
            public Flux<DataBuffer> getBody() {
                return super.getBody().doOnNext(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    requestBody.append(new String(bytes, StandardCharsets.UTF_8));
                    dataBuffer.readPosition(0);
                });
            }
        };

        // --- 3. Decorate Response Body ---
        StringBuilder responseBody = new StringBuilder();
        ServerHttpResponse decoratedResponse = new ServerHttpResponseDecorator(exchange.getResponse()) {

            @NonNull
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.map(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        responseBody.append(new String(bytes, StandardCharsets.UTF_8));
                        dataBuffer.readPosition(0);
                        return dataBuffer;
                    }));
                }
                return super.writeWith(body);
            }
        };

        ServerWebExchange decoratedExchange = exchange.mutate()
                .request(decoratedRequest)
                .response(decoratedResponse)
                .build();

        return chain.filter(decoratedExchange).then(Mono.fromRunnable(() -> {

            // --- 4. Extract target service (Available only AFTER routing) ---
            String targetService = extractTargetService(decoratedExchange);
            int statusCode = decoratedResponse.getStatusCode() != null ? decoratedResponse.getStatusCode().value() : 500;
            long latencyMs = java.time.Duration.between(startTime, Instant.now()).toMillis();

            // --- 5. Build DTO and Save ---
            ApiRecordData recordData = ApiRecordData.builder()
                    .crid(crid)
                    .httpMethod(httpMethod)
                    .requestUri(requestUri)
                    .queryParams(queryParams)
                    .requestHeaders(requestHeaders)
                    .requestBody(requestBody.toString())
                    .httpStatusCode(statusCode)
                    .responseBody(responseBody.toString())
                    .targetService(targetService)
                    .clientIp(clientIp)
                    .latencyMs(latencyMs)
                    .environment(environment)
                    .createdAt(startTime)
                    .updatedAt(Instant.now())
                    .build();

            mongoPersistenceService.persistResponse(recordData)
                    .doOnError(e -> log.error("Failed to save audit record to Mongo", e))
                    .subscribe();
        }));
    }

    // --- Utility Extraction Methods ---

    private String extractClientIp(ServerHttpRequest request) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress != null && remoteAddress.getAddress() != null
                ? remoteAddress.getAddress().getHostAddress()
                : "UNKNOWN";
    }

    private String extractTargetService(ServerWebExchange exchange) {
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        return route != null ? route.getId() : "UNKNOWN";
    }

    private String extractSafeHeaders(HttpHeaders headers) {
        // Strip sensitive tokens before saving to database!
        return headers.entrySet().stream()
                .filter(entry -> !entry.getKey().equalsIgnoreCase(HttpHeaders.AUTHORIZATION))
                .map(entry -> entry.getKey() + ": " + String.join(",", entry.getValue()))
                .collect(Collectors.joining(" | "));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}