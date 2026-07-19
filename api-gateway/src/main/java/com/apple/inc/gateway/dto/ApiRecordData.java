package com.apple.inc.gateway.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ApiRecordData {

    private String id;
    private String crid;
    private String httpMethod;
    private String requestUri;
    private String queryParams;
    private String requestHeaders;
    private String requestBody;
    private int httpStatusCode;
    private String responseBody;
    private String targetService;
    private String clientIp;
    private long latencyMs;
    private String environment;
    private Instant createdAt;
    private Instant updatedAt;
}
