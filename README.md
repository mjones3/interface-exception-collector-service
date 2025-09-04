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
- **Mock RSocket Server Integration**: Development and testing support with containerized mock server for order data retrieval

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

### Development with Tilt (Includes Mock RSocket Server)

For advanced development with hot reload and mock server integration:

```bash
./scripts/deploy-local.sh
```

This automatically deploys:
- Interface Exception Collector service
- Mock RSocket Server for order data retrieval
- All required infrastructure (PostgreSQL, Kafka, Redis)

### Access Points

- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/actuator/health
- **Mock RSocket Server**: rsocket://localhost:7000
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

The service provides both REST and GraphQL APIs for comprehensive exception management.

### GraphQL API

For modern, flexible API access with real-time subscriptions:

📖 **[GraphQL Developer Guide](docs/GRAPHQL_DEVELOPER_GUIDE.md)** - Complete guide with all queries, mutations, and subscriptions

- **GraphQL Endpoint**: `POST /graphql`
- **GraphiQL Interface**: `GET /graphiql` (development only)
- **WebSocket Subscriptions**: `wss://your-domain.com/subscriptions`
- **Schema Documentation**: `GET /graphql/schema` (development only)

### REST API

### Interface Exception Collector Service (Port 8080)

#### Exception Management

- `POST /api/v1/exceptions` - Create and publish exception event to Kafka
- `GET /api/v1/exceptions` - List exceptions with filtering
- `GET /api/v1/exceptions/{transactionId}` - Get exception details
- `GET /api/v1/exceptions/search` - Full-text search
- `GET /api/v1/exceptions/summary` - Aggregated statistics

### Partner Order Service (Port 8090)

#### Order Management

- `POST /v1/partner-order-provider/orders` - Submit partner orders (no authentication required)
- `GET /v1/partner-order-provider/orders/{transactionId}/payload` - Retrieve original payloads for retry operations

#### Example API Calls

**Create Exception Event:**
```bash
curl -X POST \
  -H @auth_header.txt \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "ORDER-123456",
    "operation": "CREATE_ORDER",
    "rejectedReason": "Insufficient inventory for blood type O-negative",
    "customerId": "CUST-MOUNT-SINAI-001",
    "locationCode": "HOSP-NYC-001",
    "orderItems": [
      {
        "bloodType": "O-",
        "productFamily": "RED_BLOOD_CELLS",
        "quantity": 2
      }
    ]
  }' \
  "http://localhost:8080/api/v1/exceptions"
```

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

#### Create Exception Event

**Endpoint:** `POST /api/v1/exceptions`

**Description:** Creates and publishes an OrderRejected event to Kafka for testing the end-to-end exception processing flow. This endpoint is primarily used for testing and development purposes.

**Request Payload:**
```json
{
  "externalId": "ORDER-123456",
  "operation": "CREATE_ORDER",
  "rejectedReason": "Insufficient inventory for blood type O-negative",
  "customerId": "CUST-MOUNT-SINAI-001",
  "locationCode": "HOSP-NYC-001",
  "orderItems": [
    {
      "bloodType": "O-",
      "productFamily": "RED_BLOOD_CELLS",
      "quantity": 2
    }
  ]
}
```

**Request Fields:**
- `externalId` (required): String - External identifier from the source system
- `operation` (required): Enum - Operation being performed (`CREATE_ORDER`, `MODIFY_ORDER`, `CANCEL_ORDER`)
- `rejectedReason` (required): String - Reason for the rejection/exception
- `customerId` (optional): String - Customer identifier
- `locationCode` (optional): String - Location code where the exception occurred
- `orderItems` (optional): Array - List of order items involved in the exception
  - `bloodType` (required): String - Blood type (e.g., "O-", "A+")
  - `productFamily` (required): String - Product family (e.g., "RED_BLOOD_CELLS")
  - `quantity` (required): Integer - Quantity requested (minimum 1)

