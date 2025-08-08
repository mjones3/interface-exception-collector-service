# Deployment Guide

## Overview

This guide covers deployment procedures for the Interface Exception Collector Service across different environments (development, staging, and production).

## Quick Start

### Automated Setup (Recommended for New Developers)

Run the getting started script to automatically set up your local environment:

```bash
./scripts/getting-started.sh
```

This script will:
1. Check prerequisites
2. Start infrastructure services
3. Run database migrations
4. Create Kafka topics
5. Build the application
6. Run smoke tests

### Manual Setup

To get the service running locally for development:

```bash
# 1. Start infrastructure services
docker-compose up -d postgres kafka redis

# 2. Wait for services to be ready
./scripts/validate-infrastructure.sh

# 3. Run database migrations
./scripts/run-migrations.sh --local

# 4. Start the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Using Tilt (Advanced Development)

For a complete development environment with hot reload:

```bash
# Start Tilt development environment
./scripts/deploy-local.sh

# Access Tilt UI at http://localhost:10350
# Application will be available at http://localhost:8080
```

## Table of Contents

- [Prerequisites](#prerequisites)
- [Environment Configuration](#environment-configuration)
- [Local Development Deployment](#local-development-deployment)
- [Development Environment Deployment](#development-environment-deployment)
- [Staging Environment Deployment](#staging-environment-deployment)
- [Production Environment Deployment](#production-environment-deployment)
- [Rollback Procedures](#rollback-procedures)
- [Post-Deployment Verification](#post-deployment-verification)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Tools

- **Docker**: Version 20.10+
- **Kubernetes CLI (kubectl)**: Version 1.25+
- **Helm**: Version 3.8+
- **Maven**: Version 3.8+
- **Java**: Version 21+

### Access Requirements

- Kubernetes cluster access with appropriate RBAC permissions
- Container registry access (Docker Hub, ECR, etc.)
- Database access credentials
- Kafka cluster access
- Redis cluster access

### Pre-deployment Checklist

- [ ] Code changes reviewed and approved
- [ ] Unit and integration tests passing
- [ ] Security scan completed
- [ ] Database migrations tested
- [ ] Configuration validated
- [ ] Monitoring and alerting configured
- [ ] Rollback plan prepared

## Environment Configuration

### Environment Variables

| Variable | Development | Staging | Production | Description |
|----------|-------------|---------|------------|-------------|
| `SPRING_PROFILES_ACTIVE` | `dev` | `staging` | `prod` | Active Spring profile |
| `DATABASE_URL` | `dev-db:5432` | `staging-db:5432` | `prod-db:5432` | Database connection |
| `KAFKA_BOOTSTRAP_SERVERS` | `dev-kafka:9092` | `staging-kafka:9092` | `prod-kafka:9092` | Kafka brokers |
| `REDIS_HOST` | `dev-redis` | `staging-redis` | `prod-redis` | Redis host |
| `LOG_LEVEL` | `DEBUG` | `INFO` | `WARN` | Logging level |
| `JVM_OPTS` | `-Xmx1g` | `-Xmx2g` | `-Xmx4g` | JVM memory settings |

### Configuration Files

```bash
# Environment-specific configuration files
src/main/resources/application-dev.yml
src/main/resources/application-staging.yml
src/main/resources/application-prod.yml

# Helm values files
helm/values-dev.yaml
helm/values-staging.yaml
helm/values-prod.yaml
```

## Local Development Deployment

### Using Docker Compose

1. **Start infrastructure services**:
   ```bash
   docker-compose up -d postgres kafka redis
   ```

2. **Wait for services to be ready**:
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

5. **Build and start the application**:
   ```bash
   ./mvnw clean package -DskipTests
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

### Using Tilt (Recommended for Development)

1. **Start Tilt development environment**:
   ```bash
   ./scripts/deploy-local.sh
   ```

2. **Access Tilt UI**:
   ```bash
   open http://localhost:10350
   ```

3. **Verify deployment**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

### Local Testing

