package com.apple.inc.user.util.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for generating HMAC-SHA256 checksums for request body integrity verification.
 *
 * <h3>What is a Checksum?</h3>
 * <p>A checksum is a cryptographic hash computed from the request body + a shared secret key.
 * The receiving service recomputes the hash using the same key and compares — if they match,
 * the request body hasn't been tampered with in transit.</p>
 *
 * <h3>Algorithm: HMAC-SHA256</h3>
 * <pre>
 *   Input:  requestBody = {"userId": "123", "name": "John"}
 *           secretKey   = "my-client-key"
 *
 *   Step 1: HMAC-SHA256(requestBody, secretKey) → raw bytes (32 bytes)
 *   Step 2: Base64 encode → "a3F5dG8xMjM0NTY3ODkw..."
 *   Step 3: Send as header: X-Checksum: a3F5dG8xMjM0NTY3ODkw...
 * </pre>
 *
 * <h3>How SPG does it:</h3>
 * <pre>
 *   SPG uses @ChecksumIncluded annotation + AOP aspect:
 *     1. AOP intercepts method call
 *     2. Serializes request body to JSON
 *     3. Computes HMAC using clientKey from properties
 *     4. Stores in ThreadLocal → AbstractClient reads it → puts in header
 *
 *   We do the same thing in a WebClient ExchangeFilterFunction — no AOP needed.
 * </pre>
 *
 * <h3>Verification on User Service side:</h3>
 * <pre>{@code
 *   // In User Service's filter/interceptor:
 *   String receivedChecksum = request.getHeader("X-Checksum");
 *   String body = readRequestBody(request);
 *   String computedChecksum = ChecksumUtils.generateChecksum(body, clientKey);
 *   if (!computedChecksum.equals(receivedChecksum)) {
 *       throw new UnauthorizedException("Checksum mismatch — request tampered");
 *   }
 * }</pre>
 *
 * @author harsh.choudhary
 * @since 1.0.0
 */
public class CheckSumUtils {

    /** HMAC algorithm used for checksum computation */
    private static final String ALGORITHM = "HmacSHA256";

    /**
     * Generates an HMAC-SHA256 checksum of the given data using the provided secret key.
     *
     * <p><b>How it works:</b></p>
     * <ol>
     *   <li>Create a SecretKeySpec from the clientKey bytes</li>
     *   <li>Initialize HMAC-SHA256 Mac instance with the key</li>
     *   <li>Feed the request body bytes into the Mac</li>
     *   <li>Get the 32-byte hash output</li>
     *   <li>Base64-encode for safe transmission as HTTP header</li>
     * </ol>
     *
     * @param data      the request body string to compute checksum for
     * @param secretKey the shared secret key (same key must be on both client and server)
     * @return Base64-encoded HMAC-SHA256 hash
     * @throws RuntimeException if the algorithm is unavailable or key is invalid
     */
    public static String generateChecksum(String data, String secretKey) {
        try {
            // Create a key specification from the raw secret key bytes
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            // Get an HMAC-SHA256 Mac instance
            Mac mac = Mac.getInstance(ALGORITHM);

            // Initialize with the secret key
            mac.init(keySpec);

            // Compute the HMAC of the request body
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Base64 encode for safe HTTP header transmission
            return Base64.getEncoder().encodeToString(hmacBytes);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to compute checksum", e);
        }
    }

    /**
     * Verifies a checksum against the expected value.
     *
     * <p>Used on the <b>receiving service side</b> to validate incoming requests.</p>
     *
     * @param data             the request body
     * @param secretKey        the shared secret
     * @param receivedChecksum the checksum received in the X-Checksum header
     * @return true if the checksum matches, false if tampered
     */
    public static boolean verifyChecksum(String data, String secretKey, String receivedChecksum) {
        String computed = generateChecksum(data, secretKey);
        return computed.equals(receivedChecksum);
    }
}

