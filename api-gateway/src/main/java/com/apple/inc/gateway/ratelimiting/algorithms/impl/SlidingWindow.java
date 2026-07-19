package com.apple.inc.gateway.ratelimiting.algorithms.impl;

import com.apple.inc.gateway.constants.StrategyBeanNames;
import com.apple.inc.gateway.ratelimiting.algorithms.RateLimitStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component(value = StrategyBeanNames.SLIDING_WINDOW)
public class SlidingWindow implements RateLimitStrategy {

    private final long maxRequests;
    private final long windowSizeInMillis;
    private final Clock clock;
    private final Map<String, Deque<Long>> map = new ConcurrentHashMap<>();

    public SlidingWindow(
            @Value("${rate-limit.sliding-window.max-requests:10}") long maxRequests,
            @Value("${rate-limit.sliding-window.window-size-ms:60000}") long windowSizeInMillis) {
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
        Deque<Long> window = map.computeIfAbsent(userId, c -> new ConcurrentLinkedDeque<>());

        while (!window.isEmpty() && currentTime - window.peekFirst() >= windowSizeInMillis) {
            window.pollFirst();
        }

        if (window.size() < maxRequests) {
            window.offerLast(currentTime);
            return true;
        }
        return false;
    }
}