```bash
# Run comprehensive tests
./mvnw clean verify

# Run integration tests
./mvnw verify -Pintegration-tests

# Load test locally
ab -n 100 -c 5 http://localhost:8080/api/v1/exceptions
```

## Development Environment Deployment

### Prerequisites

```bash
# Set up kubectl context
kubectl config use-context dev-cluster

# Verify cluster access
kubectl cluster-info
kubectl get nodes
```

### Deployment Steps

1. **Build and push Docker image**:
   ```bash
   # Build image
   docker build -t interface-exception-collector:${VERSION} .
   
   # Tag for registry
   docker tag interface-exception-collector:${VERSION} \
     ${REGISTRY}/interface-exception-collector:${VERSION}
   
   # Push to registry
   docker push ${REGISTRY}/interface-exception-collector:${VERSION}
   ```

2. **Deploy infrastructure dependencies**:
   ```bash
   # Deploy PostgreSQL
   helm upgrade --install postgresql bitnami/postgresql \
     --namespace biopro-dev \
     --values helm/postgresql-dev-values.yaml
   
   # Deploy Kafka
   helm upgrade --install kafka bitnami/kafka \
     --namespace biopro-dev \
     --values helm/kafka-dev-values.yaml
   
   # Deploy Redis
   helm upgrade --install redis bitnami/redis \
     --namespace biopro-dev \
     --values helm/redis-dev-values.yaml
   ```

3. **Run database migrations**:
   ```bash
   ./scripts/run-migrations.sh --namespace=biopro-dev
   ```

4. **Create Kafka topics**:
   ```bash
   ./scripts/create-kafka-topics.sh --namespace=biopro-dev
   ```

5. **Deploy application**:
   ```bash
   helm upgrade --install interface-exception-collector ./helm \
     --namespace biopro-dev \
     --values helm/values-dev.yaml \
     --set image.tag=${VERSION}
   ```

6. **Verify deployment**:
   ```bash
   kubectl get pods -n biopro-dev -l app=interface-exception-collector
   kubectl logs -f deployment/interface-exception-collector -n biopro-dev
   ```

### Development Environment Configuration

```yaml
# helm/values-dev.yaml
replicaCount: 2

image:
  repository: your-registry/interface-exception-collector
  tag: latest
  pullPolicy: Always

resources:
  limits:
    cpu: 1000m
    memory: 2Gi
  requests:
    cpu: 500m
    memory: 1Gi

env:
  SPRING_PROFILES_ACTIVE: dev
  LOG_LEVEL: DEBUG
  JVM_OPTS: "-Xms1g -Xmx2g"

ingress:
  enabled: true
  hosts:
    - host: exception-collector-dev.biopro.com
      paths:
        - path: /
          pathType: Prefix
```

## Staging Environment Deployment

### Prerequisites

```bash
# Set up kubectl context
kubectl config use-context staging-cluster

# Verify cluster access and resources
kubectl cluster-info
kubectl get nodes
kubectl top nodes
```

### Pre-deployment Validation

```bash
# Validate configuration
helm template interface-exception-collector ./helm \
  --values helm/values-staging.yaml \
  --set image.tag=${VERSION} | kubectl apply --dry-run=client -f -

# Run security scan
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image ${REGISTRY}/interface-exception-collector:${VERSION}
```

### Deployment Steps

1. **Deploy to staging**:
   ```bash
   ./scripts/deploy.sh staging ${VERSION}
   ```

2. **Monitor deployment**:
   ```bash
   kubectl rollout status deployment/interface-exception-collector -n biopro-staging
   kubectl get pods -n biopro-staging -l app=interface-exception-collector -w
   ```

3. **Run smoke tests**:
   ```bash
   ./scripts/smoke-tests.sh staging
   ```

4. **Run integration tests**:
   ```bash
   ./scripts/integration-tests.sh staging --token=${TEST_TOKEN}
   ```

### Staging Environment Configuration

