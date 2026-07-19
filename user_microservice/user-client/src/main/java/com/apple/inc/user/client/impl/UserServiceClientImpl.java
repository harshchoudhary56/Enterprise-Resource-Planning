package com.apple.inc.user.client.impl;

import com.apple.inc.user.client.IUserServiceClient;
import com.apple.inc.user.client.config.factory.UserServiceWebClientFactory;
import com.apple.inc.user.util.utils.CheckSumUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementation of {@link IUserServiceClient} that delegates HTTP calls to WebClient.
 *
 * <p>This is the equivalent of SPG's {@code PaymentOrchestratorClient} which:</p>
 * <ul>
 *   <li>Holds a reference to the underlying HTTP client (Feign in SPG, WebClient here)</li>
 *   <li>Maps business methods to HTTP endpoints</li>
 *   <li>Handles error responses</li>
 *   <li>Adds common headers via the base class (SPG uses AbstractClient.populateHeaders())</li>
 * </ul>
 *
 * <h3>SPG Pattern Comparison:</h3>
 * <pre>
 *   // SPG:
 *   public class PaymentOrchestratorClient extends AbstractClient {
 *       private IPaymentOrchestratorService paymentOrchestratorService; // Feign proxy
 *
 *       public SPGResponse&lt;RefundTxnInfo&gt; initiateRefund(RefundRequest req) {
 *           return paymentOrchestratorService.initiateRefund(
 *               req, CLIENT_BASE_URI + CLIENT_REFUND, populateHeaders(clientDetails));
 *       }
 *   }
 *
 *   // Ours:
 *   public class UserServiceClientImpl {
 *       private WebClient webClient;
 *
 *       public Mono&lt;UserResponse&gt; getUserById(String userId) {
 *           return webClient.get()
 *               .uri("/api/users/{id}", userId)
 *               .retrieve()
 *               .bodyToMono(UserResponse.class);
 *       }
 *   }
 * </pre>
 *
 * <p><b>Key difference:</b> In SPG, each method manually passes the relative URL and
 * headers. In our implementation, the WebClient filters handle headers automatically,
 * and we use Spring's URI template directly.</p>
 *
 * <h3>Construction:</h3>
 * <p>Unlike SPG which instantiates directly ({@code new PaymentOrchestratorClient(details)}),
 * our impl receives the WebClient via constructor injection. The WebClient is built
 * by {@link UserServiceWebClientFactory}.</p>
 *
 * @author harsh.choudhary
 * @since 1.0.0
 * @see IUserServiceClient
 */
@Slf4j
public class UserServiceClientImpl implements IUserServiceClient {

    /**
     * The pre-configured WebClient instance.
     * Already has: base URL, connection pool, timeouts, LB, auth headers, retry, logging.
     *
     * <p>Equivalent to SPG's {@code IPaymentOrchestratorService paymentOrchestratorService}
     * (the Feign proxy that does actual HTTP calls).</p>
     */
    private final WebClient webClient;
    private final String clientKey;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the client implementation.
     *
     * <p><b>SPG equivalent:</b></p>
     * <pre>{@code
     *   public PaymentOrchestratorClient(ClientDetails clientDetails) {
     *       this.paymentOrchestratorService =
     *           new PaymentOrchestratorClientConfig().getPaymentOrchestratorService(clientDetails);
     *   }
     * }</pre>
     *
     * @param webClient the fully configured WebClient (built by UserServiceWebClientFactory)
     */
    public UserServiceClientImpl(WebClient webClient, String clientKey) {
        this.webClient = webClient;
        this.clientKey = clientKey;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * Fetches a user by ID.
     *
     * <p><b>HTTP:</b> {@code GET /api/users/{userId}}</p>
     *
     * @param userId the unique user identifier
     * @return Mono emitting the user, or error if not found / service failure
     */
    @Override
    public Mono<Object> getUserById(String userId) {
        String dataToSign = "/api/users/" + userId;
        String checksum = CheckSumUtils.generateChecksum(dataToSign, clientKey);

        return webClient.get()
                .uri("/api/users/{id}", userId)
                .header("X-Checksum", checksum)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
                .bodyToMono(Object.class)
                .timeout(Duration.ofSeconds(8))
                .doOnError(e -> log.error("[USER-CLIENT] getUserById({}) failed: {}",
                        userId, e.getMessage()));
    }

    /**
     * Creates a new user.
     *
     * <p><b>HTTP:</b> {@code POST /api/users}</p>
     *
     * @param request the user creation payload
     * @return Mono emitting the created user
     */
    @Override
    public Mono<Object> createUser(Object request) {
        String bodyJson = serializeBody(request);
        String checksum = CheckSumUtils.generateChecksum(bodyJson, clientKey);

        return webClient.post()
                .uri("/api/users")
                .header("X-Checksum", checksum)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
                .bodyToMono(Object.class)
                .timeout(Duration.ofSeconds(8))
                .doOnError(e -> log.error("[USER-CLIENT] createUser failed: {}", e.getMessage()));
    }

    @Override
    public Mono<Object> updateUser(String userId, Object request) {
        String bodyJson = serializeBody(request);
        String checksum = CheckSumUtils.generateChecksum(bodyJson, clientKey);

        return webClient.put()
                .uri("/api/users/{id}", userId)
                .header("X-Checksum", checksum)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
                .bodyToMono(Object.class)
                .timeout(Duration.ofSeconds(8))
                .doOnError(e -> log.error("[USER-CLIENT] updateUser({}) failed: {}",
                        userId, e.getMessage()));
    }

    @Override
    public Mono<Void> deleteUser(String userId) {
        String dataToSign = "/api/users/" + userId;
        String checksum = CheckSumUtils.generateChecksum(dataToSign, clientKey);

        return webClient.delete()
                .uri("/api/users/{id}", userId)
                .header("X-Checksum", checksum)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
                .bodyToMono(Void.class)
                .timeout(Duration.ofSeconds(8));
    }

    @Override
    public Mono<Object> getAllUsers(int page, int size) {
        String dataToSign = "/api/users?page=" + page + "&size=" + size;
        String checksum = CheckSumUtils.generateChecksum(dataToSign, clientKey);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/users")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .header("X-Checksum", checksum)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
                .bodyToMono(Object.class)
                .timeout(Duration.ofSeconds(10));
    }

    @Override
    public Mono<Object> getUserByEmail(String email) {
        String dataToSign = "/api/users/email/" + email;
        String checksum = CheckSumUtils.generateChecksum(dataToSign, clientKey);

        return webClient.get()
                .uri("/api/users/email/{email}", email)
                .header("X-Checksum", checksum)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
                .bodyToMono(Object.class)
                .timeout(Duration.ofSeconds(8));
    }

    private String serializeBody(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            log.error("[USER-CLIENT] Failed to serialize body for checksum: {}", e.getMessage());
            throw new RuntimeException("Checksum computation failed — cannot serialize body", e);
        }
    }

    /**
     * Handles 4xx client errors by extracting the error body and wrapping in a custom exception.
     *
     * <p><b>SPG equivalent:</b> {@code PaymentOrchestratorErrorDecoder}</p>
     *
     * @param response the error response
     * @return Mono with the exception to propagate
     */
    private Mono<? extends Throwable> handle4xxError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .map(body -> new Exception())
                .defaultIfEmpty(new Exception(
                ));
    }
}

