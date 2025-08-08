# Configuration Documentation

This document provides comprehensive information about all configuration properties available in the Interface Exception Collector Service.

## Table of Contents

1. [Environment Profiles](#environment-profiles)
2. [Application Properties](#application-properties)
3. [Feature Flags](#feature-flags)
4. [Database Configuration](#database-configuration)
5. [Kafka Configuration](#kafka-configuration)
6. [Redis Configuration](#redis-configuration)
7. [Security Configuration](#security-configuration)
8. [External Services Configuration](#external-services-configuration)
9. [Logging Configuration](#logging-configuration)
10. [Monitoring Configuration](#monitoring-configuration)
11. [Resilience Configuration](#resilience-configuration)
12. [Kubernetes Configuration](#kubernetes-configuration)
13. [Environment Variables](#environment-variables)
14. [Configuration Validation](#configuration-validation)
15. [Hot Reload](#hot-reload)

## Environment Profiles

The application supports multiple environment profiles:

- **local**: For local development with minimal security
- **dev**: For development environment with debugging enabled
- **staging**: For staging environment with production-like settings
- **docker**: For Docker containerized deployment
- **prod**: For production environment with full security

### Activating Profiles

```bash
# Via command line
java -jar app.jar --spring.profiles.active=prod

# Via environment variable
export SPRING_PROFILES_ACTIVE=prod

# Via Kubernetes
env:
  - name: SPRING_PROFILES_ACTIVE
    value: "prod"
```

## Application Properties

### Core Application Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.application.name` | String | `interface-exception-collector-service` | Application name |
| `server.port` | Integer | `8080` | Server port |
| `server.shutdown` | String | `graceful` | Shutdown mode |

### Example Configuration

```yaml
spring:
  application:
    name: interface-exception-collector-service
server:
  port: 8080
  shutdown: graceful
```

## Feature Flags

Feature flags allow for gradual rollout and runtime toggling of features.

### Available Feature Flags

| Flag | Type | Default | Description |
|------|------|---------|-------------|
| `app.features.enhanced-logging` | Boolean | `false` | Enable enhanced logging with additional context |
| `app.features.debug-mode` | Boolean | `false` | Enable debug mode with verbose logging |
| `app.features.payload-caching` | Boolean | `true` | Enable caching of external service payloads |
| `app.features.circuit-breaker` | Boolean | `true` | Enable circuit breaker for external services |
| `app.features.retry-mechanism` | Boolean | `true` | Enable retry mechanism for failed operations |
| `app.features.hot-reload` | Boolean | `false` | Enable configuration hot reload |
| `app.features.metrics-collection` | Boolean | `true` | Enable metrics collection |
| `app.features.audit-logging` | Boolean | `false` | Enable audit logging for security events |

### Example Configuration

```yaml
app:
  features:
    enhanced-logging: true
    debug-mode: false
    payload-caching: true
    circuit-breaker: true
    retry-mechanism: true
    hot-reload: false
    metrics-collection: true
    audit-logging: true
```

### Runtime Feature Toggle

Features can be toggled at runtime via the management endpoint:

```bash
# Enable a feature
curl -X POST http://localhost:8080/actuator/config-reload/feature-flags \
  -H "Content-Type: application/json" \
  -d '{"featureName": "debug-mode", "enabled": true}'

# Get all feature flags
curl http://localhost:8080/actuator/config-reload/feature-flags
```

## Database Configuration

### Connection Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.datasource.url` | String | Required | PostgreSQL JDBC URL |
| `spring.datasource.username` | String | Required | Database username |
| `spring.datasource.password` | String | Required | Database password |
| `spring.datasource.driver-class-name` | String | `org.postgresql.Driver` | JDBC driver class |

### Connection Pool Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.datasource.hikari.maximum-pool-size` | Integer | `50` | Maximum pool size |
| `spring.datasource.hikari.minimum-idle` | Integer | `10` | Minimum idle connections |
| `spring.datasource.hikari.connection-timeout` | Long | `30000` | Connection timeout (ms) |
| `spring.datasource.hikari.idle-timeout` | Long | `600000` | Idle timeout (ms) |
| `spring.datasource.hikari.max-lifetime` | Long | `1800000` | Maximum connection lifetime (ms) |
| `spring.datasource.hikari.leak-detection-threshold` | Long | `60000` | Leak detection threshold (ms) |

### JPA Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.jpa.hibernate.ddl-auto` | String | `validate` | Hibernate DDL mode |
| `spring.jpa.show-sql` | Boolean | `false` | Show SQL statements |
| `spring.jpa.properties.hibernate.dialect` | String | `PostgreSQLDialect` | Hibernate dialect |
| `spring.jpa.properties.hibernate.jdbc.batch_size` | Integer | `50` | JDBC batch size |

### Retry Configuration

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `app.database.retry.enabled` | Boolean | `true` | Enable database retry |
| `app.database.retry.max-attempts` | Integer | `5` | Maximum retry attempts |
| `app.database.retry.initial-interval` | Long | `1000` | Initial retry interval (ms) |
| `app.database.retry.multiplier` | Double | `2.0` | Backoff multiplier |
| `app.database.retry.max-interval` | Long | `30000` | Maximum retry interval (ms) |

### Example Configuration

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/exception_collector_db?sslmode=require
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

app:
  database:
    retry:
      enabled: true
      max-attempts: 5
      initial-interval: 1000
      multiplier: 2.0
      max-interval: 30000
```

## Kafka Configuration

### Connection Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.kafka.bootstrap-servers` | String | Required | Kafka bootstrap servers |
| `spring.kafka.consumer.group-id` | String | Required | Consumer group ID |
| `spring.kafka.consumer.auto-offset-reset` | String | `earliest` | Offset reset strategy |

### Consumer Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.kafka.consumer.key-deserializer` | String | `StringDeserializer` | Key deserializer |
| `spring.kafka.consumer.value-deserializer` | String | `JsonDeserializer` | Value deserializer |
| `spring.kafka.consumer.properties.max.poll.records` | Integer | `100` | Max poll records |
| `spring.kafka.consumer.properties.session.timeout.ms` | Integer | `30000` | Session timeout |
| `spring.kafka.consumer.properties.heartbeat.interval.ms` | Integer | `3000` | Heartbeat interval |

### Producer Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.kafka.producer.key-serializer` | String | `StringSerializer` | Key serializer |
| `spring.kafka.producer.value-serializer` | String | `JsonSerializer` | Value serializer |
| `spring.kafka.producer.acks` | String | `all` | Acknowledgment mode |
| `spring.kafka.producer.retries` | Integer | `5` | Producer retries |
| `spring.kafka.producer.properties.batch.size` | Integer | `32768` | Batch size |
| `spring.kafka.producer.properties.linger.ms` | Integer | `10` | Linger time |
| `spring.kafka.producer.properties.compression.type` | String | `snappy` | Compression type |

### SSL Configuration

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.kafka.consumer.properties.security.protocol` | String | `PLAINTEXT` | Security protocol |
| `spring.kafka.consumer.properties.ssl.truststore.location` | String | - | Truststore location |
| `spring.kafka.consumer.properties.ssl.truststore.password` | String | - | Truststore password |
| `spring.kafka.consumer.properties.ssl.keystore.location` | String | - | Keystore location |
| `spring.kafka.consumer.properties.ssl.keystore.password` | String | - | Keystore password |
| `spring.kafka.consumer.properties.ssl.key.password` | String | - | Key password |

### Dead Letter Queue Configuration

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `app.kafka.dead-letter.enabled` | Boolean | `true` | Enable dead letter queue |
| `app.kafka.dead-letter.suffix` | String | `.DLT` | DLQ topic suffix |
| `app.kafka.dead-letter.max-retries` | Integer | `5` | Maximum retries before DLQ |
| `app.kafka.dead-letter.retry-interval` | Long | `1000` | Retry interval (ms) |

### Topic Configuration

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `app.kafka.topics.order-rejected` | String | `OrderRejected` | Order rejected topic |
| `app.kafka.topics.order-cancelled` | String | `OrderCancelled` | Order cancelled topic |
| `app.kafka.topics.collection-rejected` | String | `CollectionRejected` | Collection rejected topic |
| `app.kafka.topics.distribution-failed` | String | `DistributionFailed` | Distribution failed topic |
| `app.kafka.topics.validation-error` | String | `ValidationError` | Validation error topic |
| `app.kafka.topics.exception-captured` | String | `ExceptionCaptured` | Exception captured topic |
| `app.kafka.topics.exception-resolved` | String | `ExceptionResolved` | Exception resolved topic |
| `app.kafka.topics.critical-alert` | String | `CriticalExceptionAlert` | Critical alert topic |

### Example Configuration

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    consumer:
      group-id: interface-exception-collector
      auto-offset-reset: earliest
      properties:
        max.poll.records: 100
        session.timeout.ms: 30000
        security.protocol: SSL
        ssl.truststore.location: /etc/kafka/ssl/truststore.jks
        ssl.truststore.password: ${KAFKA_SSL_TRUSTSTORE_PASSWORD}
        ssl.keystore.location: /etc/kafka/ssl/keystore.jks
        ssl.keystore.password: ${KAFKA_SSL_KEYSTORE_PASSWORD}
    producer:
      acks: all
      retries: 5
      properties:
        batch.size: 32768
        compression.type: snappy
        security.protocol: SSL
        ssl.truststore.location: /etc/kafka/ssl/truststore.jks
        ssl.truststore.password: ${KAFKA_SSL_TRUSTSTORE_PASSWORD}
        ssl.keystore.location: /etc/kafka/ssl/keystore.jks
        ssl.keystore.password: ${KAFKA_SSL_KEYSTORE_PASSWORD}

app:
  kafka:
    dead-letter:
      enabled: true
      suffix: .DLT
      max-retries: 5
      retry-interval: 1000
    topics:
      order-rejected: OrderRejected
      exception-captured: ExceptionCaptured
      critical-alert: CriticalExceptionAlert
```

## Redis Configuration

### Connection Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.data.redis.host` | String | Required | Redis host |
| `spring.data.redis.port` | Integer | `6379` | Redis port |
| `spring.data.redis.password` | String | - | Redis password |
| `spring.data.redis.timeout` | Duration | `3000ms` | Connection timeout |
| `spring.data.redis.ssl` | Boolean | `false` | Enable SSL |

### Connection Pool Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.data.redis.lettuce.pool.max-active` | Integer | `20` | Maximum active connections |
| `spring.data.redis.lettuce.pool.max-idle` | Integer | `20` | Maximum idle connections |
| `spring.data.redis.lettuce.pool.min-idle` | Integer | `5` | Minimum idle connections |
| `spring.data.redis.lettuce.pool.max-wait` | Duration | `2000ms` | Maximum wait time |

### Cache Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `spring.cache.type` | String | `redis` | Cache type |
| `spring.cache.redis.time-to-live` | Duration | `600000` | Default TTL (ms) |
| `spring.cache.redis.cache-null-values` | Boolean | `false` | Cache null values |
| `spring.cache.redis.use-key-prefix` | Boolean | `true` | Use key prefix |
| `spring.cache.redis.key-prefix` | String | `exception-collector:` | Key prefix |

### Example Configuration

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: 6379
      password: ${REDIS_PASSWORD}
      timeout: 3000ms
      ssl: true
      lettuce:
        pool:
          max-active: 20
          max-idle: 20
          min-idle: 5
          max-wait: 2000ms
  cache:
    type: redis
    redis:
      time-to-live: 600000
      cache-null-values: false
      use-key-prefix: true
      key-prefix: "prod-exception-collector:"
```

## Security Configuration

### JWT Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `app.security.jwt.secret` | String | Required | JWT signing secret (min 32 chars) |
| `app.security.jwt.expiration` | Long | `3600000` | Token expiration (ms) |
| `app.security.jwt.issuer` | String | `interface-exception-collector` | Token issuer |
| `app.security.jwt.audience` | String | `biopro-services` | Token audience |

### Rate Limiting

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `app.security.rate-limit.enabled` | Boolean | `true` | Enable rate limiting |
| `app.security.rate-limit.requests-per-minute` | Integer | `60` | Requests per minute |
| `app.security.rate-limit.burst-capacity` | Integer | `10` | Burst capacity |

### TLS Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `app.security.tls.enabled` | Boolean | `false` | Enable application TLS |
| `app.security.tls.keystore.path` | String | - | Keystore path |
| `app.security.tls.keystore.password` | String | - | Keystore password |
| `app.security.tls.truststore.path` | String | - | Truststore path |
| `app.security.tls.truststore.password` | String | - | Truststore password |

### Audit Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `app.security.audit.enabled` | Boolean | `false` | Enable audit logging |
| `app.security.audit.log-successful-requests` | Boolean | `false` | Log successful requests |
| `app.security.audit.log-failed-requests` | Boolean | `true` | Log failed requests |

### Example Configuration

```yaml
app:
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 3600000
      issuer: interface-exception-collector
      audience: biopro-services
    rate-limit:
      enabled: true
      requests-per-minute: 60
      burst-capacity: 10
    tls:
      enabled: true
      keystore:
        path: /etc/ssl/keystore.jks
        password: ${TLS_KEYSTORE_PASSWORD}
      truststore:
        path: /etc/ssl/truststore.jks
        password: ${TLS_TRUSTSTORE_PASSWORD}
    audit:
      enabled: true
      log-successful-requests: false
      log-failed-requests: true
```

## External Services Configuration

### Service Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `app.source-services.order.base-url` | String | Required | Order service base URL |
| `app.source-services.order.api-key` | String | Required | Order service API key |
| `app.source-services.order.auth-header` | String | `X-API-Key` | Auth header name |
| `app.source-services.collection.base-url` | String | Required | Collection service base URL |
| `app.source-services.collection.api-key` | String | Required | Collection service API key |
| `app.source-services.collection.auth-header` | String | `X-API-Key` | Auth header name |
| `app.source-services.distribution.base-url` | String | Required | Distribution service base URL |
| `app.source-services.distribution.api-key` | String | Required | Distribution service API key |
| `app.source-services.distribution.auth-header` | String | `X-API-Key` | Auth header name |

### Timeout Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `app.source-services.timeout` | Integer | `5000` | Request timeout (ms) |
| `app.source-services.connection-timeout` | Integer | `3000` | Connection timeout (ms) |
| `app.source-services.read-timeout` | Integer | `5000` | Read timeout (ms) |

### Example Configuration

```yaml
app:
  source-services:
    order:
      base-url: ${ORDER_SERVICE_URL}
      api-key: ${ORDER_SERVICE_API_KEY}
      auth-header: X-API-Key
    collection:
      base-url: ${COLLECTION_SERVICE_URL}
      api-key: ${COLLECTION_SERVICE_API_KEY}
      auth-header: X-API-Key
    distribution:
      base-url: ${DISTRIBUTION_SERVICE_URL}
      api-key: ${DISTRIBUTION_SERVICE_API_KEY}
      auth-header: X-API-Key
    timeout: 5000
    connection-timeout: 3000
    read-timeout: 5000
```

## Logging Configuration

### Log Levels

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `logging.level.com.arcone.biopro.exception.collector` | String | `INFO` | Application log level |
| `logging.level.org.springframework.kafka` | String | `WARN` | Kafka log level |
| `logging.level.org.springframework.web` | String | `WARN` | Web log level |
| `logging.level.org.hibernate.SQL` | String | `WARN` | SQL log level |
| `logging.level.io.github.resilience4j` | String | `INFO` | Resilience4j log level |
| `logging.level.root` | String | `WARN` | Root log level |

### File Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `logging.file.name` | String | `/app/logs/application.log` | Log file path |
| `logging.file.max-size` | String | `200MB` | Maximum file size |
| `logging.file.max-history` | Integer | `30` | Maximum file history |
| `logging.file.total-size-cap` | String | `5GB` | Total size cap |

### Pattern Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `logging.pattern.console` | String | See example | Console log pattern |
| `logging.pattern.file` | String | See example | File log pattern |

### Example Configuration

```yaml
logging:
  level:
    com.arcone.biopro.exception.collector: INFO
    org.springframework.kafka: WARN
    org.springframework.web: WARN
    org.hibernate.SQL: WARN
    io.github.resilience4j: INFO
    root: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId:-}] [%X{transactionId:-}] [%X{interfaceType:-}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId:-}] [%X{transactionId:-}] [%X{interfaceType:-}] %logger{36} - %msg%n"
  file:
    name: /app/logs/application.log
    max-size: 200MB
    max-history: 30
    total-size-cap: 5GB
```

## Monitoring Configuration

### Management Endpoints

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `management.endpoints.web.exposure.include` | String | `health,info,metrics,prometheus` | Exposed endpoints |
| `management.endpoints.web.base-path` | String | `/actuator` | Base path |
| `management.endpoint.health.show-details` | String | `when-authorized` | Health details |
| `management.endpoint.health.show-components` | String | `when-authorized` | Health components |

### Health Checks

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `management.health.circuitbreakers.enabled` | Boolean | `true` | Circuit breaker health |
| `management.health.kafka.enabled` | Boolean | `true` | Kafka health |
| `management.health.redis.enabled` | Boolean | `true` | Redis health |
| `management.health.diskspace.enabled` | Boolean | `true` | Disk space health |
| `management.health.diskspace.threshold` | String | `10GB` | Disk space threshold |

### Metrics Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `management.metrics.export.prometheus.enabled` | Boolean | `true` | Enable Prometheus export |
| `management.metrics.export.prometheus.step` | Duration | `30s` | Metrics step |
| `management.metrics.export.prometheus.descriptions` | Boolean | `true` | Include descriptions |

### Example Configuration

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      show-components: when-authorized
  health:
    circuitbreakers:
      enabled: true
    kafka:
      enabled: true
    redis:
      enabled: true
    diskspace:
      enabled: true
      threshold: 10GB
  metrics:
    export:
      prometheus:
        enabled: true
        step: 30s
        descriptions: true
    tags:
      application: interface-exception-collector-service
      environment: production
      version: 1.0.0
```

## Resilience Configuration

### Circuit Breaker Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `resilience4j.circuitbreaker.instances.source-service.sliding-window-size` | Integer | `20` | Sliding window size |
| `resilience4j.circuitbreaker.instances.source-service.minimum-number-of-calls` | Integer | `10` | Minimum calls |
| `resilience4j.circuitbreaker.instances.source-service.failure-rate-threshold` | Integer | `50` | Failure rate threshold |
| `resilience4j.circuitbreaker.instances.source-service.wait-duration-in-open-state` | Duration | `30s` | Wait duration |

### Retry Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `resilience4j.retry.instances.source-service.max-attempts` | Integer | `3` | Maximum attempts |
| `resilience4j.retry.instances.source-service.wait-duration` | Duration | `1s` | Wait duration |
| `resilience4j.retry.instances.source-service.exponential-backoff-multiplier` | Integer | `2` | Backoff multiplier |

### Time Limiter Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `resilience4j.timelimiter.instances.source-service.timeout-duration` | Duration | `5s` | Timeout duration |
| `resilience4j.timelimiter.instances.source-service.cancel-running-future` | Boolean | `true` | Cancel running future |

### Example Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      source-service:
        register-health-indicator: true
        sliding-window-size: 20
        minimum-number-of-calls: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        automatic-transition-from-open-to-half-open-enabled: true
  retry:
    instances:
      source-service:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2
  timelimiter:
    instances:
      source-service:
        timeout-duration: 5s
        cancel-running-future: true
```

## Kubernetes Configuration

### Helm Values

The application can be configured via Helm values for Kubernetes deployment:

```yaml
# values.yaml
environment: production
datacenter: us-east-1
region: us-east-1

app:
  name: interface-exception-collector-service

image:
  repository: interface-exception-collector
  tag: "1.0.0"
  pullPolicy: IfNotPresent

database:
  host: postgres-service
  port: 5432
  name: exception_collector_db
  username: exception_user
  password: "secure-password"
  sslMode: require

redis:
  host: redis-service
  port: 6379
  password: "redis-password"
  ssl: true

kafka:
  bootstrapServers: kafka-service:9092
  ssl:
    enabled: true
    truststore: "base64-encoded-truststore"
    keystore: "base64-encoded-keystore"
    truststorePassword: "truststore-password"
    keystorePassword: "keystore-password"
    keyPassword: "key-password"

security:
  jwt:
    secret: "jwt-secret-key-32-characters-minimum"
    expiration: 3600000
  tls:
    enabled: true
    keystorePassword: "keystore-password"
    truststorePassword: "truststore-password"

externalServices:
  order:
    baseUrl: "https://order-service:8080"
    apiKey: "order-api-key"
  collection:
    baseUrl: "https://collection-service:8080"
    apiKey: "collection-api-key"
  distribution:
    baseUrl: "https://distribution-service:8080"
    apiKey: "distribution-api-key"

features:
  enhancedLogging: false
  debugMode: false
  payloadCaching: true
  circuitBreaker: true
  retryMechanism: true
  hotReload: false
  metricsCollection: true
  auditLogging: true
```

## Environment Variables

### Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_HOST` | Database host | `postgres-service` |
| `DB_NAME` | Database name | `exception_collector_db` |
| `DB_USERNAME` | Database username | `exception_user` |
| `DB_PASSWORD` | Database password | `secure-password` |
| `REDIS_HOST` | Redis host | `redis-service` |
| `REDIS_PASSWORD` | Redis password | `redis-password` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `kafka-service:9092` |
| `JWT_SECRET` | JWT signing secret | `jwt-secret-32-chars-minimum` |
| `ORDER_SERVICE_URL` | Order service URL | `https://order-service:8080` |
| `ORDER_SERVICE_API_KEY` | Order service API key | `order-api-key` |
| `COLLECTION_SERVICE_URL` | Collection service URL | `https://collection-service:8080` |
| `COLLECTION_SERVICE_API_KEY` | Collection service API key | `collection-api-key` |
| `DISTRIBUTION_SERVICE_URL` | Distribution service URL | `https://distribution-service:8080` |
| `DISTRIBUTION_SERVICE_API_KEY` | Distribution service API key | `distribution-api-key` |

### Optional Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profiles | `default` |
| `SERVER_PORT` | Server port | `8080` |
| `LOG_LEVEL_APP` | Application log level | `INFO` |
| `LOG_LEVEL_ROOT` | Root log level | `WARN` |
| `FEATURE_DEBUG_MODE` | Enable debug mode | `false` |
| `FEATURE_ENHANCED_LOGGING` | Enable enhanced logging | `false` |
| `RATE_LIMIT_ENABLED` | Enable rate limiting | `true` |
| `RATE_LIMIT_RPM` | Requests per minute | `60` |
| `TLS_ENABLED` | Enable TLS | `false` |

## Configuration Validation

The application performs comprehensive configuration validation on startup:

### Validation Checks

1. **Database Configuration**
   - URL format validation
   - Connection pool settings validation
   - Required credentials check

2. **Kafka Configuration**
   - Bootstrap servers validation
   - SSL configuration validation (if enabled)
   - Consumer group validation

3. **Redis Configuration**
   - Host and port validation
   - Connection pool settings validation

4. **External Services Configuration**
   - URL format validation
   - API key presence validation
   - Timeout settings validation

5. **Security Configuration**
   - JWT secret length validation
   - TLS configuration validation (if enabled)
   - Rate limiting settings validation

6. **Feature Flags Validation**
   - Boolean value validation
   - Dependency validation

### Validation Failure Handling

If validation fails, the application will:
1. Log detailed error messages
2. Prevent application startup
3. Return non-zero exit code

### Example Validation Output

```
2025-08-05 10:30:00.123 [main] INFO  ConfigurationValidator - Starting configuration validation...
2025-08-05 10:30:00.125 [main] INFO  ConfigurationValidator - Feature flags configuration:
2025-08-05 10:30:00.125 [main] INFO  ConfigurationValidator -   Enhanced Logging: false
2025-08-05 10:30:00.125 [main] INFO  ConfigurationValidator -   Debug Mode: false
2025-08-05 10:30:00.125 [main] INFO  ConfigurationValidator -   Payload Caching: true
2025-08-05 10:30:00.126 [main] INFO  ConfigurationValidator - Configuration validation completed successfully
2025-08-05 10:30:00.127 [main] INFO  ConfigurationValidator - Configuration Summary:
2025-08-05 10:30:00.127 [main] INFO  ConfigurationValidator -   Active Profile: prod
2025-08-05 10:30:00.127 [main] INFO  ConfigurationValidator -   Application Name: interface-exception-collector-service
2025-08-05 10:30:00.127 [main] INFO  ConfigurationValidator -   Server Port: 8080
2025-08-05 10:30:00.128 [main] INFO  ConfigurationValidator -   Database URL: jdbc:postgresql://postgres-service:5432/***
2025-08-05 10:30:00.128 [main] INFO  ConfigurationValidator -   Kafka Bootstrap Servers: kafka-service:9092
2025-08-05 10:30:00.128 [main] INFO  ConfigurationValidator -   Redis Host: redis-service:6379
2025-08-05 10:30:00.128 [main] INFO  ConfigurationValidator -   TLS Enabled: true
2025-08-05 10:30:00.128 [main] INFO  ConfigurationValidator -   Rate Limiting Enabled: true
```

## Hot Reload

The application supports hot-reloading of certain configuration properties without requiring a restart.

### Supported Hot Reload Operations

1. **Feature Flags**: Can be toggled at runtime
2. **Logging Levels**: Can be changed at runtime
3. **Spring Cloud Config**: Full configuration refresh (if available)

### Hot Reload Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/actuator/config-reload` | POST | Reload all configuration |
| `/actuator/config-reload/feature-flags` | POST | Reload feature flags only |
| `/actuator/config-reload/logging` | POST | Reload logging configuration |

### Example Hot Reload Usage

```bash
# Reload all configuration
curl -X POST http://localhost:8080/actuator/config-reload

# Reload only feature flags
curl -X POST http://localhost:8080/actuator/config-reload/feature-flags

# Get reload status
curl http://localhost:8080/actuator/config-reload/status
```

### Hot Reload Response

```json
{
  "reloadTime": "2025-08-05T10:30:00.123",
  "status": "success",
  "changes": {
    "debug-mode": "false -> true",
    "enhanced-logging": "false -> true"
  },
  "currentFlags": {
    "debug-mode": true,
    "enhanced-logging": true,
    "payload-caching": true,
    "circuit-breaker": true,
    "retry-mechanism": true,
    "hot-reload": true,
    "metrics-collection": true,
    "audit-logging": true
  }
}
```

### Limitations

- Database connection settings cannot be hot-reloaded
- Kafka connection settings cannot be hot-reloaded
- Server port and SSL settings require restart
- JPA/Hibernate settings require restart

## Best Practices

1. **Environment-Specific Configuration**
   - Use separate configuration files for each environment
   - Externalize sensitive configuration via environment variables
   - Use Kubernetes ConfigMaps and Secrets for containerized deployments

2. **Security**
   - Never commit sensitive values to version control
   - Use strong JWT secrets (minimum 32 characters)
   - Enable TLS in production environments
   - Regularly rotate API keys and passwords

3. **Performance**
   - Tune database connection pool settings based on load
   - Configure appropriate cache TTL values
   - Set reasonable timeout values for external services
   - Monitor and adjust JVM heap settings

4. **Monitoring**
   - Enable comprehensive health checks
   - Configure appropriate log levels for each environment
   - Use structured logging with correlation IDs
   - Set up alerts for critical configuration changes

5. **Feature Flags**
   - Use feature flags for gradual rollouts
   - Document feature flag purposes and dependencies
   - Regularly clean up unused feature flags
   - Monitor feature flag usage and performance impact