```yaml
# helm/values-staging.yaml
replicaCount: 3

image:
  repository: your-registry/interface-exception-collector
  pullPolicy: IfNotPresent

resources:
  limits:
    cpu: 2000m
    memory: 4Gi
  requests:
    cpu: 1000m
    memory: 2Gi

env:
  SPRING_PROFILES_ACTIVE: staging
  LOG_LEVEL: INFO
  JVM_OPTS: "-Xms2g -Xmx4g"

autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 6
  targetCPUUtilizationPercentage: 70

monitoring:
  enabled: true
  serviceMonitor:
    enabled: true
```

## Production Environment Deployment

### Prerequisites

```bash
# Set up kubectl context
kubectl config use-context prod-cluster

# Verify cluster access
kubectl cluster-info
kubectl auth can-i create deployments --namespace biopro-prod
```

### Pre-production Checklist

- [ ] Staging deployment successful and tested
- [ ] Performance tests completed
- [ ] Security review approved
- [ ] Database backup completed
- [ ] Monitoring and alerting configured
- [ ] Rollback plan documented
- [ ] Change management approval obtained
- [ ] Maintenance window scheduled (if required)

### Blue-Green Deployment Strategy

1. **Prepare blue environment (current production)**:
   ```bash
   # Label current deployment as blue
   kubectl patch deployment interface-exception-collector \
     -n biopro-prod \
     -p '{"metadata":{"labels":{"version":"blue"}}}'
   ```

2. **Deploy green environment (new version)**:
   ```bash
   # Deploy new version with green label
   helm upgrade --install interface-exception-collector-green ./helm \
     --namespace biopro-prod \
     --values helm/values-prod.yaml \
     --set image.tag=${VERSION} \
     --set nameOverride=interface-exception-collector-green \
     --set labels.version=green
   ```

3. **Validate green deployment**:
   ```bash
   # Wait for green deployment to be ready
   kubectl rollout status deployment/interface-exception-collector-green -n biopro-prod
   
   # Run health checks
   kubectl port-forward svc/interface-exception-collector-green 8080:8080 -n biopro-prod &
   curl http://localhost:8080/actuator/health
   
   # Run smoke tests
   ./scripts/smoke-tests.sh prod-green --token=${TEST_TOKEN}
   ```

4. **Switch traffic to green**:
   ```bash
   # Update service selector to point to green deployment
   kubectl patch service interface-exception-collector \
     -n biopro-prod \
     -p '{"spec":{"selector":{"version":"green"}}}'
   ```

5. **Monitor and validate**:
   ```bash
   # Monitor metrics and logs
   kubectl logs -f deployment/interface-exception-collector-green -n biopro-prod
   
   # Check error rates and response times
   curl http://prod-api.biopro.com/actuator/metrics/http.server.requests
   ```

6. **Clean up blue deployment** (after validation):
   ```bash
   # Remove blue deployment
   kubectl delete deployment interface-exception-collector -n biopro-prod
   
   # Rename green deployment
   kubectl patch deployment interface-exception-collector-green \
     -n biopro-prod \
     -p '{"metadata":{"name":"interface-exception-collector"}}'
   ```

### Production Environment Configuration

```yaml
# helm/values-prod.yaml
replicaCount: 5

image:
  repository: your-registry/interface-exception-collector
  pullPolicy: IfNotPresent

resources:
  limits:
    cpu: 4000m
    memory: 8Gi
  requests:
    cpu: 2000m
    memory: 4Gi

env:
  SPRING_PROFILES_ACTIVE: prod
  LOG_LEVEL: WARN
  JVM_OPTS: "-Xms4g -Xmx8g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

autoscaling:
  enabled: true
  minReplicas: 5
  maxReplicas: 15
  targetCPUUtilizationPercentage: 70
  targetMemoryUtilizationPercentage: 80

podDisruptionBudget:
  enabled: true
  minAvailable: 3

monitoring:
  enabled: true
  serviceMonitor:
    enabled: true
  prometheusRule:
    enabled: true

security:
  networkPolicy:
    enabled: true
  podSecurityPolicy:
    enabled: true
```

