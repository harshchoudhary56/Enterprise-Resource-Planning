package com.apple.inc.gateway.filters.routeSpecific;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import reactor.core.publisher.Mono;

public class CustomRouteGatewayFilterFactory extends AbstractGatewayFilterFactory<CustomRouteGatewayFilterFactory.CustomConfig> {

    @Override
    public GatewayFilter apply(CustomRouteGatewayFilterFactory.CustomConfig config) {
        return (exchange, chain) -> {
            System.out.println("Pre filter logic here, config value: " + config.getCountry());

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                System.out.println("Post filter logic here, config value: " + config.getCountry());
            }));
        };
    }

    @Data
    public static class CustomConfig {
        private String country;    // Put the configuration properties for your filter here
    }

}
