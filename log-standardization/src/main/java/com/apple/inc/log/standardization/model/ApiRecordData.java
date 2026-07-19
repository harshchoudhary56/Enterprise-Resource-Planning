package com.apple.inc.log.standardization.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Database-agnostic DTO representing an API audit record.
 *
 * <p>The library populates this object with request/response metadata.
 * The consuming service's {@link com.apple.inc.log.standardization.service.ApiRecordPersistenceService}
 * receives this and maps it to whatever entity/collection it uses.</p>
 *
 * <p>This DTO intentionally has NO database annotations (@Document, @Entity, @Id).
 * The consumer decides the storage format.</p>
 */
@Data
@Builder
public class ApiRecordData {

    /** Unique correlation/trace ID for the request */
    private String crid;

    /** Controller method signature (e.g., "UserController.register(..)") */
    private String methodSignature;

    /** Serialized request body (first argument) */
    private String requestBody;

    /** Serialized response body */
    private String responseBody;

    /** HTTP status code of the response */
    private int httpStatusCode;

    /** Time taken to process the request (in milliseconds) */
    private long latencyMs;

    /** Active environment (dev, uat, prod) */
    private String environment;

    /** Timestamp when the request was received */
    private Instant createdAt;

    /** Timestamp when the response was captured */
    private Instant updatedAt;
}

