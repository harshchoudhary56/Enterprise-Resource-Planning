# Core Architecture & Communication Stack

This document outlines the foundational technology stack, communication protocols, and design patterns for the distributed ERP microservices. The architecture is designed to handle high-scale, enterprise-level traffic using non-blocking, reactive paradigms.

---

# 1. The "Holy Trinity" Stack Overview

The system relies on three core technologies to balance frontend flexibility with backend performance:

- **GraphQL** – The external front door (API Gateway)
- **gRPC** – The internal nervous system (Service-to-Service)
- **Reactive Java (Spring WebFlux + R2DBC)** – The non-blocking execution engine

---

# 2. The External Boundary: API Gateway & GraphQL

The system follows the **Backend-for-Frontend (BFF)** architecture.

> **Rule:** Web and Mobile clients **never communicate directly** with backend microservices. Every request passes through the API Gateway.

## Why GraphQL?

### Prevents Over/Under Fetching

Clients request **exactly** the fields they need, reducing unnecessary network traffic.

### Dynamic Orchestration

Instead of exposing numerous REST endpoints, the API Gateway uses a **Directed Acyclic Graph (DAG)** execution engine to dynamically compose responses from multiple backend microservices.

### Resiliency with Partial Responses

If a GraphQL query requires data from multiple services:

- Profile Service ✅
- Fees Service ✅
- Attendance Service ❌

GraphQL still returns:

- HTTP **200 OK**
- Successfully resolved data
- Localized error object for the failed resolver

This prevents a single service failure from breaking the entire response.

## Execution Strategies

### Concurrent Execution

Independent data sources execute simultaneously using reactive composition.

Example:

```java
Mono.zip(profileMono, feesMono)
```

Use this when services have **no dependencies**.

Examples:

- Profile + Fees
- Dashboard + Notifications
- Courses + Events

---

### Sequential Execution

GraphQL automatically resolves dependent relationships.

Example:

```graphql
User
 └── Timetable
```

The execution flow becomes:

```
Fetch User
      ↓
Pass User Context
      ↓
Fetch Timetable
```

The resolver automatically receives the parent object without manual orchestration.

---

# 3. The Internal Boundary: gRPC

Internal communication **never uses REST or GraphQL**.

Every microservice communicates exclusively through **gRPC**.

## Why gRPC?

### High Performance

gRPC uses:

- HTTP/2
- Binary Protocol Buffers (Protobuf)
- Connection multiplexing
- Header compression

Compared to JSON over HTTP/1.1, gRPC is typically **7–10× faster** while consuming significantly less bandwidth.

### Strict API Contracts

Every service exposes `.proto` files.

Example:

```
user-client
├── user.proto
├── generated Java classes
└── shared dependency
```

Benefits:

- Compile-time validation
- Type safety
- Automatic client generation
- Versioned contracts

---

## Security

### Mutual TLS (mTLS)

Before any data exchange:

- Client proves identity
- Server proves identity

Communication is only established after successful certificate validation.

---

### Authentication

The API Gateway forwards **asymmetric JWT tokens** inside gRPC metadata.

Flow:

```
Client
      ↓
Gateway
      ↓
JWT
      ↓
gRPC Metadata
      ↓
Microservice
```

Each service verifies the JWT locally using the IAM Service's **public key**, eliminating unnecessary authentication network calls.

---

### Payload Signatures

Critical operations (such as Payroll Approval) additionally sign payload hashes.

Purpose:

- Non-repudiation
- Tamper detection
- Integrity verification

---

# 4. Data Persistence & Caching

Reactive systems cannot use blocking persistence technologies like JDBC or Hibernate during request processing.

---

## Database Interaction (R2DBC)

Microservices use:

- Spring Data R2DBC
- Reactive PostgreSQL/MySQL drivers
- Non-blocking SQL execution

Every repository returns:

```java
Mono<T>
Flux<T>
```

instead of:

```java
Entity
List<Entity>
```

---

## Database Migrations

Schema migrations remain synchronous.

