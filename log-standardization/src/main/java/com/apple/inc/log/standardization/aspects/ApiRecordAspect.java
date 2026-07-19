package com.apple.inc.log.standardization.aspects;

import com.apple.inc.log.standardization.annotations.ApiRecord;
import com.apple.inc.log.standardization.model.ApiRecordData;
import com.apple.inc.log.standardization.service.ApiRecordPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.UUID;

/**
 * Reactive aspect that intercepts methods annotated with {@link ApiRecord}
 * and delegates request/response audit logging to the consumer-provided
 * {@link ApiRecordPersistenceService}.
 *
 * <h3>Design:</h3>
 * <pre>
 *   ┌──────────────────────────────────────────────────┐
 *   │           log-standardization (this library)      │
 *   │                                                    │
 *   │  @ApiRecord ──► ApiRecordAspect                   │
 *   │                    │                               │
 *   │                    ▼                               │
 *   │  ApiRecordPersistenceService (interface)           │
 *   │  ApiRecordData (DTO)                               │
 *   └──────────────────┬─────────────────────────────────┘
 *                      │ implements
 *   ┌──────────────────▼─────────────────────────────────┐
 *   │        Consumer service (e.g., user-web)            │
 *   │                                                      │
 *   │  MongoApiRecordPersistenceService                    │
 *   │     └─► ApiRecordRepository (MongoDB)                │
 *   │     └─► ApiRecordEntity (@Document)                  │
 *   └────────────────────────────────────────────────────┘
 * </pre>
 *
 * <p>This library has ZERO database dependencies. The consumer decides
 * the storage backend (MongoDB, Elasticsearch, PostgreSQL, etc.).</p>
 */
@Slf4j
@Order(1)
@Aspect
@Component
@RequiredArgsConstructor
public class ApiRecordAspect {

    private final ApiRecordPersistenceService persistenceService;

    @Value("${spring.profiles.active:unknown}")
    private String environment;

    @SuppressWarnings("unchecked")
    @Around("@annotation(apiRecord)")
    public Object around(ProceedingJoinPoint pjp, ApiRecord apiRecord) throws Throwable {
        String crid = UUID.randomUUID().toString();
        String methodSignature = pjp.getSignature().toShortString();
        Instant startTime = Instant.now();

        // Build request data
        ApiRecordData.ApiRecordDataBuilder dataBuilder = ApiRecordData.builder()
                .crid(crid)
                .methodSignature(methodSignature)
                .environment(environment)
                .createdAt(startTime);

        // Capture request body from first argument
        if (apiRecord.persistRequest() && pjp.getArgs().length > 0) {
            Object firstArg = pjp.getArgs()[0];
            dataBuilder.requestBody(firstArg != null ? firstArg.toString() : null);
        }

        ApiRecordData requestData = dataBuilder.build();

        // Persist request (fire-and-forget, non-blocking)
        Mono<Void> persistRequestMono = persistenceService.persistRequest(requestData)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("[ApiRecord] Failed to persist request: crid={}", crid, e))
                .onErrorComplete(); // swallow errors — logging should never break the request

        Object result = pjp.proceed();

        // If controller returns Mono, chain response persistence
        if (result instanceof Mono<?> monoResult) {
            Mono<Object> castedMono = (Mono<Object>) monoResult;

            return persistRequestMono.then(
                    castedMono.doOnNext(response -> {
                        if (apiRecord.persistResponse()) {
                            Instant now = Instant.now();
                            ApiRecordData responseData = ApiRecordData.builder()
                                    .crid(crid)
                                    .methodSignature(methodSignature)
                                    .requestBody(requestData.getRequestBody())
                                    .responseBody(truncate(response.toString(), 5000))
                                    .httpStatusCode(extractHttpStatus(response))
                                    .latencyMs(java.time.Duration.between(startTime, now).toMillis())
                                    .environment(environment)
                                    .createdAt(startTime)
                                    .updatedAt(now)
                                    .build();

                            persistenceService.persistResponse(responseData)
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .doOnSuccess(v -> log.debug("[ApiRecord] Record saved: crid={}", crid))
                                    .doOnError(e -> log.error("[ApiRecord] Failed to persist response: crid={}", crid, e))
                                    .subscribe();
                        }
                    })
            );
        }

        // Non-reactive fallback
        log.warn("[ApiRecord] Used on non-reactive method: {}", methodSignature);
        return result;
    }

    private int extractHttpStatus(Object response) {
        if (response instanceof ResponseEntity<?> re) {
            return re.getStatusCode().value();
        }
        return 200;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength
                ? value.substring(0, maxLength) + "...[TRUNCATED]"
                : value;
    }
}

