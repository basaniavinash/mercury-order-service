# Mercury Order Service

Order management service for the Mercury e-commerce platform. Handles order creation with inventory reservation, and uses the **transactional outbox pattern** to publish events to Kafka without distributed transaction risk.

---

## What it does

| Feature | Detail |
|---------|--------|
| **Create order** | Validates stock, reserves inventory, creates order + line items atomically |
| **Outbox publishing** | `OrderCreatedEvent` written to outbox table in same DB transaction |
| **Stock enforcement** | Throws `InsufficientStockException` before any order record is created |
| **Status tracking** | Orders move through `NEW → CREATED` state transitions |
| **OAuth2 auth** | All endpoints require a valid JWT with appropriate scope |

---

## Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 21, Spring Boot 4.0 |
| Persistence | PostgreSQL + Flyway, JDBC (no ORM) |
| Messaging | Apache Kafka |
| Auth | Spring Security, OAuth2 Resource Server |
| Observability | Micrometer/Prometheus, Logstash JSON logging |

---

## Outbox pattern

The core reliability problem: if you write to the database and then publish to Kafka in two separate operations, a crash between them leaves them out of sync.

**Solution:** Write the event to an `outbox` table *inside the same database transaction* as the order creation. A separate relay process reads from the outbox and publishes to Kafka, then marks events as processed.

```
POST /orders
  │
  ├─ BEGIN TRANSACTION
  │   ├─ Validate stock
  │   ├─ INSERT orders
  │   ├─ INSERT order_items
  │   └─ INSERT outbox (OrderCreatedEvent JSON)
  └─ COMMIT
       │
       └─ Relay → Kafka topic → payment-service, catalog-service
```

If the DB commit fails, nothing is published. If the relay crashes after publishing but before marking complete, it re-publishes — consumers must be idempotent.

---

## Design decisions

### JDBC over JPA

Raw JDBC with prepared statements rather than Hibernate. The order creation path is a tight transactional unit with known access patterns — no benefit from ORM abstraction, and it avoids the N+1 query and lazy-loading footguns that come with JPA in microservices.

### Stock check before insert

Inventory is validated before any rows are written. If stock is insufficient, no order record exists to clean up. This avoids compensating transactions or saga rollbacks for the common rejection case.

### Strongly typed event schema

`OrderCreatedEvent` is a Java record with UUID `correlationId` and `eventId`. The `eventId` enables idempotent processing downstream — consumers deduplicate on it.

---

## Running locally

Requires PostgreSQL and Kafka. Configure connection strings in `application.yml`.

```bash
# Start infrastructure (see mercury-docker)
docker compose up postgres kafka

./mvnw spring-boot:run
```

```bash
# Create an order
curl -X POST http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"items": [{"product_id": 1, "quantity": 2}]}'
```
