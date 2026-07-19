package com.apple.inc.user.client.config.details;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuration POJO that holds all connection parameters for the User Service client.
 *
 * <p>This is the <b>single entry point</b> for consumers to configure the client.
 * Instead of scattering configuration across multiple properties, everything is
 * bundled here — similar to SPG's {@code ClientDetails} pattern.</p>
 *
 * <h3>Usage by consuming service:</h3>
 * <pre>{@code
 *   @Bean
 *   public UserServiceClientDetails userServiceClientDetails() {
 *       return UserServiceClientDetails.builder()
 *           .serviceName("USER-MICROSERVICE")
 *           .clientId("academic-service")
 *           .clientKey("secret-key")
 *           .connectTimeoutMs(3000)
 *           .responseTimeoutMs(5000)
 *           .build();
 *   }
 * }</pre>
 *
 * <h3>Mapping to SPG's ClientDetails:</h3>
 * <pre>
 *   SPG ClientDetails          →  UserServiceClientDetails
 *   ─────────────────────         ──────────────────────────
 *   host                       →  serviceName (logical Eureka name)
 *   clientId                   →  clientId
 *   clientKey                  →  clientKey
 *   connectTimeout             →  connectTimeoutMs
 *   readTimeout                →  responseTimeoutMs
 *   maxIdleConnections         →  maxConnections
 *   isSSLEnabled               →  sslEnabled
 *   isCustomLoggingEnabled     →  loggingEnabled
 * </pre>
 *
 * @author harsh.choudhary
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserServiceClientDetails {

    /**
     * Logical service name registered in Eureka.
     * NOT a URL — the load balancer resolves this to real instances.
     *
     * <p><b>Example:</b> {@code "USER-MICROSERVICE"}</p>
     * <p>WebClient base URL becomes: {@code http://USER-MICROSERVICE}</p>
     */
    private String serviceName;

    /**
     * Client identifier for service-to-service auth.
     * Sent as {@code X-Client-Id} header.
     */
    private String clientId;

    /**
     * Secret key for service-to-service auth.
     * Sent as {@code X-Client-Key} header.
     */
    private String clientKey;

    /**
     * TCP connection timeout in milliseconds.
     * Max time for the TCP 3-way handshake to complete.
     */
    @Builder.Default
    private int connectTimeoutMs = 3000;

    /**
     * Response timeout in milliseconds.
     * Max time from request sent → first response byte received.
     */
    @Builder.Default
    private int responseTimeoutMs = 5000;

    /**
     * Socket read timeout in seconds.
     * Max idle time between two consecutive read events on the connection.
     */
    @Builder.Default
    private int readTimeoutSeconds = 5;

    /**
     * Socket write timeout in seconds.
     * Max time to write the request body to the socket.
     */
    @Builder.Default
    private int writeTimeoutSeconds = 3;

    /**
     * Maximum number of concurrent TCP connections in the pool.
     */
    @Builder.Default
    private int maxConnections = 100;

    /**
     * Max time (ms) a request waits in queue for a free connection from the pool.
     */
    @Builder.Default
    private int pendingAcquireTimeoutMs = 3000;

    /**
     * Max requests allowed to wait in queue. Beyond this → immediate rejection.
     */
    @Builder.Default
    private int pendingAcquireMaxCount = 500;

    /**
     * Idle connections are closed after this duration (seconds).
     */
    @Builder.Default
    private int maxIdleTimeSeconds = 30;

    /**
     * Even active connections are rotated after this duration (minutes).
     */
    @Builder.Default
    private int maxLifeTimeMinutes = 5;

    /**
     * Whether to enable SSL/TLS for the connection.
     */
    @Builder.Default
    private boolean sslEnabled = false;

    /**
     * Whether to enable wire-level request/response logging.
     * ⚠️ Disable in production — logs full request/response bodies.
     */
    @Builder.Default
    private boolean loggingEnabled = true;

    /**
     * Whether to enable GZIP compression for request/response.
     */
    @Builder.Default
    private boolean compressionEnabled = true;

    /**
     * Max retry attempts on transient failures (5xx, timeouts).
     */
    @Builder.Default
    private int maxRetryAttempts = 3;

    /**
     * Initial backoff delay (ms) between retries.
     */
    @Builder.Default
    private int retryBackoffMs = 500;

    /**
     * Max response body buffer size in bytes. Default 2MB.
     */
    @Builder.Default
    private int maxInMemorySize = 2 * 1024 * 1024;

    /**
     * Convenience factory method.
     */
    public static UserServiceClientDetails getInstance() {
        return new UserServiceClientDetails();
    }
}
