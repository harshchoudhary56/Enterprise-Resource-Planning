# user-client Module — Code Review Report

**Date:** May 2, 2026  
**Reviewer:** GitHub Copilot  
**Module:** `user_microservice/user-client`

---

## 📁 Module Structure

```
user-client/
├── pom.xml
└── src/main/java/com/apple/inc/user/client/
    ├── IUserServiceClient.java                          ← Public contract
    ├── impl/
    │   └── UserServiceClientImpl.java                   ← WebClient-based implementation
    └── config/
        ├── UserServiceClientAutoConfiguration.java      ← Auto-wiring bean factory
        ├── details/
        │   └── UserServiceClientDetails.java            ← Config POJO (@ConfigurationProperties)
        ├── factory/
        │   └── UserServiceWebClientFactory.java         ← Builds 3-layer WebClient
        └── loadBalancer/
            ├── LoadBalancerConfig.java                  ← Registers custom LB
            └── RoundRobinLoadBalancer.java              ← Custom round-robin implementation
```

---

## ✅ What's Done Well

| # | Area | Details |
|---|------|---------|
| 1 | **Clean Architecture** | Interface → Impl → Factory → Config separation is excellent |
| 2 | **Auto-Configuration** | `@ConditionalOnMissingBean` allows consumers to override any bean |
| 3 | **Externalized Config** | `UserServiceClientDetails` with `@ConfigurationProperties` is production-ready |
| 4 | **3-Layer HTTP Stack** | ConnectionPool → HttpClient → WebClient separation is well thought out |
| 5 | **Retry + Backoff** | Exponential backoff with jitter prevents thundering herd |
| 6 | **Checksum Security** | HMAC-based request integrity verification |
| 7 | **Comprehensive Javadoc** | Excellent documentation with SPG comparisons |

---

## 🚨 Critical Issues (Must Fix)

### Issue 1: Missing `spring.factories` / Auto-Configuration Registration

**Problem:** Your `UserServiceClientAutoConfiguration` will NOT be picked up by consuming services.

When another service (e.g., Academic-Service) adds `user-client` as a dependency, Spring Boot does NOT scan `com.apple.inc.user.client.config` because it's not in the consuming service's `@ComponentScan` base package.

**Fix:** Create the auto-configuration registration file:

```
src/main/resources/META-INF/spring/
    org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

With content:
```
com.apple.inc.user.client.config.UserServiceClientAutoConfiguration
```

This is the **Spring Boot 3.x** way (replaces the old `spring.factories`).

---

### Issue 2: `RoundRobinLoadBalancer` Should NOT Have `@Configuration`

**Problem:** `RoundRobinLoadBalancer` is annotated with `@Configuration`, but it's **not a configuration class** — it's a load balancer implementation. `@Configuration` makes Spring try to create it as a bean via component scanning, which will fail because it has constructor parameters (`supplier`, `serviceId`) that Spring cannot auto-resolve.

**Current (Wrong):**
```java
@Slf4j
@Configuration  // ❌ WRONG — this is not a config class
public class RoundRobinLoadBalancer implements ReactorServiceInstanceLoadBalancer {
```

**Fix:** Remove `@Configuration`:
```java
@Slf4j
public class RoundRobinLoadBalancer implements ReactorServiceInstanceLoadBalancer {
```

The `LoadBalancerConfig` already creates the `RoundRobinLoadBalancer` bean via the `@Bean` method — it doesn't need `@Configuration` on itself.

---

### Issue 3: `LoadBalancerConfig` Should NOT Have `@Configuration`

**Problem:** Same as the Javadoc itself warns — `@Configuration` makes it apply globally to ALL services, not just USER-MICROSERVICE. This class should be a **plain class** referenced via `@LoadBalancerClient`.

**Current (Wrong):**
```java
@Configuration  // ❌ Applied globally to ALL services
public class LoadBalancerConfig {
```

**Fix:** Remove `@Configuration` and reference it from `UserServiceClientAutoConfiguration`:
```java
public class LoadBalancerConfig {  // No annotation
```

Then in `UserServiceClientAutoConfiguration`, add:
```java
@LoadBalancerClient(name = "USER-MICROSERVICE", configuration = LoadBalancerConfig.class)
```

---

### Issue 4: Double Checksum Computation

**Problem:** Checksum is computed in **two places**:
1. `UserServiceWebClientFactory.checksumFilter()` — computes from URL + timestamp
2. `UserServiceClientImpl` (every method) — computes from URL/body + clientKey

These produce **different checksums** for the same request, and both add `X-Checksum` header. The factory filter runs AFTER the impl sets the header, so it **overwrites** the impl's checksum.

**Result:** The checksum the server receives is always the factory's URL+timestamp version, never the body-based one from the impl. The body-based checksum computation in `UserServiceClientImpl` is dead code.

**Fix:** Pick ONE location. Recommended: Keep ONLY the factory-level `checksumFilter()` and remove all checksum logic from `UserServiceClientImpl`:

```java
// UserServiceClientImpl — REMOVE all checksum code:
@Override
public Mono<Object> getUserById(String userId) {
    return webClient.get()
            .uri("/api/users/{id}", userId)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, this::handle4xxError)
            .bodyToMono(Object.class)
            .timeout(Duration.ofSeconds(8))
            .doOnError(e -> log.error("[USER-CLIENT] getUserById({}) failed: {}", userId, e.getMessage()));
}
```

---

### Issue 5: `IUserServiceClient` Uses Raw `Object` Types

**Problem:** Every method returns `Mono<Object>` and accepts `Object` parameters. This defeats the purpose of having a typed client interface. Consuming services get no type safety.

**Current:**
```java
Mono<Object> getUserById(String userId);
Mono<Object> createUser(Object request);
```

**Fix:** Use proper DTOs from `user-commons`:
```java
Mono<UserResponse> getUserById(String userId);
Mono<UserResponse> createUser(CreateUserRequest request);
Mono<UserResponse> updateUser(String userId, UpdateUserRequest request);
Mono<PageResponse<UserResponse>> getAllUsers(int page, int size);
Mono<UserResponse> getUserByEmail(String email);
```

If these DTOs don't exist in `user-commons` yet, create them there. That's exactly what `user-commons` is for.

---

## ⚠️ Important Issues (Should Fix)

### Issue 6: `handle4xxError` Returns Generic `Exception`

**Problem:**
```java
private Mono<? extends Throwable> handle4xxError(ClientResponse response) {
    return response.bodyToMono(String.class)
            .map(body -> new Exception())          // ❌ Loses the body & status code
            .defaultIfEmpty(new Exception());       // ❌ No useful info
}
```

This discards the HTTP status code AND the error body. Callers have no way to distinguish a 404 from a 400 or 409.

**Fix:** Create a proper exception class in `user-commons`:
```java
public class UserServiceClientException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;
    
