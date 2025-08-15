# BioPro Interface Services

This repository contains multiple microservices for BioPro interface management:

## Services

### Interface Exception Collector Service
- **Port**: 8080 (HTTP), 5005 (Debug)
- **Purpose**: Centralized collection and management of interface exceptions
- **Location**: `./interface-exception-collector/`

### Partner Order Service  
- **Port**: 8090 (HTTP), 5006 (Debug)
- **Purpose**: External API for partners to submit blood product orders with retry capabilities
- **Location**: `./partner-order-service/`

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker and Docker Compose
- Kubernetes cluster (for Tilt development)
- Tilt (for development)

### Development with Tilt (Recommended)

1. **Start all services:**
   ```bash
   tilt up
   ```

2. **Start specific services:**
   ```bash
   # Interface Exception Collector only
   tilt up -- --services=interface-exception-collector
   
   # Partner Order Service only
   tilt up -- --services=partner-order-service
   
   # Both services (default)
   tilt up -- --services=interface-exception-collector,partner-order-service
   ```

3. **Access services:**
   - **Tilt UI**: http://localhost:10350
   - **Interface Exception Collector**: http://localhost:8080
   - **Partner Order Service**: http://localhost:8090
   - **Kafka UI**: http://localhost:8081

### Building

```bash
# Build all services
mvn clean package

# Build specific service
mvn clean package -pl interface-exception-collector
mvn clean package -pl partner-order-service

# Build with Docker
mvn clean package -Pdocker
```

### Testing

```bash
# Run all tests
mvn test

# Run tests for specific service
mvn test -pl interface-exception-collector
mvn test -pl partner-order-service

# Integration tests
mvn verify
```

## Service Integration

The Partner Order Service integrates with the Interface Exception Collector Service:

1. **Event Flow**: Partner Order Service publishes `OrderRejected` and `InvalidOrderEvent` events
2. **Exception Collection**: Interface Exception Collector consumes these events and stores exceptions
3. **Retry Capability**: Interface Exception Collector can retrieve original payloads and retry failed orders

### Integration Endpoints

**Partner Order Service provides:**
- `POST /v1/partner-order-provider/orders` - Submit orders (used for retries)
- `GET /v1/partner-order-provider/orders/{transactionId}/payload` - Retrieve original payloads

**Interface Exception Collector provides:**
- `POST /api/v1/exceptions/{transactionId}/retry` - Initiate retry operations

## Configuration

### Environment Variables

**Shared:**
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka broker addresses
- `SPRING_PROFILES_ACTIVE` - Active Spring profiles

**Interface Exception Collector:**
- `DATABASE_URL` - PostgreSQL connection (port 5432)
- `REDIS_HOST` - Redis cache host

**Partner Order Service:**
- `PARTNER_ORDER_DATABASE_URL` - PostgreSQL connection (port 5433)

### Profiles

- **local**: Local development with Tilt/Kubernetes
- **dev**: Development environment
- **staging**: Staging environment
- **prod**: Production environment

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    External Partner Systems                     │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Partner Order Service                          │
│                     (Port 8090)                                │
├─────────────────┬─────────────────┬─────────────────────────────┤
│   Validation    │  Event Publish  │    Payload Storage          │
└─────────┬───────┴─────────┬───────┴─────────────┬───────────────┘
          │                 │                     │
          ▼                 ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Kafka Event Bus                           │
├─────────────────┬─────────────────┬─────────────────────────────┤
│OrderReceived    │OrderRejected    │  InvalidOrderEvent          │
└─────────┬───────┴─────────┬───────┴─────────────┬───────────────┘
          │                 │                     │
          ▼                 ▼                     ▼
┌─────────────────────────────────────────────────────────────────┐
│              Interface Exception Collector                      │
│                     (Port 8080)                                │
├─────────────────┬─────────────────┬─────────────────────────────┤
│ Exception Store │  Retry Logic    │    Management APIs          │
└─────────────────┴─────────────────┴─────────────────────────────┘
```

## Monitoring

### Health Checks
- **Interface Exception Collector**: http://localhost:8080/actuator/health
- **Partner Order Service**: http://localhost:8090/actuator/health

### Metrics
- **Interface Exception Collector**: http://localhost:8080/actuator/prometheus
- **Partner Order Service**: http://localhost:8090/actuator/prometheus

### API Documentation
- **Interface Exception Collector**: http://localhost:8080/swagger-ui.html
- **Partner Order Service**: http://localhost:8090/swagger-ui.html

## Development

### Adding New Services

1. Create new module directory: `mkdir new-service`
2. Add module to parent `pom.xml`: `<module>new-service</module>`
3. Create service `pom.xml` with parent reference
4. Update `Tiltfile` to include new service
5. Create Kubernetes configurations in `k8s/`

### Shared Schemas

Event schemas are stored in `./shared-schemas/` and can be referenced by both services for consistency.

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 8080, 8090, 5005, 5006 are available
2. **Database connections**: Check PostgreSQL containers are running (ports 5432, 5433)
3. **Kafka connectivity**: Verify Kafka is accessible on port 9092
4. **Maven dependencies**: Run `mvn dependency:resolve` if builds fail

### Logs

```bash
# Tilt logs
tilt logs interface-exception-collector
tilt logs partner-order-service

# Kubernetes logs
kubectl logs -l app=interface-exception-collector
kubectl logs -l app=partner-order-service
```