package com.apple.inc.gateway.util.mapper;

import com.apple.inc.gateway.dto.ApiRecordData;
import com.apple.inc.gateway.entities.ApiRecordEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiRecordMapper {

    public ApiRecordEntity toEntity(ApiRecordData data) {
        return ApiRecordEntity.builder()
                .id(data.getId())
                .crid(data.getCrid())
                .httpMethod(data.getHttpMethod())
                .requestUri(data.getRequestUri())
                .queryParams(data.getQueryParams())
                .requestHeaders(data.getRequestHeaders())
                .requestBody(data.getRequestBody())
                .httpStatusCode(data.getHttpStatusCode())
                .httpStatusCode(data.getHttpStatusCode())
                .responseBody(data.getResponseBody())
                .targetService(data.getTargetService())
                .clientIp(data.getClientIp())
                .latencyMs(data.getLatencyMs())
                .environment(data.getEnvironment())
                .createdAt(data.getCreatedAt())
                .updatedAt(data.getUpdatedAt())
                .build();

    }
}
