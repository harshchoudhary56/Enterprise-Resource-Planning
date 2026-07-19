package com.apple.inc.log.standardization.aspects;

import com.apple.inc.log.standardization.annotations.CompleteLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive-compatible AOP Aspect for complete request-response logging.
 *
 * <h3>Why is this different from Servlet-based logging?</h3>
 * <p>In reactive (WebFlux), controller methods return {@link Mono} or {@link Flux}.
 * The actual execution happens LATER when someone subscribes. So we can't just
 * wrap with try-catch — we must hook into the reactive pipeline.</p>
 *
 * <h3>How it works:</h3>
 * <pre>
 *   Controller returns: Mono&lt;UserResponse&gt;
 *
 *   Without aspect:  Mono → subscriber → executes → result
 *   With aspect:     Mono → doOnSubscribe(log request)
 *                         → doOnSuccess(log response)
 *                         → doOnError(log error)
 *                         → subscriber → executes → result
 * </pre>
 *
 * <h3>MDC Problem in Reactive:</h3>
 * <p>Traditional MDC is ThreadLocal-based. In reactive, a single request hops
 * across multiple threads (Netty event loop threads). So MDC values set on
 * one thread are LOST on another.</p>
 *
 * <p><b>Solution:</b> We use Reactor's {@code contextual logging} with
 * {@code Mono.deferContextual()} or simply log everything inline without
 * relying on MDC. For production, use Micrometer's context propagation.</p>
 *
 * @author harsh.choudhary
 * @since 1.0.0
 */
@Aspect
@Component
@Slf4j
public class CompleteLogAspect {

    private final ObjectMapper objectMapper;
    private final String serviceName;

    public CompleteLogAspect(ObjectMapper objectMapper,
                             @Value("${spring.application.name}") String serviceName) {
        this.objectMapper = objectMapper;
        this.serviceName = serviceName;
    }

    /**
     * Intercepts methods annotated with {@link CompleteLog}.
     *
     * <p><b>Key challenge:</b> The method returns a {@link Mono} or {@link Flux}.
     * At this point, NOTHING has executed yet — the reactive pipeline is cold.
     * We must attach logging operators to the pipeline itself.</p>
     *
     * <h4>Flow:</h4>
     * <pre>
     *   1. joinPoint.proceed() → returns Mono (cold, not yet subscribed)
     *   2. We wrap it: Mono.doOnSubscribe(log IN).doOnSuccess(log OUT).doOnError(log ERR)
     *   3. Return the wrapped Mono to the framework
     *   4. Spring WebFlux subscribes → actual execution + logging happens
     * </pre>
     */
    @Around("@annotation(com.apple.inc.common.logging.annotation.CompleteLog)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {

        String methodName = joinPoint.getSignature().toShortString();
        String correlationId = "req-" + UUID.randomUUID().toString().substring(0, 8);
        String requestBody = serializeArgs(joinPoint.getArgs());

        // Execute the method — this returns a Mono or Flux (still cold/unsubscribed)
        Object result = joinPoint.proceed();

        // ─── If return type is Mono, wrap with logging operators ───
        if (result instanceof Mono<?> mono) {
            return wrapMono(mono, methodName, correlationId, requestBody);
        }

        // ─── If return type is Flux, wrap with logging operators ───
        if (result instanceof Flux<?> flux) {
            return wrapFlux(flux, methodName, correlationId, requestBody);
        }

        // ─── Non-reactive method (shouldn't happen if everything is reactive) ───
        log.info("[COMPLETE-LOG] [SYNC] service={}, method={}, correlationId={}, request={}",
                serviceName, methodName, correlationId, requestBody);
        return result;
    }

    /**
     * Wraps a {@link Mono} with logging operators.
     *
     * <p><b>Operator chain:</b></p>
     * <ul>
     *   <li>{@code doOnSubscribe}: Logs when the pipeline starts (request received)</li>
     *   <li>{@code doOnSuccess}: Logs the successful response + time taken</li>
     *   <li>{@code doOnError}: Logs the error + time taken</li>
     * </ul>
     *
     * <p><b>Why doOnSubscribe and not doFirst?</b><br>
     * {@code doOnSubscribe} fires when subscription propagates upstream — this is
     * the moment the "request processing" truly begins in reactive terms.</p>
     */
    private <T> Mono<T> wrapMono(Mono<T> mono, String methodName,
                                 String correlationId, String requestBody) {
        long[] startTime = new long[1]; // array trick for lambda capture

        return mono
                .doOnSubscribe(subscription -> {
                    startTime[0] = System.currentTimeMillis();
                    log.info("[COMPLETE-LOG] [REQUEST-IN] service={}, method={}, " +
                                    "correlationId={}, request={}",
                            serviceName, methodName, correlationId, requestBody);
                })
                .doOnSuccess(response -> {
                    long timeTaken = System.currentTimeMillis() - startTime[0];
                    String responseBody = safeSerialize(response);
                    log.info("[COMPLETE-LOG] [RESPONSE-OUT] service={}, method={}, " +
                                    "correlationId={}, timeTakenMs={}, response={}",
                            serviceName, methodName, correlationId, timeTaken, responseBody);
                })
                .doOnError(error -> {
                    long timeTaken = System.currentTimeMillis() - startTime[0];
                    log.error("[COMPLETE-LOG] [ERROR] service={}, method={}, " +
                                    "correlationId={}, timeTakenMs={}, error={}, errorClass={}",
                            serviceName, methodName, correlationId, timeTaken,
                            error.getMessage(), error.getClass().getSimpleName());
                });
    }

    /**
     * Wraps a {@link Flux} with logging operators.
     *
     * <p>For Flux (streaming responses), we log on subscribe and on complete/error.
     * We don't log every item (could be thousands) — just the lifecycle.</p>
     */
    private <T> Flux<T> wrapFlux(Flux<T> flux, String methodName,
                                 String correlationId, String requestBody) {
        long[] startTime = new long[1];

        return flux
                .doOnSubscribe(subscription -> {
                    startTime[0] = System.currentTimeMillis();
                    log.info("[COMPLETE-LOG] [REQUEST-IN] [FLUX] service={}, method={}, " +
                                    "correlationId={}, request={}",
                            serviceName, methodName, correlationId, requestBody);
                })
                .doOnComplete(() -> {
                    long timeTaken = System.currentTimeMillis() - startTime[0];
                    log.info("[COMPLETE-LOG] [RESPONSE-COMPLETE] [FLUX] service={}, method={}, " +
                                    "correlationId={}, timeTakenMs={}",
                            serviceName, methodName, correlationId, timeTaken);
                })
                .doOnError(error -> {
                    long timeTaken = System.currentTimeMillis() - startTime[0];
                    log.error("[COMPLETE-LOG] [ERROR] [FLUX] service={}, method={}, " +
                                    "correlationId={}, timeTakenMs={}, error={}",
                            serviceName, methodName, correlationId, timeTaken, error.getMessage());
                });
    }

    /**
     * Serializes method arguments, skipping non-serializable framework objects.
     */
    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) return "{}";
        for (Object arg : args) {
            if (arg != null && !(arg instanceof org.springframework.web.server.ServerWebExchange)) {
                return safeSerialize(arg);
            }
        }
        return "{}";
    }

    private String safeSerialize(Object obj) {
        if (obj == null) return "null";
        try {
            String json = objectMapper.writeValueAsString(obj);
            return json.length() > 2000 ? json.substring(0, 2000) + "...[TRUNCATED]" : json;
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
