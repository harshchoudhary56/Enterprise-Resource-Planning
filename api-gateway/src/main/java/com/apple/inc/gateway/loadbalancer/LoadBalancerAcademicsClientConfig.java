package com.apple.inc.gateway.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.loadbalancer.support.ServiceInstanceListSuppliers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class LoadBalancerAcademicsClientConfig {

    @Bean
    public ReactiveLoadBalancer<ServiceInstance> academicsServiceLoadBalancer(
            LoadBalancerClientFactory factory,
            Environment environment) {

        String serviceId = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new GatewayRoundRobinLoadBalancer(serviceId,
                factory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class));
    }
}
