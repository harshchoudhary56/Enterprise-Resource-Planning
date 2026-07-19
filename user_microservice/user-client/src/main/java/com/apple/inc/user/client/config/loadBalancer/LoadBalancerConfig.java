package com.apple.inc.user.client.config.loadBalancer;

import com.apple.inc.user.client.config.factory.UserServiceWebClientFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Spring Cloud LoadBalancer configuration that registers our custom
 * {@link RoundRobinLoadBalancer} as the load balancing strategy.
 *
 * <h3>How Spring Cloud LoadBalancer Works</h3>
 * <p>When you use a load-balanced WebClient (with {@code ReactorLoadBalancerExchangeFilterFunction}),
 * Spring Cloud creates a <b>child application context per service</b>. Each child context
 * looks for a {@link ReactiveLoadBalancer} bean to decide how to pick instances.</p>
 *
 * <pre>
 *   WebClient request to http://USER-MICROSERVICE/api/users
 *       │
 *       ▼
 *   ReactorLoadBalancerExchangeFilterFunction (WebClient filter)
 *       │
 *       ▼  "I need a load balancer for USER-MICROSERVICE"
 *   LoadBalancerClientFactory → creates child context for "USER-MICROSERVICE"
 *       │
 *       ▼  scans this config class
 *   LoadBalancerConfig.roundRobinLoadBalancer() → returns RoundRobinLoadBalancer
 *       │
 *       ▼
 *   RoundRobinLoadBalancer.choose() → picks instance 192.168.1.10:8080
 *       │
 *       ▼
 *   WebClient sends request to http://192.168.1.10:8080/api/users
 * </pre>
 *
 * <h3>⚠️ Important: {@code @Configuration} Behavior</h3>
 * <p>This class is annotated with {@code @Configuration}, which means it will be
 * <b>component-scanned</b> and applied <b>globally</b> to ALL services this application
 * communicates with. If you want to apply it to a specific service only, remove
 * {@code @Configuration} and reference it via {@code @LoadBalancerClient}:</p>
 *
 * <pre>{@code
 *   // Apply ONLY to USER-MICROSERVICE:
 *   @LoadBalancerClient(name = "USER-MICROSERVICE", configuration = LoadBalancerConfig.class)
 *   public class UserServiceWebClientFactory { ... }
 * }</pre>
 *
 * @author harsh.choudhary
 * @since 1.0.0
 * @see RoundRobinLoadBalancer
 * @see UserServiceWebClientFactory
 */
@RequiredArgsConstructor
public class LoadBalancerConfig {

    /**
     * Creates and registers the {@link RoundRobinLoadBalancer} bean.
     *
     * <p>Spring Cloud LoadBalancer injects two things into this method:</p>
     * <ul>
     *   <li><b>{@code supplier}</b> — provides the list of healthy service instances
     *       fetched from Eureka's local cache. Spring auto-creates this bean based on
     *       the service ID in the child context.</li>
     *   <li><b>{@code environment}</b> — the Spring Environment for the child context.
     *       Contains the property {@code loadbalancer.client.name} which holds the
     *       service ID (e.g., "USER-MICROSERVICE") that this load balancer is being
     *       created for.</li>
     * </ul>
     *
     * <p><b>How {@code serviceId} is extracted:</b></p>
     * <pre>
     *   LoadBalancerClientFactory.PROPERTY_NAME = "loadbalancer.client.name"
     *
     *   When Spring creates a child context for "USER-MICROSERVICE", it sets:
     *     loadbalancer.client.name = USER-MICROSERVICE
     *
     *   environment.getProperty("loadbalancer.client.name") → "USER-MICROSERVICE"
     * </pre>
     *
     * @param supplier    provides the list of available instances from Eureka
     * @param environment Spring environment containing the target service ID
     * @return a {@link RoundRobinLoadBalancer} configured for the target service
     */

    @Bean
    public ReactiveLoadBalancer<ServiceInstance> alternatingLoadBalancer(
            ServiceInstanceListSupplier supplier,
            Environment environment) {

        // Extract the service ID (e.g., "USER-MICROSERVICE") from the child context.
        // Spring Cloud sets this property when creating a per-service child context.
        String serviceId = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        ReactorServiceInstanceLoadBalancer roundRobinLoadBalancer = new RoundRobinLoadBalancer(supplier, serviceId);
        ReactorServiceInstanceLoadBalancer randomLoadBalancer = new RandomLoadBalancer(serviceId, supplier);

        return new AlternatingLoadBalancer(roundRobinLoadBalancer, randomLoadBalancer);
    }
}