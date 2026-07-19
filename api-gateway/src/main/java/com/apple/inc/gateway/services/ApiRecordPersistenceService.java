package com.apple.inc.gateway.services;

import com.apple.inc.gateway.dto.ApiRecordData;
import com.apple.inc.gateway.entities.ApiRecordEntity;
import com.apple.inc.gateway.repositories.ApiRecordRepository;
import com.apple.inc.gateway.util.mapper.ApiRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ApiRecordPersistenceService {

    private final ApiRecordRepository apiRecordRepository;

    public Mono<ApiRecordEntity> persistRequest(ApiRecordData apiRecordData) {
        return apiRecordRepository.save(ApiRecordMapper.toEntity(apiRecordData));
    }

    public Mono<ApiRecordEntity> persistResponse(ApiRecordData apiRecordData) {
        return apiRecordRepository.save(ApiRecordMapper.toEntity(apiRecordData));
    }
}
