package com.apple.inc.user.client.config.loadBalancer;

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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom <b>Round-Robin Load Balancer</b> for inter-service communication.
 *
 * <h3>What is Round-Robin?</h3>
 * <p>Round-Robin distributes requests <b>equally and sequentially</b> across all
 * available instances of a service. If there are 3 instances (A, B, C), traffic
 * flows as: A → B → C → A → B → C → ...</p>
 *
 * <h3>Where is this used?</h3>
 * <p>This load balancer is used for <b>inter-service (service-to-service)</b> calls only.
 * For example, when Academic-Service calls User-Service via the {@code user-client} module,
 * this class picks which User-Service instance to route to.</p>
 *
 * <p><b>Note:</b> The API Gateway has its own separate built-in load balancer
 * (also round-robin by default via {@code lb://SERVICE-NAME}). This class does NOT
 * affect gateway routing.</p>
 *
 * <h3>How instance list is obtained</h3>
 * <pre>
 *   Eureka Server (registry)
 *        │
 *        ▼  (fetched every 30s by default)
 *   Eureka Client (local cache inside this JVM)
 *        │
 *        ▼
 *   ServiceInstanceListSupplier ← this class reads from here
 *        │
 *        ▼
 *   RoundRobinLoadBalancer.choose() → picks one instance
 * </pre>
 *
 * <h3>Thread Safety</h3>
 * <p>Uses {@link AtomicInteger} for the counter, making it safe for concurrent
 * reactive pipelines on Netty event loop threads without any locking.</p>
 *
 * <h3>Integer Overflow Handling</h3>
 * <p>{@code Math.abs(counter.getAndIncrement() % instances.size())} ensures the
 * index is always positive. When the counter overflows {@link Integer#MAX_VALUE},
 * it wraps to {@link Integer#MIN_VALUE}. {@code Math.abs()} handles the negative
 * value, so round-robin continues without interruption.</p>
 *
 * <p><b>⚠️ Edge case:</b> {@code Math.abs(Integer.MIN_VALUE)} returns
 * {@code Integer.MIN_VALUE} (still negative). This is a 1-in-4-billion event and
 * the fallback would be an {@link ArrayIndexOutOfBoundsException}. For production,
 * consider using {@code (counter.getAndIncrement() & Integer.MAX_VALUE) % size}
 * which is always non-negative.</p>
 *
 * @author harsh.choudhary
 * @since 1.0.0
 * @see LoadBalancerConfig
 * @see ReactorServiceInstanceLoadBalancer
 */
@Slf4j
public class RoundRobinLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    /**
     * Atomically incrementing counter that tracks which instance to pick next.
     *
     * <p>Each call to {@link #choose(Request)} increments this by 1.
     * The modulo operation ({@code counter % instanceCount}) maps it to a valid index.</p>
     *
     * <p>Example with 3 instances:</p>
     * <pre>
     *   counter=0 → 0%3=0 → Instance A
     *   counter=1 → 1%3=1 → Instance B
     *   counter=2 → 2%3=2 → Instance C
     *   counter=3 → 3%3=0 → Instance A (wraps around)
     * </pre>
     */
    private final AtomicInteger counter = new AtomicInteger(0);

    /**
     * Supplies the list of currently registered service instances from Eureka's
     * local cache. This list is refreshed periodically based on
     * {@code eureka.client.registry-fetch-interval-seconds} (default: 30s).
     *
     * <p>The supplier emits a {@code Flux<List<ServiceInstance>>}. We take the
     * first emission via {@code .next()} since the supplier provides the full
     * list as a single snapshot.</p>
     */
    private final ServiceInstanceListSupplier supplier;

    /**
     * The logical service ID (e.g., "USER-MICROSERVICE") this load balancer
     * is responsible for. Used only for logging/debugging purposes.
     */
    private final String serviceId;

    /**
     * Constructs a new RoundRobinLoadBalancer.
     *
     * @param supplier  provides the live list of service instances from Eureka's local cache
     * @param serviceId the logical service name (e.g., "USER-MICROSERVICE") — used for logging
     */
    public RoundRobinLoadBalancer(ServiceInstanceListSupplier supplier, String serviceId) {
        this.supplier = supplier;
        this.serviceId = serviceId;
    }

    /**
     * Chooses the next service instance using round-robin selection.
     *
     * <p>This method is called automatically by Spring Cloud's
     * {@link org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction}
     * every time a WebClient request is made to a load-balanced URL
     * (e.g., {@code http://USER-MICROSERVICE/api/users}).</p>
     *
     * <p><b>Reactive chain:</b></p>
     * <pre>
     *   supplier.get()        → Flux&lt;List&lt;ServiceInstance&gt;&gt; (stream of instance lists)
     *       .next()           → Mono&lt;List&lt;ServiceInstance&gt;&gt; (take first snapshot)
     *       .map(this::select)→ Mono&lt;Response&lt;ServiceInstance&gt;&gt; (pick one instance)
     * </pre>
     *
     * @param request the load balancer request (contains hints, cookies — unused in round-robin)
     * @return a {@link Mono} emitting the selected instance wrapped in a {@link Response},
     *         or an {@link EmptyResponse} if no instances are registered
     */
    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        return supplier.get()
                .next()
                .map(this::selectInstance);
    }

    /**
     * Selects one instance from the list using modular arithmetic.
     *
     * <p><b>Algorithm:</b></p>
     * <ol>
     *   <li>Atomically get the current counter value and increment it by 1.</li>
     *   <li>Compute {@code index = |counter| % instanceCount}.</li>
     *   <li>Return the instance at that index.</li>
     * </ol>
     *
     * <p><b>Visual example with 3 instances:</b></p>
     * <pre>
     *   Request #1: counter=0 → index=0 → 192.168.1.10:8080
     *   Request #2: counter=1 → index=1 → 192.168.1.11:8080
     *   Request #3: counter=2 → index=2 → 192.168.1.12:8080
     *   Request #4: counter=3 → index=0 → 192.168.1.10:8080  (wraps around)
     * </pre>
     *
     * @param instances the list of available service instances from Eureka
     * @return a {@link DefaultResponse} wrapping the selected instance,
     *         or {@link EmptyResponse} if the list is empty
     */
    private Response<ServiceInstance> selectInstance(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            log.warn("No instances available for service: {}", serviceId);
            return new EmptyResponse();
        }

        // Thread-safe round-robin (bitwise AND masks sign bit — always non-negative)
        int index = (counter.getAndIncrement() & Integer.MAX_VALUE) % instances.size();
        ServiceInstance selected = instances.get(index);

        log.debug("Selected instance {}:{} (index: {}) for service: {}",
                selected.getHost(), selected.getPort(), index, serviceId);

        return new DefaultResponse(selected);
    }

}