**Example Response (Success):**
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "eventId": "123e4567-e89b-12d3-a456-426614174000",
  "correlationId": "corr-123e4567-e89b-12d3-a456-426614174000",
  "publishedAt": "2025-08-13T10:30:00.000Z",
  "topic": "OrderRejected",
  "status": "SUCCESS",
  "message": "Exception event published successfully to Kafka"
}
```

**Example Response (Error):**
```json
{
  "status": "FAILED",
  "message": "Failed to publish event to Kafka: Connection timeout",
  "publishedAt": "2025-08-13T10:30:00.000Z"
}
```

**Response Codes:**
- `201 Created` - Exception event created and published successfully
- `400 Bad Request` - Invalid request payload or validation errors
- `500 Internal Server Error` - Failed to publish event to Kafka

**Example cURL Command:**
```bash
curl -X POST \
  -H @auth_header.txt \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "ORDER-123456",
    "operation": "CREATE_ORDER",
    "rejectedReason": "Insufficient inventory for blood type O-negative",
    "customerId": "CUST-MOUNT-SINAI-001",
    "locationCode": "HOSP-NYC-001",
    "orderItems": [
      {
        "bloodType": "O-",
        "productFamily": "RED_BLOOD_CELLS",
        "quantity": 2
      }
    ]
  }' \
  "http://localhost:8080/api/v1/exceptions"
```

**Notes:**
- This endpoint publishes an `OrderRejected` event to the Kafka topic
- The event will be consumed by the application's Kafka consumers and processed as a real exception
- Generated transaction ID can be used to track the exception through the system
- Useful for testing the complete exception processing workflow

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

**Generate JWT Tokens for Testing:**

```bash
# Generate VIEWER token (read-only access)
node generate-jwt-correct-secret.js "viewer-user" "VIEWER"

# Generate OPERATOR token (can perform retries and management)
node generate-jwt-correct-secret.js "operator-user" "OPERATOR"

# Generate ADMIN token (full access to all endpoints)
node generate-jwt-correct-secret.js "admin-user" "ADMIN"
```

**Example Token Generation:**
```bash
# Create an admin token
$ node generate-jwt-correct-secret.js "test-user" "ADMIN"
🔑 JWT Token Generated
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
👤 Username: test-user
🛡️  Roles: ADMIN
⏰ Expires: 8/20/2025, 1:30:00 PM
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📋 For Bruno Authorization Header:
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

🔗 Raw Token:
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Role Requirements:**
- `VIEWER` - Can access read-only endpoints (GET operations)
- `OPERATOR` - Can perform retry operations and management actions  
- `ADMIN` - Full access to all endpoints including management operations

**Using Tokens with curl:**
```bash
# Save token to file to avoid command-line parsing issues
echo "Authorization: Bearer <your-jwt-token>" > auth_header.txt

# Use with Interface Exception Collector (port 8080)
curl -H @auth_header.txt "http://localhost:8080/api/v1/exceptions"

# Use with Partner Order Service (port 8090) - no auth required for order submission
curl -X POST -H "Content-Type: application/json" \
  -d '{"externalId":"ORDER-123","orderStatus":"OPEN",...}' \
  "http://localhost:8090/v1/partner-order-provider/orders"
```

**Token Examples:**
```bash
# VIEWER Token (expires in 1 hour)
VIEWER_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ2aWV3ZXItdXNlciIsInJvbGVzIjpbIlZJRVdFUiJdLCJpYXQiOjE3NTUyMTM5OTksImV4cCI6MTc1NTIxNzU5OX0.XMDw0HPMgxalYxMGIwpUOZThhlZjdc8Su6ifExytx9s"

# OPERATOR Token (expires in 1 hour)  
OPERATOR_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJvcGVyYXRvci11c2VyIiwicm9sZXMiOlsiT1BFUkFUT1IiXSwiaWF0IjoxNzU1MjE0Mjg3LCJleHAiOjE3NTUyMTc4ODd9.cmhcFpxjZZG1uCD1do3SEoc-HAUNhum_FAI69IUytPc"

# ADMIN Token (expires in 1 hour)
ADMIN_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbi11c2VyIiwicm9sZXMiOlsiQURNSU4iXSwiaWF0IjoxNzU1MjE0MDE4LCJleHAiOjE3NTUyMTc2MTh9.zl0Fv3C7zGQ69wCQBPIjmgBsuX6JoKaDwYsX_-rUvmg"

# Use tokens in requests
curl -H "Authorization: Bearer $VIEWER_TOKEN" "http://localhost:8080/api/v1/exceptions"
curl -H "Authorization: Bearer $OPERATOR_TOKEN" -X POST "http://localhost:8080/api/v1/exceptions/123/retry" -d '{"reason":"test"}'
curl -H "Authorization: Bearer $ADMIN_TOKEN" "http://localhost:8080/actuator/metrics"
```

