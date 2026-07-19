package com.apple.inc.user.repository.mongo;

import com.apple.inc.user.entities.mongo.ApiRecordEntity;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ApiRecordRepository extends ReactiveMongoRepository<ApiRecordEntity, String> {

    @Query("{ 'crid': ?0 }")
    Mono<ApiRecordEntity> findByRequestId(String crid);
}
