package com.apple.inc.gateway.loadbalancer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom Round-Robin Load Balancer for the <b>API Gateway</b>.
 *
 * <h3>What does this do?</h3>
 * <p>When the gateway routes a request to {@code lb://USER-MICROSERVICE}, Spring Cloud
 * Gateway needs to resolve this to an actual instance (e.g., 192.168.1.10:8080).
 * This class is the decision-maker — it picks the next instance in round-robin order.</p>
 *
 * <h3>How is this different from user-client's RoundRobinLoadBalancer?</h3>
 * <table>
 *   <tr><th>Component</th><th>Used By</th><th>Purpose</th></tr>
 *   <tr>
 *     <td>This class (gateway)</td>
 *     <td>API Gateway</td>
 *     <td>Picks which instance to <b>proxy/forward</b> the external client request to</td>
 *   </tr>
 *   <tr>
 *     <td>user-client's RoundRobinLoadBalancer</td>
 *     <td>Other microservices (Academic, Finance, etc.)</td>
 *     <td>Picks which instance to <b>call APIs on</b> during inter-service communication</td>
 *   </tr>
 * </table>
 *
 * <h3>Thread Safety</h3>
 * <p>Uses {@link AtomicInteger} with bitwise AND to ensure the index is always
 * non-negative, even after integer overflow. This is safe for concurrent access
 * on Netty event loop threads.</p>
 *
 * <h3>Overflow handling</h3>
 * <p>{@code (counter.getAndIncrement() & Integer.MAX_VALUE)} masks the sign bit,
 * so even when counter wraps to {@code Integer.MIN_VALUE}, the result is always ≥ 0.
 * This is superior to {@code Math.abs()} which returns negative for {@code MIN_VALUE}.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class GatewayRoundRobinLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final String serviceId;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final ObjectProvider<ServiceInstanceListSupplier> supplierProvider;

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = supplierProvider.getIfAvailable();
        if (supplier == null) {
            return Mono.just(new EmptyResponse());
        }

        return supplier.get()
                .next()
                .map(this::selectInstance);

    }

    private Response<ServiceInstance> selectInstance(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            log.warn("[GATEWAY-LB] No instances available for service: {}", serviceId);
            return new EmptyResponse();
        }

        int index = (counter.getAndIncrement() & Integer.MAX_VALUE) % instances.size();
        ServiceInstance selected = instances.get(index);

        log.debug("[GATEWAY-LB] Selected {}:{} (index={}, total={}) for service: {}",
                selected.getHost(), selected.getPort(), index, instances.size(), serviceId);

        return new DefaultResponse(selected);
    }
}