## Rollback Procedures

### Automatic Rollback

```bash
# Rollback to previous version
kubectl rollout undo deployment/interface-exception-collector -n biopro-prod

# Rollback to specific revision
kubectl rollout undo deployment/interface-exception-collector \
  --to-revision=2 -n biopro-prod

# Check rollback status
kubectl rollout status deployment/interface-exception-collector -n biopro-prod
```

### Manual Rollback

```bash
# Identify previous version
kubectl rollout history deployment/interface-exception-collector -n biopro-prod

# Deploy previous version using Helm
helm upgrade interface-exception-collector ./helm \
  --namespace biopro-prod \
  --values helm/values-prod.yaml \
  --set image.tag=${PREVIOUS_VERSION}
```

### Database Rollback

```bash
# If database migrations need to be rolled back
# Note: Flyway doesn't support automatic rollbacks
# Manual intervention may be required

# Restore from backup if necessary
./scripts/restore-database.sh --backup-file=${BACKUP_FILE} --database=exception_collector_db --confirm
```

## Post-Deployment Verification

### Health Checks

```bash
# Application health
curl https://api.biopro.com/actuator/health

# Readiness check
curl https://api.biopro.com/actuator/health/readiness

# Liveness check
curl https://api.biopro.com/actuator/health/liveness
```

### Functional Tests

```bash
# API functionality
curl -H "Authorization: Bearer $TOKEN" \
  https://api.biopro.com/api/v1/exceptions?page=0&size=5

# Run comprehensive integration tests
./scripts/integration-tests.sh prod --token=${TEST_TOKEN} --no-cleanup
```

### Performance Validation

```bash
# Load test
ab -n 1000 -c 10 -H "Authorization: Bearer $TOKEN" \
  https://api.biopro.com/api/v1/exceptions

# Monitor metrics
curl https://api.biopro.com/actuator/metrics/http.server.requests
curl https://api.biopro.com/actuator/metrics/jvm.memory.used
```

### Monitoring Verification

```bash
# Check Prometheus metrics
curl https://api.biopro.com/actuator/prometheus

# Verify Grafana dashboards
open https://grafana.biopro.com/d/exception-collector

# Check alerting rules
kubectl get prometheusrules -n biopro-prod
```

## Troubleshooting

### Common Deployment Issues

1. **Image Pull Errors**:
   ```bash
   # Check image exists
   docker pull ${REGISTRY}/interface-exception-collector:${VERSION}
   
   # Verify registry credentials
   kubectl get secret regcred -n biopro-prod -o yaml
   ```

2. **Resource Constraints**:
   ```bash
   # Check node resources
   kubectl top nodes
   kubectl describe nodes
   
   # Check pod resource usage
   kubectl top pods -n biopro-prod
   ```

3. **Configuration Issues**:
   ```bash
   # Check ConfigMap
   kubectl get configmap interface-exception-collector-config -n biopro-prod -o yaml
   
   # Check Secrets
   kubectl get secret interface-exception-collector-secret -n biopro-prod
   ```

4. **Network Issues**:
   ```bash
   # Check service endpoints
   kubectl get endpoints -n biopro-prod
   
   # Test network connectivity
   kubectl exec -it <pod-name> -n biopro-prod -- nslookup postgres
   ```

### Deployment Logs

```bash
# View deployment events
kubectl describe deployment interface-exception-collector -n biopro-prod

# Check pod logs
kubectl logs -f deployment/interface-exception-collector -n biopro-prod

# View previous pod logs
kubectl logs deployment/interface-exception-collector -n biopro-prod --previous
```

### Emergency Procedures

1. **Immediate Rollback**:
   ```bash
   kubectl rollout undo deployment/interface-exception-collector -n biopro-prod
   ```

2. **Scale Down**:
   ```bash
   kubectl scale deployment interface-exception-collector --replicas=0 -n biopro-prod
   ```

3. **Emergency Maintenance Mode**:
   ```bash
   # Deploy maintenance page
   kubectl apply -f k8s/maintenance-mode.yaml
   ```

