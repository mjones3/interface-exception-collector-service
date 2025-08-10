# Interface Exception Collector Service

Centralized collection and management of interface exceptions from all BioPro interface services (Order, Collection, Distribution). This service provides real-time exception processing, retry capabilities, comprehensive search and filtering, and operational dashboards for monitoring interface health.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Deployment](#deployment)
- [Development](#development)
- [Monitoring and Observability](#monitoring-and-observability)
- [Troubleshooting](#troubleshooting)
- [Operational Runbooks](#operational-runbooks)
- [Contributing](#contributing)

## Architecture Overview

The Interface Exception Collector Service is built using event-driven architecture with clean architecture principles. It processes exception events from multiple BioPro interface services and provides comprehensive exception management capabilities.

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    BioPro Interface Services                    │
├─────────────────┬─────────────────┬─────────────────────────────┤
│  Order Service  │Collection Service│  Distribution Service      │
└─────────┬───────┴─────────┬───────┴─────────────┬───────────────┘
          │                 │                     │
          ▼                 ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Kafka Event Bus                           │
├─────────────────┬─────────────────┬─────────────────────────────┤
│OrderRejected    │CollectionRejected│  DistributionFailed        │
│OrderCancelled   │                 │  ValidationError            │
└─────────┬───────┴─────────┬───────┴─────────────┬───────────────┘
          │                 │                     │
          ▼                 ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│              Exception Collector Service                        │
├─────────────────┬─────────────────┬─────────────────────────────┤
│   API Layer     │  Service Layer  │  Infrastructure Layer       │
│ - Controllers   │ - Business Logic│ - Kafka Consumers/Producers │
│ - DTOs          │ - Orchestration │ - Database Access           │
│ - Mappers       │ - Validation    │ - External Service Clients  │
└─────────┬───────┴─────────┬───────┴─────────────┬───────────────┘
          │                 │                     │
          ▼                 ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Data Layer                                 │
├─────────────────┬─────────────────┬─────────────────────────────┤
│  PostgreSQL     │   Redis Cache   │    External Services        │
│  Database       │                 │  - Order Service            │
│                 │                 │  - Collection Service       │
│                 │                 │  - Distribution Service     │
└─────────────────┴─────────────────┴─────────────────────────────┘
```

### Layer Architecture

The service follows clean architecture principles with clear layer separation:

- **API Layer** (`api/`): REST controllers, DTOs, and API mappers
  - Controllers handle HTTP requests and responses
  - DTOs define request/response data structures
  - Mappers convert between DTOs and domain objects

- **Application Layer** (`application/`): Business services and use cases  
  - Orchestrates business workflows
  - Implements use cases and business rules
  - Coordinates between domain and infrastructure layers

- **Domain Layer** (`domain/`): Core business entities, events, and domain logic
  - Contains business entities and value objects
  - Defines domain events and business rules
  - Independent of external frameworks and technologies

- **Infrastructure Layer** (`infrastructure/`): External integrations
  - Kafka consumers and producers
  - Database repositories and configurations
  - HTTP clients for external service integration
  - Configuration and cross-cutting concerns

### Event Flow

1. **Exception Events**: Interface services publish exception events to Kafka topics
2. **Event Processing**: Kafka consumers process events and extract exception data
3. **Business Logic**: Service layer applies categorization, severity assignment, and business rules
4. **Data Persistence**: Exception data is stored in PostgreSQL with proper indexing
5. **Event Publishing**: Outbound events are published for downstream systems
6. **API Access**: REST APIs provide access to exception data with filtering and search capabilities

## Features

- **Real-time Exception Collection**: Automatic capture of exception events from Kafka topics
- **Comprehensive Search**: Full-text search and filtering capabilities
- **Retry Management**: Automated and manual retry operations with history tracking
- **Critical Alerting**: Automatic alerts for critical exceptions and escalation
- **Operational Dashboard**: REST APIs for monitoring and management
- **High Availability**: Resilient design with circuit breakers and graceful degradation

## Prerequisites

- Java 21+
- Maven 3.8+
- Docker and Docker Compose
- Kubernetes cluster (for production deployment)
- Helm 3.x (for Kubernetes deployment)

## Quick Start

### Automated Setup (Recommended)

For new developers, use the getting started script:

```bash
./scripts/getting-started.sh
```

This will automatically set up your local development environment.

### Manual Setup

1. **Start infrastructure services**:
   ```bash
   docker-compose up -d postgres kafka redis
   ```

2. **Validate infrastructure is ready**:
   ```bash
   ./scripts/validate-infrastructure.sh
   ```

3. **Run database migrations**:
   ```bash
   ./scripts/run-migrations.sh --local
   ```

4. **Create Kafka topics**:
   ```bash
   ./scripts/create-kafka-topics.sh --local
   ```

5. **Start the application**:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

### Development with Tilt

For advanced development with hot reload:

```bash
./scripts/deploy-local.sh
```

### Access Points

- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/actuator/health
- **Tilt UI**: http://localhost:10350 (when using Tilt)

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profiles | `local` |
| `DATABASE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/exception_collector` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | `localhost:9092` |
| `REDIS_HOST` | Redis cache host | `localhost` |
| `REDIS_PORT` | Redis cache port | `6379` |

### Application Profiles

- **local**: Local development with Docker Compose
- **dev**: Development environment
- **staging**: Staging environment  
- **prod**: Production environment

## API Documentation

### Exception Management

- `GET /api/v1/exceptions` - List exceptions with filtering
- `GET /api/v1/exceptions/{transactionId}` - Get exception details
- `GET /api/v1/exceptions/search` - Full-text search
- `GET /api/v1/exceptions/summary` - Aggregated statistics

#### Example API Calls

**List Exceptions:**
```bash
curl -H @auth_header.txt "http://localhost:8080/api/v1/exceptions"
```

**Search Exceptions:**
```bash
curl -H @auth_header.txt "http://localhost:8080/api/v1/exceptions/search?query=suspended"
```

**Get Exception Details:**
```bash
curl -H @auth_header.txt "http://localhost:8080/api/v1/exceptions/7aa8b2de-6b39-48fc-a309-bc22df28fb5b"
```

**Get Exception Summary:**
```bash
curl -H @auth_header.txt "http://localhost:8080/api/v1/exceptions/summary?timeRange=week"
```

### Retry Operations

- `POST /api/v1/exceptions/{transactionId}/retry` - Initiate retry
- `GET /api/v1/exceptions/{transactionId}/retry-history` - Get retry history

#### Initiate Retry

**Endpoint:** `POST /api/v1/exceptions/{transactionId}/retry`

**Request Payload:**
```json
{
  "initiatedBy": "user@company.com",
  "reason": "Customer requested retry after fixing data issue"
}
```

**Fields:**
- `initiatedBy` (required): String, max 255 characters - User or system that initiated the retry
- `reason` (optional): String, max 500 characters - Reason for the retry attempt

**Example Response:**
```json
{
  "retryId": 123,
  "status": "PENDING",
  "message": "Retry operation initiated successfully",
  "estimatedCompletionTime": "2025-08-09T22:36:37.708251-04:00",
  "attemptNumber": 2
}
```

#### Get Retry History

**Endpoint:** `GET /api/v1/exceptions/{transactionId}/retry-history`

**Example Response:**
```json
[
  {
    "id": 1,
    "attemptNumber": 1,
    "status": "PENDING",
    "initiatedBy": "test user",
    "initiatedAt": "2025-08-10T01:46:37.503291Z",
    "completedAt": null,
    "resultSuccess": null,
    "resultMessage": null,
    "resultResponseCode": null,
    "resultErrorDetails": null
  }
]
```

### Management Operations

- `PUT /api/v1/exceptions/{transactionId}/acknowledge` - Acknowledge exception
- `PUT /api/v1/exceptions/{transactionId}/resolve` - Mark as resolved

#### Acknowledge Exception

**Endpoint:** `PUT /api/v1/exceptions/{transactionId}/acknowledge`

**Request Payload:**
```json
{
  "acknowledgedBy": "user@company.com",
  "notes": "Reviewed and assigned to development team"
}
```

**Fields:**
- `acknowledgedBy` (required): String, max 255 characters - User or system that acknowledged the exception
- `notes` (optional): String, max 1000 characters - Optional notes about the acknowledgment

**Example Response:**
```json
{
  "status": "ACKNOWLEDGED",
  "acknowledgedAt": "2025-08-09T22:31:27.507582-04:00",
  "acknowledgedBy": "user@company.com",
  "notes": "Reviewed and assigned to development team",
  "transactionId": "7aa8b2de-6b39-48fc-a309-bc22df28fb5b"
}
```

#### Resolve Exception

**Endpoint:** `PUT /api/v1/exceptions/{transactionId}/resolve`

**Request Payload:**
```json
{
  "resolvedBy": "user@company.com",
  "resolutionMethod": "MANUAL_RESOLUTION",
  "resolutionNotes": "Fixed data validation issue in source system"
}
```

**Fields:**
- `resolvedBy` (required): String, max 255 characters - User or system that resolved the exception
- `resolutionMethod` (required): Enum - Method used to resolve the exception
- `resolutionNotes` (optional): String, max 1000 characters - Notes about how the exception was resolved

**Available Resolution Methods:**
- `RETRY_SUCCESS` - Exception was resolved through a successful retry
- `MANUAL_RESOLUTION` - Exception was manually resolved by a user
- `CUSTOMER_RESOLVED` - Exception was resolved by the customer
- `AUTOMATED` - Exception was resolved automatically by the system

**Example Response:**
```json
{
  "status": "RESOLVED",
  "resolvedAt": "2025-08-09T22:31:37.708251-04:00",
  "resolvedBy": "user@company.com",
  "resolutionMethod": "MANUAL_RESOLUTION",
  "resolutionNotes": "Fixed data validation issue in source system",
  "transactionId": "7aa8b2de-6b39-48fc-a309-bc22df28fb5b",
  "totalRetryAttempts": 1
}
```

#### Example cURL Commands

**Acknowledge Exception:**
```bash
curl -X PUT \
  -H @auth_header.txt \
  -H "Content-Type: application/json" \
  -d '{"acknowledgedBy": "user@company.com", "notes": "Reviewed and assigned to development team"}' \
  "http://localhost:8080/api/v1/exceptions/7aa8b2de-6b39-48fc-a309-bc22df28fb5b/acknowledge"
```

**Resolve Exception:**
```bash
curl -X PUT \
  -H @auth_header.txt \
  -H "Content-Type: application/json" \
  -d '{"resolvedBy": "user@company.com", "resolutionMethod": "MANUAL_RESOLUTION", "resolutionNotes": "Fixed data validation issue in source system"}' \
  "http://localhost:8080/api/v1/exceptions/7aa8b2de-6b39-48fc-a309-bc22df28fb5b/resolve"
```

**Initiate Retry:**
```bash
curl -X POST \
  -H @auth_header.txt \
  -H "Content-Type: application/json" \
  -d '{"initiatedBy": "user@company.com", "reason": "Customer requested retry after fixing data issue"}' \
  "http://localhost:8080/api/v1/exceptions/7aa8b2de-6b39-48fc-a309-bc22df28fb5b/retry"
```

### Authentication

All API endpoints require JWT authentication. Include the JWT token in the Authorization header:

```bash
Authorization: Bearer <your-jwt-token>
```

**Generate JWT Token for Testing:**
```bash
# Generate token with VIEWER role
node generate-jwt.js "test-user" "VIEWER"

# Generate token with ADMIN role (required for management operations)
node generate-jwt.js "test-user" "ADMIN"
```

**Role Requirements:**
- `VIEWER` - Can access read-only endpoints (GET operations)
- `OPERATOR` - Can perform retry operations and management actions
- `ADMIN` - Full access to all endpoints including management operations

**Note:** When using curl with long JWT tokens, save the token to a file to avoid command-line parsing issues:
```bash
echo "Authorization: Bearer <your-jwt-token>" > auth_header.txt
curl -H @auth_header.txt "http://localhost:8080/api/v1/exceptions"
```

### Health and Monitoring

- `GET /actuator/health` - Application health status
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/prometheus` - Prometheus metrics

## Deployment

### Kubernetes with Helm

1. **Deploy to development**:
   ```bash
   ./scripts/deploy.sh dev
   ```

2. **Deploy to staging**:
   ```bash
   ./scripts/deploy.sh staging
   ```

3. **Deploy to production**:
   ```bash
   ./scripts/deploy.sh prod
   ```

### Manual Kubernetes Deployment

1. **Setup infrastructure**:
   ```bash
   ./scripts/setup-infrastructure.sh
   ```

2. **Deploy application**:
   ```bash
   kubectl apply -f k8s/
   ```

## Development

### Building

```bash
# Compile and run tests
./mvnw clean verify

# Build Docker image
docker build -t interface-exception-collector:latest .

# Build with specific profile
./mvnw clean package -Pdev
```

### Testing

```bash
# Run unit tests
./mvnw test

# Run integration tests
./mvnw verify -Pintegration-tests

# Run all tests with coverage
./mvnw clean verify jacoco:report
```

### Code Quality

```bash
# Run static analysis
./mvnw spotbugs:check pmd:check checkstyle:check

# Format code
./mvnw spotless:apply
```

## Monitoring and Observability

### Metrics

The service exposes metrics via Micrometer:
- Exception processing rates
- API response times  
- Retry success rates
- Database connection pool metrics
- Kafka consumer lag

### Logging

Structured JSON logging with correlation IDs:
- Application logs: `/var/log/app/application.log`
- Access logs: `/var/log/app/access.log`
- Audit logs: `/var/log/app/audit.log`

### Health Checks

- **Liveness**: `/actuator/health/liveness`
- **Readiness**: `/actuator/health/readiness`
- **Database**: Automatic connection health monitoring
- **Kafka**: Consumer group health monitoring
- **Redis**: Cache connectivity monitoring

## Troubleshooting

### Quick Diagnostics

1. **Health Check**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. **Database Connection Issues**:
   ```bash
   # Check database connectivity
   kubectl exec -it <pod-name> -- pg_isready -h <db-host> -p 5432
   
   # Check database connection pool metrics
   curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
   ```

3. **Kafka Consumer Lag**:
   ```bash
   # Check consumer group status
   kubectl exec -it kafka-0 -- kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group interface-exception-collector
   
   # Check Kafka health
   curl http://localhost:8080/actuator/health/kafka
   ```

4. **High Memory Usage**:
   ```bash
   # Check JVM memory metrics
   curl http://localhost:8080/actuator/metrics/jvm.memory.used
   curl http://localhost:8080/actuator/metrics/jvm.gc.pause
   ```

5. **Redis Cache Issues**:
   ```bash
   # Check Redis connectivity
   curl http://localhost:8080/actuator/health/redis
   
   # Check cache metrics
   curl http://localhost:8080/actuator/metrics/cache.gets
   ```

### Log Analysis

```bash
# View application logs
kubectl logs -f deployment/interface-exception-collector

# Search for errors
kubectl logs deployment/interface-exception-collector | grep ERROR

# Follow logs with correlation ID
kubectl logs -f deployment/interface-exception-collector | grep "correlationId=abc-123"

# Check structured logs for specific patterns
kubectl logs deployment/interface-exception-collector | jq '.level == "ERROR"'
```

### Performance Issues

```bash
# Check API response times
curl http://localhost:8080/actuator/metrics/http.server.requests

# Check exception processing rates
curl http://localhost:8080/actuator/metrics/exception.processing.rate

# Check retry success rates
curl http://localhost:8080/actuator/metrics/retry.success.rate
```

### Recent Fixes and Known Issues

#### Circular Reference Issues (Fixed)

**Issue**: Some endpoints were returning 500 Internal Server Error due to StackOverflowError caused by circular references in JPA entity relationships during JSON serialization.

**Affected Endpoints**:
- `/api/v1/exceptions/search`
- `/api/v1/exceptions/{transactionId}/retry-history`

**Root Cause**: Bidirectional relationships between `InterfaceException`, `RetryAttempt`, and `OrderItem` entities caused infinite loops during JSON serialization when using `@Data` annotation from Lombok.

**Solution Applied**:
1. Replaced `@Data` with specific Lombok annotations (`@Getter`, `@Setter`, `@ToString`)
2. Excluded problematic fields from `toString()` methods using `@ToString(exclude = {...})`
3. Added Jackson annotations to handle circular references:
   - `@JsonManagedReference` on parent entity collections
   - `@JsonBackReference` on child entity references

**Status**: ✅ Fixed - All endpoints now return proper responses

#### JWT Token Authentication Issues (Fixed)

**Issue**: Long JWT tokens were causing 400 Bad Request errors when passed directly in curl command line.

**Root Cause**: Command-line parsing issues with long authorization headers in curl.

**Solution**: Use file-based headers for curl requests:
```bash
echo "Authorization: Bearer <token>" > auth_header.txt
curl -H @auth_header.txt "http://localhost:8080/api/endpoint"
```

**Status**: ✅ Fixed - Authentication working correctly

For detailed troubleshooting procedures, see the [Operational Runbooks](#operational-runbooks) section.

## Operational Runbooks

Comprehensive operational runbooks are available in the `docs/runbooks/` directory:

- [Service Startup and Shutdown](docs/runbooks/service-lifecycle.md)
- [Database Issues](docs/runbooks/database-troubleshooting.md)
- [Kafka Consumer Problems](docs/runbooks/kafka-troubleshooting.md)
- [Performance Tuning](docs/runbooks/performance-tuning.md)
- [Monitoring and Alerting](docs/runbooks/monitoring-setup.md)
- [Disaster Recovery](docs/runbooks/disaster-recovery.md)
- [Security Incident Response](docs/runbooks/security-incidents.md)

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is proprietary software owned by BioPro. All rights reserved.

## Support

For support and questions:
- Create an issue in the project repository
- Contact the BioPro development team
- Check the operational runbooks in the `docs/` directory