### Health and Monitoring

- `GET /actuator/health` - Application health status
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/prometheus` - Prometheus metrics

## Deployment

### Kubernetes Development with Tilt (Recommended for Development)

Tilt provides the best development experience with hot reloading, automatic rebuilds, and comprehensive monitoring. This is the recommended approach for local development.

#### Prerequisites

- **Kubernetes cluster** (Docker Desktop, minikube, kind, or remote cluster)
- **kubectl** configured to access your cluster
- **Tilt** installed ([Installation Guide](https://docs.tilt.dev/install.html))
- **Docker** for building images
- **Java 21+** and **Maven 3.8+**

#### Installation

**macOS (Homebrew):**
```bash
brew install tilt-dev/tap/tilt
```

**Other platforms:**
```bash
curl -fsSL https://raw.githubusercontent.com/tilt-dev/tilt/master/scripts/install.sh | bash
```

#### Quick Start with Tilt

1. **Ensure Kubernetes is running:**
   ```bash
   kubectl cluster-info
   ```

2. **Start Tilt development environment:**
   ```bash
   tilt up
   ```

3. **Access the Tilt UI:**
   - Automatically opens at http://localhost:10350
   - Monitor all services, logs, and build status

4. **Access your services:**
   - **Application**: http://localhost:8080
   - **Kafka UI**: http://localhost:8081
   - **API Documentation**: http://localhost:8080/swagger-ui.html
   - **Health Check**: http://localhost:8080/actuator/health
   - **Metrics**: http://localhost:8080/actuator/prometheus
   - **Debug Port**: localhost:5005 (for remote debugging)

#### Tilt Development Workflow

**What Tilt Does Automatically:**

1. **Infrastructure Setup:**
   - Deploys PostgreSQL with persistent storage
   - Deploys Redis for caching
   - Deploys Kafka in KRaft mode
   - Deploys Kafka UI for monitoring
   - Sets up all necessary port forwarding

2. **Database Management:**
   - Runs database migrations automatically
   - Creates all required tables and indexes
   - Sets up proper database connections

3. **Kafka Management:**
   - Creates all required Kafka topics
   - Sets up proper partitioning and replication
   - Configures consumer groups

4. **Application Deployment:**
   - Builds the application with Maven
   - Creates Docker image with development configuration
   - Deploys to Kubernetes with hot reload enabled
   - Sets up health checks and resource limits

**Hot Reload Development:**

1. **Make code changes** in your IDE (any Java file in `src/main/java/`)
2. **Save the file** - Tilt automatically detects changes
3. **Watch automatic rebuild** in Tilt UI:
   - Maven compiles your changes
   - Docker image is updated with new classes
   - Kubernetes pod is updated with live sync
   - Application restarts with new code
4. **Test immediately** - Changes are live in seconds

**Tilt Commands:**

```bash
# Start development environment
tilt up

# Start in background (no UI auto-open)
tilt up --stream

# View logs for specific service
tilt logs interface-exception-collector

# Trigger manual build
tilt trigger maven-compile

# Run tests manually
tilt trigger run-tests

# Clean up Kafka topics for testing
tilt trigger cleanup-kafka-topics

# Restart specific service
tilt restart interface-exception-collector

# Stop all services
tilt down

# Check status
tilt status
```

**Tilt UI Features:**

- **Resource Overview**: See all services and their status
- **Live Logs**: Real-time logs from all services with filtering
- **Build History**: Track all builds and their duration
- **Port Forwards**: Quick access to all exposed services
- **Resource Dependencies**: Visual dependency graph
- **Manual Triggers**: Run tests, builds, or cleanup tasks

#### Customizing Tilt Configuration

**Environment Variables:**
```bash
# Use different namespace
tilt up -- --namespace=my-dev-env

