package com.apple.inc.user.entities.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "tbl_api_record")
public class ApiRecordEntity {

    @Id
    private String id;

    /** Unique trace/correlation ID for the request */
    private String crid;

    /** HTTP method (GET, POST, PUT, DELETE, etc.) */
    private String httpMethod;

    /** Full request URI path */
    private String requestUri;

    /** Query parameters as string */
    private String queryParams;

    /** Request headers (selected, not all — avoid logging sensitive headers) */
    private String requestHeaders;

    /** Request body (if captured) */
    private String requestBody;

    /** HTTP status code of the response */
    private int httpStatusCode;

    /** Response body (if captured) */
    private String responseBody;

    /** Target service the request was routed to */
    private String targetService;

    /** Client IP address */
    private String clientIp;

    /** Time taken to process the request (in milliseconds) */
    private long latencyMs;

    /** Active environment (dev, uat, prod) */
    private String environment;

    private Instant createdAt;

    private Instant updatedAt;
}

