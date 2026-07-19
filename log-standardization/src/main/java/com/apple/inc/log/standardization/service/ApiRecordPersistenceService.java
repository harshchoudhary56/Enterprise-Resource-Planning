package com.apple.inc.log.standardization.service;

import com.apple.inc.log.standardization.model.ApiRecordData;
import reactor.core.publisher.Mono;

/**
 * Contract for persisting API audit records.
 *
 * <p><b>Each consuming service MUST provide a Spring bean implementing this interface.</b>
 * The {@link com.apple.inc.log.standardization.aspects.ApiRecordAspect} in this library
 * autowires this bean and delegates all persistence to it.</p>
 *
 * <h3>Why an interface?</h3>
 * <p>This library ({@code log-standardization}) is database-agnostic. It has no
 * MongoDB, JPA, or any database dependency. The consumer decides:</p>
 * <ul>
 *   <li><b>Where</b> to persist — MongoDB, Elasticsearch, PostgreSQL, file, etc.</li>
 *   <li><b>What entity</b> to use — map {@link ApiRecordData} to their own @Document/@Entity</li>
 *   <li><b>What indexes</b> to create — via their own Liquibase/Flyway migrations</li>
 * </ul>
 *
 * <h3>Example implementation in user-web:</h3>
 * <pre>{@code
 *   @Service
 *   public class MongoApiRecordPersistenceService implements ApiRecordPersistenceService {
 *
 *       private final ApiRecordRepository repository;
 *
 *       @Override
 *       public Mono<Void> persistRequest(ApiRecordData data) {
 *           ApiRecordEntity entity = mapToEntity(data);
 *           return repository.save(entity).then();
 *       }
 *
 *       @Override
 *       public Mono<Void> persistResponse(ApiRecordData data) {
 *           return repository.findByCrid(data.getCrid())
 *               .flatMap(entity -> {
 *                   entity.setResponseBody(data.getResponseBody());
 *                   entity.setHttpStatusCode(data.getHttpStatusCode());
 *                   entity.setLatencyMs(data.getLatencyMs());
 *                   entity.setUpdatedAt(data.getUpdatedAt());
 *                   return repository.save(entity);
 *               }).then();
 *       }
 *   }
 * }</pre>
 *
 * @author harsh.choudhary
 * @since 1.0.0
 */
public interface ApiRecordPersistenceService {

    /**
     * Persist the initial request record.
     * Called BEFORE the controller method executes.
     *
     * @param data the request metadata (crid, methodSignature, requestBody, environment, createdAt)
     * @return Mono<Void> that completes when persistence is done (or errors gracefully)
     */
    Mono<Void> persistRequest(ApiRecordData data);

    /**
     * Update the record with response data.
     * Called AFTER the controller method returns a response.
     *
     * @param data the complete record (request + response + httpStatusCode + latencyMs + updatedAt)
     * @return Mono<Void> that completes when persistence is done (or errors gracefully)
     */
    Mono<Void> persistResponse(ApiRecordData data);
}

