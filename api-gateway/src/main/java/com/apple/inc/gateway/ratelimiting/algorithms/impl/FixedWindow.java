package com.apple.inc.gateway.ratelimiting.algorithms.impl;

import com.apple.inc.gateway.constants.StrategyBeanNames;
import com.apple.inc.gateway.ratelimiting.algorithms.RateLimitStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component(value = StrategyBeanNames.FIXED_WINDOW)
public class FixedWindow implements RateLimitStrategy {

    private final long maxRequests;
    private final long windowSizeInMillis;
    private final Clock clock;
    private final Map<String, Window> userFixedWindow = new ConcurrentHashMap<>();

    private record Window(long startTime, long requestCount) {}

    public FixedWindow(
            @Value("${rate-limit.fixed-window.max-requests:10}") long maxRequests,
            @Value("${rate-limit.fixed-window.window-size-ms:60000}") long windowSizeInMillis) {
        this.maxRequests = maxRequests;
        this.windowSizeInMillis = windowSizeInMillis;
        this.clock = Clock.systemUTC();
    }

    @Override
    public Mono<Boolean> isAllowed(String userId) {
        return Mono.fromSupplier(() -> handleRequest(userId));
    }

    private synchronized boolean handleRequest(String userId) {
        long currentTime = clock.millis();
        Window window = userFixedWindow.get(userId);

        if (window == null || currentTime - window.startTime >= windowSizeInMillis) {
            userFixedWindow.put(userId, new Window(currentTime, 1));
            return true;
        } else if (window.requestCount < maxRequests) {
            userFixedWindow.put(userId, new Window(window.startTime, window.requestCount + 1));
            return true;
        }
        return false;
    }
}
