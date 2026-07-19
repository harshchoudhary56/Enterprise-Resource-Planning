package com.apple.inc.user.client.config.loadBalancer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class RandomLoadBalancer implements ReactorServiceInstanceLoadBalancer {


    /**
     * Client to call the service registry (like Eureka) to get the list of available service instances.
     * private final DiscoveryClient discoveryClient;

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {

        List<ServiceInstance> instances = discoveryClient.getInstances("USER-SERVICE");
        if (instances.isEmpty()) {
            return Mono.just(new EmptyResponse());
        }
        ServiceInstance instance = instances.getFirst();

        return Mono.just(new DefaultResponse(instance));
    }
    **/

    private final String serviceId;
    private final ServiceInstanceListSupplier supplier;

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        return supplier.get()
                .next()
                .map(this::selectInstance);
    }

    private Response<ServiceInstance> selectInstance(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            log.warn("Custom: No instances available for service: {}", serviceId);
            return new EmptyResponse();
        }

        ServiceInstance selected = instances.get((int) (Math.random() * Integer.MAX_VALUE % instances.size()));
        log.debug("Custom Selected instance {}:{} for service: {}",
                selected.getHost(), selected.getPort(), serviceId);

        return new DefaultResponse(selected);
    }
}

