package com.apple.inc.user.client.config;

import com.apple.inc.user.client.IUserServiceClient;
import com.apple.inc.user.client.config.details.UserServiceClientDetails;
import com.apple.inc.user.client.config.factory.UserServiceWebClientFactory;
import com.apple.inc.user.client.impl.UserServiceClientImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Auto-configuration that wires all pieces together.
 *
 * <p>When a consuming service adds {@code user-client} as a dependency, this class
 * automatically creates the required beans. The consumer only needs to provide
 * properties in their {@code application.yml}.</p>
 *
 * <h3>How consuming service uses this:</h3>
 * <pre>
 *   // 1. Add dependency in pom.xml:
 *   &lt;dependency&gt;
 *       &lt;groupId&gt;com.apple.inc&lt;/groupId&gt;
 *       &lt;artifactId&gt;user-client&lt;/artifactId&gt;
 *   &lt;/dependency&gt;
 *
 *   // 2. Add properties in application.yml:
 *   user.service.service-name=USER-MICROSERVICE
 *   user.service.client-id=academic-service
 *   user.service.client-key=secret
 *
 *   // 3. Inject and use:
 *   @Autowired
 *   private IUserServiceClient userServiceClient;
 * </pre>
 *
 * <h3>Comparison with SPG:</h3>
 * <p>SPG does NOT auto-configure. The consumer must manually do:</p>
 * <pre>{@code
 *   ClientDetails details = ClientDetails.builder().host("...").build();
 *   IPaymentOrchestratorClient client = new PaymentOrchestratorClient(details);
 * }</pre>
 * <p>Our approach is more Spring-idiomatic — just add the dependency + properties.</p>
 *
 * @author harsh.choudhary
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class UserServiceClientAutoConfiguration {

    /**
     * Binds {@code user.service.*} properties to {@link UserServiceClientDetails}.
     *
     * <p>Maps properties like:</p>
     * <pre>
     *   user.service.service-name=USER-MICROSERVICE
     *   user.service.client-id=academic-service
     *   user.service.client-key=secret
     *   user.service.connect-timeout-ms=3000
     *   user.service.response-timeout-ms=5000
     *   user.service.max-connections=100
     * </pre>
     */
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "user.service")
    public UserServiceClientDetails userServiceClientDetails() {
        return new UserServiceClientDetails();
    }

    /**
     * Creates the WebClient using the factory.
     */
    @Bean
    @ConditionalOnMissingBean(name = "userServiceWebClient")
    public WebClient userServiceWebClient(UserServiceClientDetails details,
                                          ReactorLoadBalancerExchangeFilterFunction lbFunction) {
        log.info("Creating UserService WebClient for service: {}", details.getServiceName());
        return new UserServiceWebClientFactory().createWebClient(details, lbFunction);
    }

    /**
     * Creates the public client interface bean.
     *
     * <p>This is what consuming services inject:
     * {@code @Autowired IUserServiceClient userServiceClient;}</p>
     */
    @Bean
    @ConditionalOnMissingBean
    public IUserServiceClient userServiceClient(WebClient userServiceWebClient) {
        return new UserServiceClientImpl(userServiceWebClient, userServiceClientDetails().getClientKey());
    }
}
