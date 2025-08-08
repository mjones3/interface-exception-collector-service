# Docker Configuration for Interface Exception Collector Service

This document describes the Docker containerization setup for the Interface Exception Collector Service, including multi-stage builds, security best practices, and local development configuration.

## Overview

The service uses a multi-stage Docker build process to create optimized, secure container images:

1. **Dependencies Stage**: Downloads and caches Maven dependencies
2. **Builder Stage**: Compiles the application
3. **Runtime Stage**: Creates the final minimal runtime image

## Docker Files

### Production Dockerfile
- **File**: `Dockerfile`
- **Purpose**: Production-ready image with security best practices
- **Features**:
  - Multi-stage build for smaller image size
  - Non-root user execution
  - Health checks
  - Proper signal handling
  - JVM optimization for containers

### Development Dockerfile
- **File**: `Dockerfile.dev`
- **Purpose**: Development image with hot reload capabilities
- **Features**:
  - Maven Spring Boot plugin for live reload
  - Development tools included
  - Source code mounting support

### Docker Ignore
- **File**: `.dockerignore`
- **Purpose**: Excludes unnecessary files from build context
- **Benefits**: Faster builds, smaller context, improved security

## Docker Compose Configuration

### Main Compose File
- **File**: `docker-compose.yml`
- **Services**:
  - `postgres`: PostgreSQL database with health checks
  - `redis`: Redis cache with persistence
  - `kafka`: Kafka broker with KRaft mode
  - `kafka-ui`: Web UI for Kafka management
  - `kafka-init`: Topic initialization
  - `exception-collector`: Main application service
  - `prometheus`: Metrics collection (optional)
  - `grafana`: Metrics visualization (optional)

### Development Override
- **File**: `docker-compose.override.yml`
- **Purpose**: Development-specific overrides
- **Features**:
  - Source code mounting for hot reload
  - Development Dockerfile usage
  - Enhanced logging
  - Live reload port exposure

## Usage

### Quick Start

```bash
# Start all services
docker-compose up

# Start in detached mode
docker-compose up -d

# Start with monitoring stack
docker-compose --profile monitoring up

# View logs
docker-compose logs -f exception-collector
```

### Using Helper Scripts

```bash
# Build production image
./scripts/docker-build.sh

# Build development image
./scripts/docker-build.sh --dev

# Start services with build
./scripts/docker-run.sh build

# Start with monitoring
./scripts/docker-run.sh up --monitoring

# Clean up everything
./scripts/docker-run.sh clean
```

### Development Workflow

1. **Start development environment**:
   ```bash
   docker-compose up
   ```

2. **Make code changes**: Changes are automatically detected and the application restarts

3. **View logs**:
   ```bash
   docker-compose logs -f exception-collector
   ```

4. **Access services**:
   - Application: http://localhost:8080
   - Kafka UI: http://localhost:8081
   - Prometheus: http://localhost:9090 (with monitoring profile)
   - Grafana: http://localhost:3000 (with monitoring profile)

## Security Features

### Container Security
- **Non-root user**: Application runs as user `appuser` (UID 1001)
- **Minimal base image**: Uses Alpine Linux for smaller attack surface
- **Read-only filesystem**: Application files are read-only
- **Resource limits**: CPU and memory constraints configured
- **Health checks**: Proper liveness and readiness probes

### Network Security
- **Custom network**: Services communicate on isolated network
- **Port exposure**: Only necessary ports exposed to host
- **Internal communication**: Services use internal hostnames

### Data Security
- **Volume permissions**: Proper ownership and permissions
- **Secret management**: Sensitive data via environment variables
- **TLS ready**: Configuration supports TLS for external connections

## Configuration

### Environment Variables

The application supports the following environment variables in Docker:

```yaml
# Database
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/exception_collector_db
SPRING_DATASOURCE_USERNAME: exception_user
SPRING_DATASOURCE_PASSWORD: exception_pass

# Kafka
SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
SPRING_KAFKA_CONSUMER_GROUP_ID: interface-exception-collector

# Redis
SPRING_REDIS_HOST: redis
SPRING_REDIS_PORT: 6379

# JVM
JAVA_OPTS: "-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
```

### Profiles

- **docker**: Base Docker configuration
- **dev**: Development-specific settings
- **monitoring**: Enables monitoring services

## Health Checks

### Application Health Check
```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
```

### Service Dependencies
All services include health checks and proper dependency ordering:
- Database: PostgreSQL ready check
- Cache: Redis ping check
- Messaging: Kafka broker availability
- Application: Spring Boot actuator health endpoint

## Monitoring

### Metrics Collection
- **Prometheus**: Scrapes metrics from `/actuator/prometheus`
- **Grafana**: Visualizes metrics with pre-configured dashboards
- **Application metrics**: Custom business metrics included

### Logging
- **Structured logging**: JSON format with correlation IDs
- **Log aggregation**: Centralized logging to `/app/logs`
- **Log rotation**: Automatic rotation with size limits

## Troubleshooting

### Common Issues

1. **Port conflicts**:
   ```bash
   # Check port usage
   docker-compose ps
   netstat -tulpn | grep :8080
   ```

2. **Database connection issues**:
   ```bash
   # Check database health
   docker-compose exec postgres pg_isready -U exception_user
   ```

3. **Kafka connectivity**:
   ```bash
   # Check Kafka topics
   docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092
   ```

4. **Memory issues**:
   ```bash
   # Check container resources
   docker stats exception-collector-service
   ```

### Debug Mode

Enable debug logging:
```bash
docker-compose up -e LOGGING_LEVEL_COM_ARCONE_BIOPRO=DEBUG
```

### Container Shell Access

```bash
# Access application container
docker-compose exec exception-collector sh

# Access database
docker-compose exec postgres psql -U exception_user -d exception_collector_db
```

## Production Considerations

### Resource Limits
Configure appropriate resource limits in production:
```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'
      memory: 2G
    reservations:
      cpus: '1.0'
      memory: 1G
```

### Secrets Management
Use Docker secrets or external secret management:
```yaml
secrets:
  db_password:
    external: true
```

### Backup Strategy
- Database: Regular PostgreSQL backups
- Configuration: Version-controlled configuration files
- Logs: Centralized log aggregation

### Scaling
- Horizontal scaling: Multiple application instances
- Database: Read replicas for query scaling
- Kafka: Multiple partitions for parallel processing

## Build Optimization

### Multi-stage Benefits
- **Smaller images**: Runtime image ~200MB vs ~800MB full build
- **Security**: No build tools in production image
- **Caching**: Efficient layer caching for faster builds

### Build Performance
- **Dependency caching**: Maven dependencies cached in separate layer
- **Parallel builds**: Multi-core build support
- **Build context**: Minimal context with `.dockerignore`

## Integration with CI/CD

### GitLab CI Example
```yaml
build:
  script:
    - docker build -t $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA .
    - docker push $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
```

### Kubernetes Deployment
The Docker images are designed to work seamlessly with Kubernetes:
- Health checks map to liveness/readiness probes
- Graceful shutdown handles SIGTERM properly
- Resource limits align with Kubernetes resource management