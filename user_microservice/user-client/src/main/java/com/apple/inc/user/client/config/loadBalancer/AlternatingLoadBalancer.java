package com.apple.inc.user.client.config.loadBalancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class AlternatingLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final ReactorServiceInstanceLoadBalancer roundRobinBalancer;
    private final ReactorServiceInstanceLoadBalancer randomBalancer;
    private final AtomicInteger toggle = new AtomicInteger(0);

    public AlternatingLoadBalancer(ReactorServiceInstanceLoadBalancer roundRobinBalancer,
                                   ReactorServiceInstanceLoadBalancer randomBalancer) {
        this.roundRobinBalancer = roundRobinBalancer;
        this.randomBalancer = randomBalancer;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        // Increment and get the absolute value to prevent negative modulo errors
        int currentRequest = (toggle.getAndIncrement() & Integer.MAX_VALUE);

        if (currentRequest % 2 == 0) {
            log.debug("Routing request #{} via RoundRobinLoadBalancer", currentRequest);
            return roundRobinBalancer.choose(request);
        } else {
            log.debug("Routing request #{} via CustomLoadBalancer", currentRequest);
            return randomBalancer.choose(request);
        }
    }
}