# Skip tests on startup
tilt up -- --skip-tests=true
```

**Tiltfile Customization:**
The `Tiltfile` can be modified to suit your development needs:

```python
# Add custom local resources
local_resource(
    'custom-setup',
    'echo "Running custom setup"',
    auto_init=True
)

# Modify resource dependencies
k8s_resource(
    'interface-exception-collector',
    resource_deps=['postgres', 'kafka', 'custom-setup']
)
```

#### Troubleshooting Tilt

**Common Issues:**

1. **Tilt won't start:**
   ```bash
   # Check Kubernetes connection
   kubectl cluster-info
   
   # Check Tilt version
   tilt version
   
   # Clean up and restart
   tilt down
   tilt up
   ```

2. **Build failures:**
   ```bash
   # Check Maven dependencies
   mvn dependency:resolve
   
   # Clean Maven cache
   mvn clean
   
   # Trigger manual build
   tilt trigger maven-compile
   ```

3. **Port forwarding issues:**
   ```bash
   # Check port availability
   lsof -i :8080
   
   # Restart port forwards
   tilt restart interface-exception-collector
   ```

4. **Kafka connection issues:**
   ```bash
   # Check Kafka logs
   tilt logs kafka
   
   # Restart Kafka
   tilt restart kafka
   
   # Recreate topics
   tilt trigger cleanup-kafka-topics
   ```

**Performance Optimization:**

```bash
# Increase resource limits in k8s/app.yaml
resources:
  limits:
    memory: "2Gi"
    cpu: "1000m"
  requests:
    memory: "1Gi"
    cpu: "500m"
```

#### Advanced Tilt Features

**Live Update Configuration:**
The Tiltfile is configured for optimal live updates:

```python
docker_build_with_restart(
    'interface-exception-collector',
    '.',
    dockerfile='Dockerfile.dev',
    entrypoint=['sh', '-c', 'mvn spring-boot:run -Dspring-boot.run.profiles=dev'],
    only=['./target/classes', './target/lib'],
    live_update=[
        sync('./target/classes', '/app/classes'),
    ]
)
```

**Resource Dependencies:**
Services start in the correct order:
```
postgres → migration-job
kafka → kafka-topics-job
redis
↓
interface-exception-collector
```

**Manual Triggers:**
- `run-tests`: Execute Maven test suite
- `maven-package`: Create JAR package
- `cleanup-kafka-topics`: Reset Kafka topics for testing

### Kubernetes with Helm (Production)

For production deployments, use Helm charts with environment-specific values.

#### Prerequisites

- **Helm 3.x** installed
- **Kubernetes cluster** with appropriate RBAC
- **Container registry** access
- **Database** (managed PostgreSQL recommended)
- **Kafka cluster** (managed Kafka recommended)

#### Deployment Commands

1. **Deploy to development:**
   ```bash
   helm install interface-exception-collector ./helm \
     --namespace exception-collector-dev \
     --create-namespace \
     --values helm/values-dev.yaml
   ```

2. **Deploy to staging:**
   ```bash
   helm install interface-exception-collector ./helm \
     --namespace exception-collector-staging \
     --create-namespace \
     --values helm/values-staging.yaml
   ```

3. **Deploy to production:**
   ```bash
   helm install interface-exception-collector ./helm \
     --namespace exception-collector-prod \
     --create-namespace \
     --values helm/values-prod.yaml
   ```

#### Helm Configuration

**Environment-specific values files:**

- `helm/values-dev.yaml` - Development environment
- `helm/values-staging.yaml` - Staging environment  
- `helm/values-prod.yaml` - Production environment

**Key configuration options:**

```yaml
# Application configuration
app:
  profile: staging
  logLevel: INFO
  
# Scaling configuration
replicaCount: 2
autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10

# Database configuration
postgresql:
  enabled: true
  auth:
    database: "exception_collector_staging"
    username: "staging_user"
    
