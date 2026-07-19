package com.apple.inc.gateway.ratelimiting.algorithms.impl;

import com.apple.inc.gateway.constants.StrategyBeanNames;
import com.apple.inc.gateway.ratelimiting.algorithms.RateLimitStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component(value = StrategyBeanNames.TOKEN_BUCKET)
public class TokenBucket implements RateLimitStrategy {

    private final long capacity;
    private final long period;
    private final long tokensPerPeriod;

    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();

    public TokenBucket(
            @Value("${rate-limit.token-bucket.capacity:10}") long capacity,
            @Value("${rate-limit.token-bucket.period:1000}") long period,
            @Value("${rate-limit.token-bucket.tokens-per-period:10}") long tokensPerPeriod) {
        this.capacity = capacity;
        this.period = period;
        this.tokensPerPeriod = tokensPerPeriod;
    }

    private static class Bucket {
        private long lastRefilledTimestamp;
        private long tokenCount;

        public Bucket(long lastRefilledTimestamp, long tokenCount) {
            this.lastRefilledTimestamp = lastRefilledTimestamp;
            this.tokenCount = tokenCount;
        }

        public synchronized void refill(long capacity, long period, long tokensPerPeriod) {
            long elapsedTime = System.currentTimeMillis() - lastRefilledTimestamp;
            double elapsedPeriods = (double) elapsedTime / period;
            tokenCount = (long) Math.min(capacity, tokenCount + elapsedPeriods * tokensPerPeriod);
            lastRefilledTimestamp = System.currentTimeMillis();
        }

        public synchronized boolean consume() {
            return tokenCount-- > 0;
        }
    }

    @Override
    public Mono<Boolean> isAllowed(String userId) {
        return Mono.fromSupplier(() -> {
            Bucket bucket = userBuckets.computeIfAbsent(userId,
                    id -> new Bucket(System.currentTimeMillis(), capacity));
            bucket.refill(capacity, period, tokensPerPeriod);
            return bucket.consume();
        });
    }
}