    // constructor, getters...
}
```

Then:
```java
private Mono<? extends Throwable> handle4xxError(ClientResponse response) {
    return response.bodyToMono(String.class)
            .map(body -> new UserServiceClientException(
                    response.statusCode().value(), body))
            .defaultIfEmpty(new UserServiceClientException(
                    response.statusCode().value(), "No response body"));
}
```

---

### Issue 7: Duplicate `ObjectMapper` Instances

**Problem:** `ObjectMapper` is created in 3 places:
1. `UserServiceWebClientFactory.buildObjectMapper()` — for WebClient codecs
2. `UserServiceWebClientFactory.buildObjectMapper()` — called TWICE (encoder + decoder = 2 instances)
3. `UserServiceClientImpl` constructor — for checksum serialization

Each creates a separate `new ObjectMapper()`. This wastes memory and risks configuration drift.

**Fix:** Create the `ObjectMapper` once and reuse it:
```java
// In UserServiceWebClientFactory:
private ObjectMapper buildObjectMapper() {
    // Create ONCE, reuse for encoder and decoder
    ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper;
}

// In buildWebClient:
ObjectMapper mapper = buildObjectMapper();  // Create once
.codecs(configurer -> {
    configurer.defaultCodecs().jackson2JsonEncoder(
            new Jackson2JsonEncoder(mapper, MediaType.APPLICATION_JSON));
    configurer.defaultCodecs().jackson2JsonDecoder(
            new Jackson2JsonDecoder(mapper, MediaType.APPLICATION_JSON));
});
```

And remove the `ObjectMapper` from `UserServiceClientImpl` entirely (it's only used for checksum, which should be removed per Issue 4).

---

### Issue 8: `lbFunction` Parameter Injected But Not Used in `userServiceWebClient` Bean

**Problem:** In `UserServiceClientAutoConfiguration`:
```java
public WebClient userServiceWebClient(UserServiceClientDetails details,
                                      ReactorLoadBalancerExchangeFilterFunction lbFunction) {
    return new UserServiceWebClientFactory().createWebClient(details, lbFunction);
}
```

The `lbFunction` IS passed to the factory and used — this is correct. BUT the `UserServiceWebClientFactory` also creates the `lbFunction` filter inline. Let me verify...

Actually, looking at the factory:
```java
.filter(lbFunction)  // ✅ Uses the injected LB function
```

This is correct. No issue here.

---

### Issue 9: Integer Overflow in Round-Robin

**Problem:** As noted in your Javadoc, `Math.abs(Integer.MIN_VALUE)` returns `Integer.MIN_VALUE` (negative), which would cause `ArrayIndexOutOfBoundsException`.

**Fix:**
```java
int index = (counter.getAndIncrement() & Integer.MAX_VALUE) % instances.size();
```

This bitwise AND masks the sign bit, guaranteeing a non-negative result.

---

## 💡 Minor Improvements

### Issue 10: `UserServiceClientDetails.getInstance()` is Pointless

```java
public static UserServiceClientDetails getInstance() {
    return new UserServiceClientDetails();  // Same as new UserServiceClientDetails()
}
```

This doesn't add value. `@Builder` already provides `UserServiceClientDetails.builder().build()`. Remove `getInstance()`.

---

### Issue 11: Hardcoded Timeout in `UserServiceClientImpl`

Each method has `.timeout(Duration.ofSeconds(8))` hardcoded. This should come from `UserServiceClientDetails`:

```java
// Instead of: .timeout(Duration.ofSeconds(8))
// Pass timeout through constructor or use the WebClient's configured timeout
```

The WebClient already has response timeout (5s) configured in the HttpClient layer. Adding another `.timeout(8s)` on top creates confusing dual-timeout behavior.

---

## 📋 Priority Action Items

| Priority | Issue | Action |
|----------|-------|--------|
| 🔴 P0 | #1 — Missing auto-config registration | Create `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` |
| 🔴 P0 | #2 — `@Configuration` on RoundRobinLoadBalancer | Remove `@Configuration` |
| 🔴 P0 | #3 — `@Configuration` on LoadBalancerConfig | Remove `@Configuration`, use `@LoadBalancerClient` |
| 🔴 P0 | #4 — Double checksum | Remove checksum from `UserServiceClientImpl` |
| 🟡 P1 | #5 — Raw `Object` types | Use typed DTOs from `user-commons` |
| 🟡 P1 | #6 — Useless error handler | Create `UserServiceClientException` |
| 🟡 P1 | #9 — Integer overflow | Use `& Integer.MAX_VALUE` |
| 🟢 P2 | #7 — Duplicate ObjectMapper | Reuse single instance |
| 🟢 P2 | #10 — Useless `getInstance()` | Remove |
| 🟢 P2 | #11 — Hardcoded timeout | Remove `.timeout()` from impl |

---

## ✅ Overall Assessment

Your module architecture is **well-designed** — the separation of concerns (interface → impl → factory → config → details) is excellent and production-quality. The main issues are **wiring problems** (missing auto-config registration, wrong annotations) rather than design problems.

Once the P0 issues are fixed, this module will be ready for other services to consume simply by adding the Maven dependency + YAML properties.