# Ingress configuration
ingress:
  enabled: true
  className: "nginx"
  hosts:
    - host: exception-collector-staging.company.com
      paths:
        - path: /
          pathType: Prefix
```

#### Helm Management Commands

```bash
# Check deployment status
helm status interface-exception-collector -n exception-collector-staging

# Upgrade deployment
helm upgrade interface-exception-collector ./helm \
  --namespace exception-collector-staging \
  --values helm/values-staging.yaml

# Rollback deployment
helm rollback interface-exception-collector 1 -n exception-collector-staging

# Uninstall deployment
helm uninstall interface-exception-collector -n exception-collector-staging
```

### Manual Kubernetes Deployment

For environments where Helm is not available, use raw Kubernetes manifests.

#### Prerequisites

- **kubectl** configured with cluster access
- **Container registry** with application image
- **External database** and Kafka cluster configured

#### Deployment Steps

1. **Create namespace:**
   ```bash
   kubectl create namespace interface-exception-collector
   ```

2. **Deploy infrastructure components:**
   ```bash
   # PostgreSQL (for development only)
   kubectl apply -f k8s/postgres.yaml -n interface-exception-collector
   
   # Redis
   kubectl apply -f k8s/redis.yaml -n interface-exception-collector
   
   # Kafka (for development only)
   kubectl apply -f k8s/kafka-simple.yaml -n interface-exception-collector
   
   # Kafka UI
   kubectl apply -f k8s/kafka-ui.yaml -n interface-exception-collector
   ```

3. **Run database migrations:**
   ```bash
   kubectl apply -f k8s/migration-job.yaml -n interface-exception-collector
   
   # Wait for completion
   kubectl wait --for=condition=complete job/migration-job -n interface-exception-collector --timeout=300s
   ```

4. **Create Kafka topics:**
   ```bash
   kubectl apply -f k8s/kafka-topics-job.yaml -n interface-exception-collector
   
   # Wait for completion
   kubectl wait --for=condition=complete job/kafka-topics-job -n interface-exception-collector --timeout=300s
   ```

5. **Deploy application:**
   ```bash
   kubectl apply -f k8s/app.yaml -n interface-exception-collector
   ```

6. **Verify deployment:**
   ```bash
   # Check pod status
   kubectl get pods -n interface-exception-collector
   
   # Check services
   kubectl get services -n interface-exception-collector
   
   # Check application logs
   kubectl logs -l app=interface-exception-collector -n interface-exception-collector
   
   # Test health endpoint
   kubectl port-forward service/interface-exception-collector 8080:8080 -n interface-exception-collector
   curl http://localhost:8080/actuator/health
   ```

#### Production Considerations

**Security:**
- Use Kubernetes secrets for sensitive configuration
- Enable RBAC and network policies
- Use non-root containers
- Enable TLS for all communications

**High Availability:**
- Run multiple replicas across different nodes
- Use pod disruption budgets
- Configure proper resource limits and requests
- Use managed databases and Kafka clusters

**Monitoring:**
- Deploy Prometheus and Grafana
- Configure alerting rules
- Set up log aggregation
- Monitor resource usage and performance

**Backup and Recovery:**
- Regular database backups
- Persistent volume snapshots
- Disaster recovery procedures
- Configuration backup

#### Kubernetes Troubleshooting

**Common Issues:**

1. **Pod startup failures:**
   ```bash
   # Check pod events
   kubectl describe pod <pod-name> -n interface-exception-collector
   
   # Check logs
   kubectl logs <pod-name> -n interface-exception-collector --previous
   
   # Check resource constraints
   kubectl top pods -n interface-exception-collector
   ```

2. **Database connection issues:**
   ```bash
   # Test database connectivity
   kubectl exec -it deployment/postgres -n interface-exception-collector -- pg_isready
   
   # Check database logs
   kubectl logs deployment/postgres -n interface-exception-collector
   
   # Verify configuration
   kubectl get configmap app-config -o yaml -n interface-exception-collector
   ```

3. **Kafka connectivity issues:**
   ```bash
   # Check Kafka status
   kubectl logs deployment/kafka -n interface-exception-collector
   
   # Test topic creation
   kubectl exec -it deployment/kafka -n interface-exception-collector -- \
     kafka-topics --bootstrap-server localhost:9092 --list
   
   # Check consumer groups
   kubectl exec -it deployment/kafka -n interface-exception-collector -- \
     kafka-consumer-groups --bootstrap-server localhost:9092 --list
   ```

4. **Service discovery issues:**
   ```bash
   # Check service endpoints
   kubectl get endpoints -n interface-exception-collector
   
   # Test service connectivity
   kubectl exec -it <pod-name> -n interface-exception-collector -- \
     curl http://postgres:5432
   
   # Check DNS resolution
   kubectl exec -it <pod-name> -n interface-exception-collector -- \
     nslookup postgres
   ```

**Performance Monitoring:**

```bash
# Check resource usage
kubectl top pods -n interface-exception-collector
kubectl top nodes