Startup sequence:

```
Application Starts
        ↓
Liquibase (JDBC)
        ↓
Schema Updated
        ↓
Reactive Server Starts
```

Because migrations occur **before** the event loop begins, blocking JDBC usage is acceptable.

---

## Distributed Caching (Redis)

Traditional JPA caches are unsuitable because reactive execution spans multiple threads.

### Why Not L1/L2 Cache?

Reactive execution:

```
Thread A
    ↓
Thread C
    ↓
Thread B
```

Thread-local caches become unreliable.

---

### Solution

Use **Reactive Redis** via:

- Lettuce
- Redisson

Benefits:

- Shared cache across all instances
- Consistent cache invalidation
- Horizontal scalability

Example:

```
Instance A
     │
     ▼
 Redis
     ▲
     │
Instance B
```

If Instance A updates a record, Instance B immediately observes the invalidated cache and fetches fresh data.

---

# 5. Observability & Distributed Tracing

Traditional stack traces lose context in reactive, distributed systems because execution spans multiple threads and network hops.

---

## Trace IDs

Every incoming request receives a unique:

```
trace_id
```

Generated by:

- Spring Cloud Sleuth
- Micrometer Tracing

The trace ID propagates through:

```
Gateway
      ↓
gRPC Metadata
      ↓
Service A
      ↓
Service B
      ↓
Service C
```

This enables end-to-end request tracking.

---

## Asynchronous Logging

The Logger Service is **never invoked synchronously**.

Instead:

```
Request
      ↓
Interceptor
      ↓
Kafka / RabbitMQ
      ↓
Logger Service
      ↓
Batch Insert
      ↓
MySQL
```

Advantages:

- No request blocking
- High throughput
- Reduced database contention
- Scalable logging pipeline

---

# 6. The 3 Golden Rules of Engagement

## 1. GraphQL Is the Front Door Only

- Frontend ↔ GraphQL Gateway ✅
- Service ↔ Service via GraphQL ❌

---

## 2. gRPC Is the Internal Standard

Every internal communication uses:

- HTTP/2
- Protocol Buffers
- Generated client stubs

No REST or GraphQL is permitted between microservices.

---

## 3. Never Block the Event Loop

Every asynchronous operation must remain reactive.

Examples:

### Database

```java
Mono<User>
Flux<Student>
```

### Cache

```java
Mono<User>
```

### Network Calls

```java
Mono<Response>
Flux<Response>
```

Blocking calls such as:

- `Thread.sleep()`
- JDBC during request handling
- `RestTemplate`
- Synchronous I/O

must **never** execute on the reactive event loop.

---

# Architecture Summary

| Layer | Technology | Purpose |
|--------|------------|---------|
| Client | Web / Mobile | User Interface |
| API Gateway | GraphQL | Backend-for-Frontend, Query Orchestration |
| Internal Communication | gRPC | High-performance Service-to-Service Communication |
| Reactive Framework | Spring WebFlux | Non-blocking Request Processing |
| Database Access | Spring Data R2DBC | Reactive Database Access |
| Database Migration | Liquibase + JDBC | Startup Schema Management |
| Cache | Reactive Redis (Lettuce / Redisson) | Distributed Caching |
| Authentication | IAM + Asymmetric JWT | Identity Verification |
| Transport Security | Mutual TLS (mTLS) | Secure Service Communication |
| Tracing | Micrometer / Spring Cloud Sleuth | Distributed Request Tracing |
| Logging | Kafka / RabbitMQ → Logger Service | Asynchronous Centralized Logging |

---

# Core Principles

- GraphQL is the **external API layer** only.
- gRPC is the **exclusive internal communication protocol**.
- Every service is fully **reactive**.
- Never block the event loop.
- Cache must be **distributed**, not thread-local.
- Every request must be traceable using a **Trace ID**.
- Logging must always be **asynchronous**.
- Strong contracts are enforced through **Protocol Buffers**.
- Security is guaranteed with **mTLS**, **JWT authentication**, and **payload signatures**.
