# Orderflow Mini-Guide

Welcome! This guide shows how to:
- Call the producer API to create an order and publish to Artemis.
- Understand the Artemis live/backup HA setup used by producer/consumer.
- Set up PostgreSQL (`orderflow`) that both services use.

---
## 1) Quick API How-To (Producer)

**Endpoint**  
`POST http://localhost:8080/api/v1/orders`

**Request body (JSON)**  
```json
{
  "orderId": "c5c0a4f3-6c33-4c8c-8b0b-2a6edb1f22aa", // optional; server generates if omitted
  "note": "first order"
}
```

**cURL**  
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"orderId":"c5c0a4f3-6c33-4c8c-8b0b-2a6edb1f22aa","note":"first order"}'
```

**Flow**  
1. Producer saves the order row with status `CREATED` in PostgreSQL.  
2. Producer publishes the `orderId` to queue `order.created` on Artemis.  
3. Consumer receives `order.created` and marks the order `PROCESSED` in PostgreSQL.

Expected response: `Order saved and published: <uuid>`

---
## 2) Artemis Live/Backup (HA) Setup

- Broker: Apache Artemis 2.53.x (live + backup).  
- Transport: Netty (tcp), failover enabled via `ActiveMQConnectionFactory` with `ServerLocator` that lists both live and backup.
- Credentials: `admin / admin` (configurable via `spring.artemis.*` in each service).
- Queues:
  - `order.created`  – main business queue.
  - `DLQ`            – default dead-letter queue used by Artemis; `DLQConsumer` logs anything routed here.

**Config files to edit if broker host/ports change**
- Producer: `orderflow-producer-service/src/main/resources/application.properties`
- Consumer: `orderflow-consumer-service/src/main/resources/application.properties`

Key properties:
```
artemis.live.host=localhost
artemis.live.port=61616
artemis.backup.host=localhost
artemis.backup.port=61617
spring.artemis.user=admin
spring.artemis.password=admin
```

**What happens on failover?**  
Both services use a shared `ServerLocator` with infinite reconnect attempts; when live goes down, they reconnect to backup automatically and resume consumption/production.

---
## 3) PostgreSQL Setup (`orderflow` database)

The producer and consumer share one DB to track order status.

**Create DB and user (psql)**  
```sql
CREATE DATABASE orderflow;
CREATE USER orderflow_user WITH ENCRYPTED PASSWORD 'orderflow_pass';
GRANT ALL PRIVILEGES ON DATABASE orderflow TO orderflow_user;
```

**JDBC settings (both services)** – adjust if you use a different user/password or DB name:
```
spring.datasource.url=jdbc:postgresql://localhost:5432/orderflow
spring.datasource.username=orderflow_user
spring.datasource.password=orderflow_pass
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

Hikari pool defaults are already tuned modestly (minIdle=2, maxPoolSize=10).

**Schema**  
`orders` table is auto-created by JPA (`ddl-auto=update`) on first run with columns:
- `id` (UUID, PK)
- `status` (`CREATED` → `PROCESSED`)
- `reason` (nullable)
- `created_at`, `updated_at`

---
## 4) Running the Services

Prereqs: Java 17, Maven, PostgreSQL running, Artemis live+backup running on ports above.

Producer:
```bash
cd orderflow-producer-service
mvn spring-boot:run
```

Consumer:
```bash
cd orderflow-consumer-service
mvn spring-boot:run
```

Send a test order via cURL (see §1). Check logs: producer should log `SENT ->`, consumer should log `Received order.created ->` and `Order <id> marked PROCESSED`.

---
## 5) Troubleshooting Cheatsheet

- **PSQL FATAL: database does not exist**: create `orderflow` DB or point `spring.datasource.url` to an existing one.  
- **Dialect error**: ensure `spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect` and driver class are set.  
- **Queue not found**: confirm Artemis has `order.created`; broker auto-creates if allowed; otherwise pre-create with proper roles.  
- **DLQ messages**: check queue `DLQ`; consumer `DLQConsumer` will log them.  
- **Failover not working**: verify backup broker reachable at `artemis.backup.host:port` and same credentials/roles configured.

Happy hacking! If you tweak ports, credentials, or queue names, update both services’ `application.properties` to stay in sync.
