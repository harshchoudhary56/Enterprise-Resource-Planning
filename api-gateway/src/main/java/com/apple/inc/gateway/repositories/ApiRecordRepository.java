package com.apple.inc.gateway.repositories;

import com.apple.inc.gateway.entities.ApiRecordEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Reactive MongoDB repository for API audit records.
 */
@Repository
public interface ApiRecordRepository extends ReactiveMongoRepository<ApiRecordEntity, String> {

}

