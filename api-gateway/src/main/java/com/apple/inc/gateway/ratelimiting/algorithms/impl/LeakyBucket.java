package com.apple.inc.gateway.ratelimiting.algorithms.impl;

import com.apple.inc.gateway.constants.StrategyBeanNames;
import com.apple.inc.gateway.ratelimiting.algorithms.RateLimitStrategy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component(value = StrategyBeanNames.LEAKY_BUCKET)
public class LeakyBucket implements RateLimitStrategy {

    @Override
    public Mono<Boolean> isAllowed(String userId) {
        // TODO: Implement leaky bucket algorithm
        return Mono.just(true);
    }
}