# Check application metrics
kubectl port-forward service/interface-exception-collector 8080:8080 -n interface-exception-collector
curl http://localhost:8080/actuator/metrics

# Check JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/jvm.gc.pause
```

### Container Registry and Image Management

#### Building and Pushing Images

```bash
# Build production image
docker build -t interface-exception-collector:latest .

# Tag for registry
docker tag interface-exception-collector:latest \
  your-registry.com/interface-exception-collector:v1.0.0

# Push to registry
docker push your-registry.com/interface-exception-collector:v1.0.0
```

#### Image Security Scanning

```bash
# Scan with Docker Scout
docker scout cves interface-exception-collector:latest

# Scan with Trivy
trivy image interface-exception-collector:latest
```

#### Multi-architecture Builds

```bash
# Build for multiple architectures
docker buildx build --platform linux/amd64,linux/arm64 \
  -t your-registry.com/interface-exception-collector:v1.0.0 \
  --push .
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

## Mock RSocket Server Integration

The Interface Exception Collector includes integration with a containerized mock RSocket server for development and testing. This enables order data retrieval during OrderRejected event processing without dependencies on external services.

### Overview

When processing OrderRejected events, the service can retrieve complete order data from the mock server using the `externalId` from the event payload. This order data is stored in the database and used for retry operations.

### Quick Start with Mock Server

1. **Start with Tilt (Recommended)**:
   ```bash
   tilt up
   ```
   This automatically deploys the mock server alongside the application.

2. **Test Order Data Retrieval**:
   ```bash
   # Create an OrderRejected event that will trigger order data retrieval
   curl -X POST \
     -H @auth_header.txt \
     -H "Content-Type: application/json" \
     -d '{
       "externalId": "TEST-ORDER-1",
       "operation": "CREATE_ORDER",
       "rejectedReason": "Insufficient inventory",
       "customerId": "CUST001"
     }' \
     "http://localhost:8080/api/v1/exceptions"
   ```

3. **Verify Order Data Storage**:
   ```bash
   # Check that the exception includes order data
   curl -H @auth_header.txt "http://localhost:8080/api/v1/exceptions/{transactionId}"
   ```

### Configuration

#### Enable Mock Server (Development)
```yaml
app:
  rsocket:
    mock-server:
      enabled: true
      host: localhost
      port: 7000
      timeout: 5s
```

#### Disable Mock Server (Production)
```yaml
app:
  rsocket:
    mock-server:
      enabled: false
    partner-order-service:
      enabled: true
      host: partner-order-service
      port: 8090
```

### Test Scenarios

The mock server supports various test scenarios:

#### Successful Order Retrieval
- `TEST-ORDER-1` - Basic order with 2 items
- `TEST-ORD-2025-018` - Bulk order with multiple items
- Any pattern matching `[A-Z0-9-]+` - Returns generic complete order

#### Error Scenarios
- `NOTFOUND-123` - Returns 404 not found
- `INVALID-ABC` - Returns 400 validation error
- `ERROR-500-XYZ` - Returns 500 server error

### Monitoring

Monitor mock server integration through:
- **Health Check**: `/actuator/health/rsocket`
- **Metrics**: `/actuator/metrics` (search for `rsocket.*`)
- **Logs**: Enable debug logging for detailed interaction logs