### Script Troubleshooting

#### Common Script Issues

1. **Permission Denied**:
   ```bash
   # Make scripts executable
   chmod +x scripts/*.sh
   ```

2. **Infrastructure Not Ready**:
   ```bash
   # Check infrastructure status
   ./scripts/validate-infrastructure.sh --status-only
   
   # Wait longer for services
   ./scripts/validate-infrastructure.sh --timeout=600
   ```

3. **Migration Failures**:
   ```bash
   # Check migration status
   ./scripts/run-migrations.sh status
   
   # Run migrations with local Flyway
   ./scripts/run-migrations.sh --local
   ```

4. **Test Failures**:
   ```bash
   # Run smoke tests with debug output
   ./scripts/smoke-tests.sh local --timeout=120
   
   # Skip authentication tests if no token
   ./scripts/integration-tests.sh local --skip-auth
   ```

5. **Docker Compose Issues**:
   ```bash
   # Reset Docker Compose environment
   docker-compose down -v
   docker-compose up -d
   
   # Check service logs
   docker-compose logs postgres
   docker-compose logs kafka
   ```

6. **Kubernetes Context Issues**:
   ```bash
   # Check current context
   kubectl config current-context
   
   # Switch to correct context
   kubectl config use-context docker-desktop
   ```

## Available Scripts

The following scripts are available to help with deployment and testing:

### Infrastructure Scripts

- **`scripts/setup-infrastructure.sh`** - Sets up PostgreSQL, Kafka, and Redis infrastructure
- **`scripts/validate-infrastructure.sh`** - Validates that all infrastructure services are ready
- **`scripts/run-migrations.sh`** - Runs database migrations using Flyway
- **`scripts/create-kafka-topics.sh`** - Creates required Kafka topics

### Deployment Scripts

- **`scripts/deploy-local.sh`** - Sets up local development environment with Tilt
- **`scripts/deploy.sh`** - Main deployment script for different environments
- **`scripts/docker-build.sh`** - Builds Docker images
- **`scripts/docker-run.sh`** - Runs application in Docker

### Testing Scripts

- **`scripts/smoke-tests.sh`** - Runs basic smoke tests to verify deployment
- **`scripts/integration-tests.sh`** - Runs comprehensive integration tests

### Utility Scripts

- **`scripts/restore-database.sh`** - Restores database from backup
- **`scripts/cleanup.sh`** - Cleans up development environment

### Script Usage Examples

#### Infrastructure Setup
```bash
# Setup local infrastructure
./scripts/setup-infrastructure.sh --local

# Setup development infrastructure
./scripts/setup-infrastructure.sh --namespace=biopro-dev

# Validate infrastructure is ready
./scripts/validate-infrastructure.sh --namespace=biopro-dev
```

#### Database Operations
```bash
# Run migrations locally
./scripts/run-migrations.sh --local

# Run migrations in Kubernetes
./scripts/run-migrations.sh --namespace=biopro-dev

# Show migration status
./scripts/run-migrations.sh status --namespace=biopro-dev
```

#### Testing
```bash
# Run smoke tests locally
./scripts/smoke-tests.sh local

# Run smoke tests in staging
./scripts/smoke-tests.sh staging --token=${TEST_TOKEN}

# Run integration tests
./scripts/integration-tests.sh staging --token=${TEST_TOKEN} --report
```

#### Local Development
```bash
# Start local development environment
./scripts/deploy-local.sh

# Start with custom ports
./scripts/deploy-local.sh --postgres-port=5433 --kafka-port=9093

# Show development environment info
./scripts/deploy-local.sh --info
```

## Contact Information

- **DevOps Team**: devops-team@biopro.com
- **On-call Engineer**: +1-555-0127
- **Release Manager**: release-manager@biopro.com

## Related Documentation

- [Service Lifecycle Management](runbooks/service-lifecycle.md)
- [Monitoring Setup](runbooks/monitoring-setup.md)
- [Disaster Recovery](runbooks/disaster-recovery.md)