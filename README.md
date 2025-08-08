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

### Retry Operations

- `POST /api/v1/exceptions/{transactionId}/retry` - Initiate retry
- `GET /api/v1/exceptions/{transactionId}/retry-history` - Get retry history

### Management Operations

- `PUT /api/v1/exceptions/{transactionId}/acknowledge` - Acknowledge exception
- `PUT /api/v1/exceptions/{transactionId}/resolve` - Mark as resolved

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