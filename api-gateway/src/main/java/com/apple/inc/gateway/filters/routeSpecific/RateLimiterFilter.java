package com.apple.inc.gateway.filters.routeSpecific;

import com.apple.inc.gateway.constants.StrategyType;
import com.apple.inc.gateway.ratelimiting.algorithms.RateLimitStrategy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

/**
 * Gateway Filter Factory for rate limiting.
 *
 * <p>Usage in route config (application.yml):</p>
 * <pre>
 * spring.cloud.gateway.routes:
 *   - id: USER-MICROSERVICE
 *     uri: lb://USER-MICROSERVICE
 *     predicates:
 *       - Path=/user/**
 *     filters:
 *       - name: RateLimiterFilter
 *         args:
 *           strategy: TOKEN_BUCKET
 * </pre>
 *
 * <p>This replaces the broken {@code RateLimiterAspect} which couldn't work
 * in a reactive Gateway (aspects don't intercept filter chains).</p>
 */
@Slf4j
@Component
public class RateLimiterFilter extends AbstractGatewayFilterFactory<RateLimiterFilter.Config> {

    private final Map<String, RateLimitStrategy> strategyMap;

    public RateLimiterFilter(Map<String, RateLimitStrategy> strategyMap) {
        super(Config.class);
        this.strategyMap = strategyMap;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Extract client identifier
            String clientIp = extractClientId(exchange);
            StrategyType strategyType = config.getStrategy();

            RateLimitStrategy strategy = strategyMap.get(strategyType.getValue());
            if (strategy == null) {
                log.error("Rate limit strategy not found: {}", strategyType);
                return chain.filter(exchange);
            }

            return strategy.isAllowed(clientIp)
                    .flatMap(allowed -> {
                        if (Boolean.TRUE.equals(allowed)) {
                            return chain.filter(exchange);
                        }

                        log.warn("Rate limit exceeded for client: {} on path: {}",
                                clientIp, exchange.getRequest().getURI().getPath());

                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        exchange.getResponse().getHeaders().add("X-RateLimit-Retry-After", "1");
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    private String extractClientId(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    /**
     * Configuration properties for this filter.
     * Set via route config: {@code args.strategy: TOKEN_BUCKET}
     */
    @Data
    public static class Config {
        private StrategyType strategy = StrategyType.TOKEN_BUCKET;
    }
}

