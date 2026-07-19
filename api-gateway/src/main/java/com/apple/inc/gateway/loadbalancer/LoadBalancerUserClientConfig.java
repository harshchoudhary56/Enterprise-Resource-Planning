package com.apple.inc.gateway.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.loadbalancer.support.ServiceInstanceListSuppliers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Registers the custom {@link GatewayRoundRobinLoadBalancer} as the load balancing
 * strategy for <b>all</b> services the API Gateway routes to.
 *
 * <p>{@code @LoadBalancerClients(defaultConfiguration = ...)} applies this config
 * globally to every service child context. If you want different strategies per service,
 * use {@code @LoadBalancerClient(name = "SERVICE-NAME", configuration = ...)} instead.</p>
 *
 * <h3>How it integrates with Spring Cloud Gateway:</h3>
 * <pre>
 *   Gateway route: lb://USER-MICROSERVICE
 *       │
 *       ▼
 *   ReactorLoadBalancerClientAutoConfiguration
 *       │
 *       ▼  creates child context for "USER-MICROSERVICE"
 *   GatewayLoadBalancerConfig.gatewayRoundRobinLoadBalancer()
 *       │
 *       ▼
 *   GatewayRoundRobinLoadBalancer.choose() → picks instance
 *       │
 *       ▼
 *   Gateway forwards request to selected instance
 * </pre>
 */
@Configuration
public class LoadBalancerUserClientConfig {

    @Bean
    public ReactiveLoadBalancer<ServiceInstance> userServiceLoadBalancer(
            LoadBalancerClientFactory factory,
            Environment environment) {

        String serviceId = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new GatewayRoundRobinLoadBalancer(serviceId, factory.getLazyProvider(serviceId,
                ServiceInstanceListSupplier.class));
    }
}