### Documentation

For detailed configuration and troubleshooting:
- 📖 **[RSocket Configuration Guide](interface-exception-collector/RSOCKET_CONFIGURATION_GUIDE.md)**
- 📖 **[Configuration Management](interface-exception-collector/CONFIGURATION_MANAGEMENT.md)**
- 📖 **[Mapping Files Guide](mappings/README.md)**
- 📖 **[Response Files Guide](mock-responses/README.md)**

## Troubleshooting

### Mock RSocket Server Issues

1. **Mock server connection failures:**
   ```bash
   # Check mock server logs
   tilt logs mock-rsocket-server
   
   # Restart mock server
   tilt restart mock-rsocket-server
   
   # Check RSocket connection health
   curl -H @auth_header.txt "http://localhost:8080/actuator/health/rsocket"
   ```

2. **Order data not retrieved:**
   ```bash
   # Check if mock server is enabled
   curl -H @auth_header.txt "http://localhost:8080/actuator/configprops" | grep rsocket
   
   # Check circuit breaker status
   curl -H @auth_header.txt "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state"
   
   # Enable debug logging
   kubectl patch deployment interface-exception-collector -p '{"spec":{"template":{"spec":{"containers":[{"name":"interface-exception-collector","env":[{"name":"LOGGING_LEVEL_COM_ARCONE_BIOPRO_EXCEPTION_COLLECTOR_INFRASTRUCTURE_CLIENT","value":"DEBUG"}]}]}}}}'
   ```

3. **Mapping file issues:**
   ```bash
   # Check mapping files are loaded
   kubectl exec -it mock-rsocket-server-xxx -- ls -la /app/mappings/
   
   # Reload mappings
   tilt trigger reload-mock-mappings
   
   # Test specific mapping
   curl -X POST rsocket://localhost:7000/orders.TEST-ORDER-1
   ```

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

## Documentation

This project includes comprehensive documentation to help developers, operators, and stakeholders understand and work with the Interface Exception Collector Service.

### 📋 Project Documentation

#### Core Documentation
- **[Requirements](requirements.md)** - Complete user stories and acceptance criteria for all features
- **[Implementation Summary](IMPLEMENTATION_SUMMARY.md)** - Detailed summary of external service integration implementation
- **[Contributing Guidelines](CONTRIBUTING.md)** - Code of conduct and pull request process

#### Architecture & Design
- **[Spec Requirements](.kiro/specs/interface-exception-collector/requirements.md)** - Formal requirements specification
- **[Spec Design](.kiro/specs/interface-exception-collector/design.md)** - Detailed system design and architecture
- **[Spec Tasks](.kiro/specs/interface-exception-collector/tasks.md)** - Implementation task breakdown

### 🚀 Deployment & Operations

#### Deployment Guides
- **[Deployment Guide](docs/DEPLOYMENT_GUIDE.md)** - Comprehensive deployment procedures for all environments
- **[Docker Configuration](DOCKER.md)** - Docker containerization, multi-stage builds, and security practices
- **[Tilt Development](TILT_DEVELOPMENT.md)** - Local development with Tilt and hot reloading
- **[Tilt Implementation Summary](TILT_IMPLEMENTATION_SUMMARY.md)** - Detailed Tilt setup and configuration

#### Configuration & Security
- **[Configuration Guide](docs/CONFIGURATION.md)** - Complete configuration reference with all properties
- **[API Authentication Guide](API_AUTHENTICATION_GUIDE.md)** - JWT authentication, roles, and API usage examples
- **[Security Implementation](SECURITY_IMPLEMENTATION.md)** - Security features, TLS, rate limiting, and audit logging

#### Data Management
- **[Database Schema](docs/database-schema.md)** - Database design, tables, indexes, and relationships
- **[Data Management](docs/DATA_MANAGEMENT.md)** - Data lifecycle, archiving, and backup procedures

### 📊 Monitoring & Operations

#### Implementation Summaries
- **[Monitoring Implementation](MONITORING_IMPLEMENTATION_SUMMARY.md)** - Metrics, health checks, and observability features

