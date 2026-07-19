package com.apple.inc.user.apirecord;

import com.apple.inc.log.standardization.model.ApiRecordData;
import com.apple.inc.log.standardization.service.ApiRecordPersistenceService;
import com.apple.inc.user.entities.mongo.ApiRecordEntity;
import com.apple.inc.user.repository.mongo.ApiRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * MongoDB implementation of {@link ApiRecordPersistenceService}.
 *
 * <p>This is the consumer-side implementation that the
 * {@code log-standardization} library's {@code ApiRecordAspect} delegates to.
 * It maps the library's database-agnostic {@link ApiRecordData} DTO
 * to our MongoDB {@link ApiRecordEntity} and persists it.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MongoApiRecordPersistenceService implements ApiRecordPersistenceService {

    private final ApiRecordRepository apiRecordRepository;

    @Override
    public Mono<Void> persistRequest(ApiRecordData data) {
        ApiRecordEntity entity = mapToEntity(data);
        return apiRecordRepository.save(entity)
                .doOnSuccess(saved -> log.debug("API request record persisted: crid={}", saved.getCrid()))
                .then();
    }

    @Override
    public Mono<Void> persistResponse(ApiRecordData data) {
        return apiRecordRepository.findByRequestId(data.getCrid())
                .switchIfEmpty(Mono.defer(() -> {
                    // If request wasn't saved (error/skip), create a fresh entity
                    log.warn("No existing request record found for crid={}, creating new", data.getCrid());
                    return Mono.just(mapToEntity(data));
                }))
                .flatMap(entity -> {
                    entity.setResponseBody(data.getResponseBody());
                    entity.setHttpStatusCode(data.getHttpStatusCode());
                    entity.setLatencyMs(data.getLatencyMs());
                    entity.setUpdatedAt(data.getUpdatedAt());
                    return apiRecordRepository.save(entity);
                })
                .doOnSuccess(saved -> log.debug("API response record persisted: crid={}", saved.getCrid()))
                .then();
    }

    private ApiRecordEntity mapToEntity(ApiRecordData data) {
        ApiRecordEntity entity = new ApiRecordEntity();
        entity.setCrid(data.getCrid());
        entity.setRequestUri(data.getMethodSignature());
        entity.setHttpMethod(data.getMethodSignature());
        entity.setRequestBody(data.getRequestBody());
        entity.setEnvironment(data.getEnvironment());
        entity.setCreatedAt(data.getCreatedAt());
        return entity;
    }
}

