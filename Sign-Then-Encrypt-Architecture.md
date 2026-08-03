# Zero-Trust Microservices: The Sign-Then-Encrypt Pattern
**Architecture Documentation**

## Executive Summary
This document outlines the cryptographic foundation of the "Sign-then-Encrypt" (JWS + JWE) pattern for microservice-to-microservice communication. It uses a plain-English analogy and a logical breakdown to demonstrate how identity and secrecy are guaranteed across the network, removing the complex mathematical formulas for easier implementation reference.

## 1. The "Double Envelope" Analogy
To understand how an API Gateway securely communicates with a downstream microservice (like the Academic Service), we use the Double Envelope analogy:

*   **Step 1: The Glass Envelope (Signing / Proving Identity)**
    The API Gateway writes a payload and places it in a *glass envelope*, sealing it with its unique wax seal (Private Key). Anyone can read it through the glass, but the seal proves definitively that the Gateway wrote it and it has not been tampered with.
    
*   **Step 2: The Steel Safe (Encrypting / Hiding Data)**
    The Gateway locks the glass envelope inside a *steel safe* using a padlock (Public Key) that belongs exclusively to the Academic Service. Now, the payload is completely hidden during transit.
    
*   **Step 3: Receiving the Safe (Decrypting & Verifying)**
    The Academic Service uses its only-copy-in-the-world key (Private Key) to unlock the steel safe. It retrieves the glass envelope and checks the Gateway's wax seal (using the Gateway's Public Key) to verify the sender before trusting the payload.

---

## 2. The Logical Flow: Sign Then Encrypt (Sender)

When the API Gateway (Sender) wants to send a secure message to the Academic Service (Receiver):

1.  **Identity (JWS - JSON Web Signature):**
    *   The Gateway uses its **OWN Private Key** to sign the JSON payload.
    *   *Result:* The payload is tamper-proof. It proves exactly who created the message.
2.  **Secrecy (JWE - JSON Web Encryption):**
    *   To keep the payload secret from the rest of the network, the Gateway encrypts the signed token using the **Academic Service's Public Key**.
    *   *Result:* The payload is now unreadable ciphertext. Only the Academic Service has the key to unlock it.

---

## 3. The Logical Flow: Decrypt Then Verify (Receiver)

When the Academic Service receives the encrypted token over the network:

1.  **Unlocking (Decryption):**
    *   The Academic Service uses its **OWN Private Key** to decrypt the ciphertext.
    *   *Result:* The payload is unhidden, revealing the signed JSON token inside. This proves the message was not intercepted and read by unauthorized parties.
2.  **Authentication (Verification):**
    *   The Academic Service uses the **Gateway's Public Key** to verify the signature on the JSON token.
    *   *Result:* If the signature is valid, the Academic Service has cryptographic proof that the message absolutely originated from the API Gateway and was not altered in transit.