#### Operational Runbooks
- **[Service Lifecycle](docs/runbooks/service-lifecycle.md)** - Service startup, shutdown, and lifecycle management
- **[Database Troubleshooting](docs/runbooks/database-troubleshooting.md)** - Database connection issues and performance tuning
- **[Kafka Troubleshooting](docs/runbooks/kafka-troubleshooting.md)** - Kafka consumer problems and topic management
- **[Performance Tuning](docs/runbooks/performance-tuning.md)** - Application performance optimization
- **[Monitoring Setup](docs/runbooks/monitoring-setup.md)** - Prometheus, Grafana, and alerting configuration
- **[Disaster Recovery](docs/runbooks/disaster-recovery.md)** - Backup, restore, and disaster recovery procedures
- **[Security Incidents](docs/runbooks/security-incidents.md)** - Security incident response procedures

### 🔧 Development & Testing

#### Development Resources
- **[API Documentation](docs/API_DOCUMENTATION.md)** - Complete REST API reference with examples
- **[Test Documentation](src/test/README.md)** - Testing strategy, test structure, and running tests
- **[Scripts Documentation](scripts/README.md)** - Available automation scripts and their usage
- **[Helm Chart Documentation](helm/README.md)** - Kubernetes deployment with Helm

### 📚 Quick Reference

#### For New Developers
1. Start with **[Requirements](requirements.md)** to understand what the service does
2. Review **[Spec Design](.kiro/specs/interface-exception-collector/design.md)** for architecture overview
3. Follow **[Deployment Guide](docs/DEPLOYMENT_GUIDE.md)** for local setup
4. Use **[Tilt Development](TILT_DEVELOPMENT.md)** for hot-reload development
5. Reference **[API Authentication Guide](API_AUTHENTICATION_GUIDE.md)** for testing APIs

#### For Operations Teams
1. **[Deployment Guide](docs/DEPLOYMENT_GUIDE.md)** - Production deployment procedures
2. **[Configuration Guide](docs/CONFIGURATION.md)** - Environment-specific configuration
3. **[Monitoring Setup](docs/runbooks/monitoring-setup.md)** - Observability and alerting
4. **[Troubleshooting Runbooks](docs/runbooks/)** - Issue resolution procedures
5. **[Security Implementation](SECURITY_IMPLEMENTATION.md)** - Security features and best practices

#### For DevOps Engineers
1. **[Docker Configuration](DOCKER.md)** - Container builds and security
2. **[Kubernetes Deployment](#kubernetes-development-with-tilt-recommended-for-development)** - K8s deployment strategies
3. **[Helm Chart Documentation](helm/README.md)** - Helm-based deployments
4. **[Infrastructure Scripts](scripts/README.md)** - Automation and deployment scripts
5. **[Disaster Recovery](docs/runbooks/disaster-recovery.md)** - Backup and recovery procedures

#### For API Consumers
1. **[API Documentation](docs/API_DOCUMENTATION.md)** - Complete API reference
2. **[API Authentication Guide](API_AUTHENTICATION_GUIDE.md)** - Authentication and authorization
3. **[Requirements](requirements.md)** - Understanding API behavior and use cases

### 📖 Documentation Standards

All documentation follows these standards:
- **Markdown format** for consistency and version control
- **Clear headings** and table of contents for navigation
- **Code examples** with proper syntax highlighting
- **Step-by-step procedures** for operational tasks
- **Troubleshooting sections** with common issues and solutions
- **Regular updates** to maintain accuracy with code changes

### 🔄 Documentation Updates

Documentation is maintained alongside code changes:
- **Requirements changes** trigger updates to specs and API docs
- **Configuration changes** are reflected in configuration guides
- **New features** include updated API documentation and examples
- **Operational procedures** are validated and updated during deployments

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is proprietary software owned by BioPro. All rights reserved.

## Support

For support and questions:
- Create an issue in the project repository
- Contact the BioPro development team
- Check the operational runbooks in the `docs/runbooks/` directory
- Review the troubleshooting sections in relevant documentation