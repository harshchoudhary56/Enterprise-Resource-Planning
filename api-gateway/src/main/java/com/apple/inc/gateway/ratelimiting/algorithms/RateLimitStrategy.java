package com.apple.inc.gateway.ratelimiting.algorithms;

import reactor.core.publisher.Mono;

/**
 * Reactive rate limit strategy interface.
 * All implementations must return a {@link Mono<Boolean>} to support
 * non-blocking evaluation (e.g., Redis-based checks in the future).
 */
public interface RateLimitStrategy {

    /**
     * Determines whether the request from the given user/IP should be allowed.
     *
     * @param userId the client identifier (IP address, user ID, API key, etc.)
     * @return Mono emitting {@code true} if allowed, {@code false} if rate-limited
     */
    Mono<Boolean> isAllowed(String userId);
}
