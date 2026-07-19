package com.apple.inc.log.standardization.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method for automatic API request/response audit logging.
 *
 * <p>When a method annotated with {@code @ApiRecord} is invoked, the
 * {@link com.apple.inc.log.standardization.aspects.ApiRecordAspect} intercepts it and:</p>
 * <ol>
 *   <li>Captures request metadata (method signature, arguments, correlation ID)</li>
 *   <li>Persists the request record via {@link com.apple.inc.log.standardization.service.ApiRecordPersistenceService}</li>
 *   <li>Lets the method execute and captures the response</li>
 *   <li>Updates the record with response data + HTTP status + latency</li>
 * </ol>
 *
 * <h3>Consumer Responsibility:</h3>
 * <p>Each consuming service must provide a Spring bean implementing
 * {@link com.apple.inc.log.standardization.service.ApiRecordPersistenceService}.
 * This library does NOT include any database dependency — the consumer decides
 * where and how to persist (MongoDB, Elasticsearch, file, etc.).</p>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 *   @ApiRecord
 *   @PostMapping("/register")
 *   public Mono<ResponseEntity<UserDto>> register(@RequestBody RegisterRequest req) {
 *       ...
 *   }
 * }</pre>
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiRecord {

    /** Whether to persist the request body. Default: true */
    boolean persistRequest() default true;

    /** Whether to persist the response body. Default: true */
    boolean persistResponse() default true;
}

