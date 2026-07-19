package com.apple.inc.user.client;

import reactor.core.publisher.Mono;

/**
 * Public interface for the User Service client.
 *
 * <p>This is the <b>contract</b> that consuming services depend on.
 * It exposes clean business methods without leaking HTTP/WebClient details.</p>
 *
 * <h3>Equivalent in SPG:</h3>
 * <pre>
 *   SPG: IPaymentOrchestratorClient
 *     - initiateRefund(RefundRequest) → SPGResponse&lt;RefundTxnInfo&gt;
 *     - checkTransactionStatus(TxnStatusRequest) → SPGResponse&lt;TxnStatusInfo&gt;
 *
 *   Ours: IUserServiceClient
 *     - getUserById(String) → Mono&lt;UserResponse&gt;
 *     - createUser(CreateUserRequest) → Mono&lt;UserResponse&gt;
 * </pre>
 *
 * <h3>Key Difference from SPG:</h3>
 * <p>SPG's interface returns synchronous objects ({@code SPGResponse<T>}).
 * Ours returns reactive {@link Mono} because we use WebClient (non-blocking).</p>
 *
 * <h3>Usage by consuming service:</h3>
 * <pre>{@code
 *   @Autowired
 *   private IUserServiceClient userServiceClient;
 *
 *   public Mono<UserResponse> getUser(String id) {
 *       return userServiceClient.getUserById(id);
 *   }
 * }</pre>
 *
 * @author harsh.choudhary
 * @since 1.0.0
 */
public interface IUserServiceClient {

    Mono<Object> getUserById(String userId);

    Mono<Object> createUser(Object request);

    Mono<Object> updateUser(String userId, Object request);

    Mono<Void> deleteUser(String userId);

    Mono<Object> getAllUsers(int page, int size);

    Mono<Object> getUserByEmail(String email);
}
