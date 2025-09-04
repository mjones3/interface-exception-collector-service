# Mock RSocket Server Deployment Guide

This guide provides comprehensive deployment procedures for the Mock RSocket Server integration across different environments.

## Table of Contents

- [Overview](#overview)
- [Environment-Specific Deployment](#environment-specific-deployment)
- [Configuration Management](#configuration-management)
- [Container Deployment](#container-deployment)
- [Kubernetes Deployment](#kubernetes-deployment)
- [Tilt Development Deployment](#tilt-development-deployment)
- [Production Considerations](#production-considerations)
- [Monitoring and Health Checks](#monitoring-and-health-checks)
- [Troubleshooting Deployment Issues](#troubleshooting-deployment-issues)

## Overview

The Mock RSocket Server integration supports multiple deployment scenarios:

- **Development**: Local development with Tilt orchestration
- **Testing**: Kubernetes-based testing environments
- **Staging**: Pre-production validation environments
- **Production**: Mock server disabled, production services enabled

## Environment-Specific Deployment

### Development Environment

#### Prerequisites
- Docker Desktop with Kubernetes enabled
- Tilt CLI installed
- kubectl configured for local cluster
- Java 21+ and Maven 3.8+

#### Deployment Steps

1. **Start Development Environment**:
   ```bash
   # Clone repository
   git clone <repository-url>
   cd interface-exception-collector
   
   # Start Tilt development environment
   tilt up
   ```

2. **Verify Deployment**:
   ```bash
   # Check all services are running
   kubectl get pods
   
   # Verify mock server is accessible
   curl -H @auth_header.txt "http://localhost:8080/actuator/health/rsocket"
   
   # Test order data retrieval
   curl -X POST \
     -H @auth_header.txt \
     -H "Content-Type: application/json" \
     -d '{"externalId": "TEST-ORDER-1", "operation": "CREATE_ORDER", "rejectedReason": "Test"}' \
     "http://localhost:8080/api/v1/exceptions"
   ```

3. **Development Configuration**:
   ```yaml
   # Automatically configured by Tilt
   app:
     rsocket:
       mock-server:
         enabled: true
         host: mock-rsocket-server
         port: 7000
         timeout: 5s
         debug-logging: true
   ```

### Testing Environment

#### Prerequisites
- Kubernetes cluster access
- Helm 3.x installed
- Container registry access
- Test database and Kafka cluster

#### Deployment Steps

1. **Deploy Infrastructure**:
   ```bash
   # Create namespace
   kubectl create namespace exception-collector-test
   
   # Deploy PostgreSQL
   helm install postgresql bitnami/postgresql \
     --namespace exception-collector-test \
     --set auth.database=exception_collector_test
   
   # Deploy Kafka
   helm install kafka bitnami/kafka \
     --namespace exception-collector-test
   ```

2. **Deploy Mock Server**:
   ```bash
   # Create ConfigMaps for mappings and responses
   kubectl create configmap mock-rsocket-mappings \
     --from-file=mappings/ \
     --namespace exception-collector-test
   
   kubectl create configmap mock-rsocket-responses \
     --from-file=mock-responses/ \
     --namespace exception-collector-test
   
   # Deploy mock server
   kubectl apply -f k8s/mock-rsocket-server.yaml \
     --namespace exception-collector-test
   ```

3. **Deploy Application**:
   ```bash
   # Deploy with test configuration
   helm install interface-exception-collector ./helm \
     --namespace exception-collector-test \
     --values helm/values-test.yaml \
     --set app.rsocket.mock-server.enabled=true
   ```

4. **Verify Test Deployment**:
   ```bash
   # Check pod status
   kubectl get pods -n exception-collector-test
   
   # Check service endpoints
   kubectl get svc -n exception-collector-test
   
   # Run health checks
   kubectl exec -n exception-collector-test deployment/interface-exception-collector -- \
     curl http://localhost:8080/actuator/health/rsocket
   ```

### Staging Environment

#### Prerequisites
- Kubernetes cluster with ingress controller
- External database and Kafka cluster
- SSL certificates configured
- Monitoring stack deployed

#### Deployment Steps

1. **Prepare Configuration**:
   ```bash
   # Create staging configuration
   kubectl create configmap interface-exception-collector-config \
     --from-file=application-staging.yml \
     --namespace exception-collector-staging
   ```

2. **Deploy with Helm**:
   ```bash
   # Deploy to staging
   helm install interface-exception-collector ./helm \
     --namespace exception-collector-staging \
     --values helm/values-staging.yaml \
     --set app.rsocket.mock-server.enabled=true \
     --set ingress.enabled=true \
     --set ingress.hosts[0].host=exception-collector-staging.company.com
   ```

3. **Configure Ingress**:
   ```yaml
   # ingress configuration in values-staging.yaml
   ingress:
     enabled: true
     className: "nginx"
     annotations:
       cert-manager.io/cluster-issuer: "letsencrypt-prod"
       nginx.ingress.kubernetes.io/ssl-redirect: "true"
     hosts:
       - host: exception-collector-staging.company.com
         paths:
           - path: /
             pathType: Prefix
     tls:
       - secretName: exception-collector-staging-tls
         hosts:
           - exception-collector-staging.company.com
   ```

4. **Verify Staging Deployment**:
   ```bash
   # Check deployment status
   helm status interface-exception-collector -n exception-collector-staging
   
   # Test external access
   curl -H @auth_header.txt "https://exception-collector-staging.company.com/actuator/health"
   
   # Verify mock server integration
   curl -H @auth_header.txt "https://exception-collector-staging.company.com/actuator/health/rsocket"
   ```

## Configuration Management

### Environment-Specific Configuration Files

#### Development (application-dev.yml)
```yaml
spring:
  profiles:
    active: dev

app:
  rsocket:
    mock-server:
      enabled: true
      host: mock-rsocket-server
      port: 7000
      timeout: 5s
      debug-logging: true
      circuit-breaker:
        failure-rate-threshold: 30
        wait-duration-in-open-state: 10s

logging:
  level:
    com.arcone.biopro.exception.collector.infrastructure.client: DEBUG
```

#### Testing (application-test.yml)
```yaml
spring:
  profiles:
    active: test

app:
  rsocket:
    mock-server:
      enabled: true
      host: mock-rsocket-server
      port: 7000
      timeout: 10s
      circuit-breaker:
        failure-rate-threshold: 40
        wait-duration-in-open-state: 15s

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

#### Staging (application-staging.yml)
```yaml
spring:
  profiles:
    active: staging

app:
  rsocket:
    mock-server:
      enabled: true
      host: mock-rsocket-server
      port: 7000
      timeout: 15s
      circuit-breaker:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

#### Production (application-prod.yml)
```yaml
spring:
  profiles:
    active: prod

app:
  rsocket:
    mock-server:
      enabled: false  # MUST be false in production
    partner-order-service:
      enabled: true
      host: partner-order-service
      port: 8090
      timeout: 30s

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### Configuration Validation

The application performs startup validation to ensure proper configuration:

```java
@Component
public class ConfigurationValidator {
    
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        // Prevent mock server in production
        if (isProductionEnvironment() && mockServerEnabled) {
            throw new IllegalStateException("Mock server cannot be enabled in production");
        }
        
        // Validate connection parameters
        validateConnectionSettings();
        
        // Warn about suboptimal configurations
        checkForWarnings();
    }
}
```

## Container Deployment

### Mock Server Container

#### Container Image
```dockerfile
# Mock server uses pre-built image
FROM artifactory.sha.ao.arc-one.com/docker/biopro/utils/rsocket_mock:21.0.1

# Configuration is provided via volume mounts
VOLUME ["/app/mappings", "/app/__files"]

# Default port
EXPOSE 7000

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD nc -z localhost 7000 || exit 1
```

#### Volume Mounts
```yaml
# Kubernetes volume configuration
volumes:
  - name: mappings
    configMap:
      name: mock-rsocket-mappings
  - name: responses
    configMap:
      name: mock-rsocket-responses

volumeMounts:
  - name: mappings
    mountPath: /app/mappings
    readOnly: true
  - name: responses
    mountPath: /app/__files
    readOnly: true
```

### Application Container

#### Environment Variables
```yaml
env:
  - name: SPRING_PROFILES_ACTIVE
    value: "dev"
  - name: MOCK_RSOCKET_SERVER_ENABLED
    value: "true"
  - name: MOCK_RSOCKET_SERVER_HOST
    value: "mock-rsocket-server"
  - name: MOCK_RSOCKET_SERVER_PORT
    value: "7000"
```

## Kubernetes Deployment

### Complete Kubernetes Manifests

#### Mock Server Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mock-rsocket-server
  labels:
    app: mock-rsocket-server
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mock-rsocket-server
  template:
    metadata:
      labels:
        app: mock-rsocket-server
    spec:
      containers:
      - name: mock-rsocket-server
        image: artifactory.sha.ao.arc-one.com/docker/biopro/utils/rsocket_mock:21.0.1
        ports:
        - containerPort: 7000
          name: rsocket
        volumeMounts:
        - name: mappings
          mountPath: /app/mappings
          readOnly: true
        - name: responses
          mountPath: /app/__files
          readOnly: true
        env:
        - name: SERVER_PORT
          value: "7000"
        - name: MAPPINGS_PATH
          value: "/app/mappings"
        - name: RESPONSES_PATH
          value: "/app/__files"
        resources:
          limits:
            memory: "512Mi"
            cpu: "500m"
          requests:
            memory: "256Mi"
            cpu: "250m"
        livenessProbe:
          tcpSocket:
            port: 7000
          initialDelaySeconds: 10
          periodSeconds: 30
        readinessProbe:
          tcpSocket:
            port: 7000
          initialDelaySeconds: 5
          periodSeconds: 10
      volumes:
      - name: mappings
        configMap:
          name: mock-rsocket-mappings
      - name: responses
        configMap:
          name: mock-rsocket-responses
---
apiVersion: v1
kind: Service
metadata:
  name: mock-rsocket-server
  labels:
    app: mock-rsocket-server
spec:
  selector:
    app: mock-rsocket-server
  ports:
  - port: 7000
    targetPort: 7000
    name: rsocket
  type: ClusterIP
```

#### ConfigMaps Creation
```bash
# Create mappings ConfigMap
kubectl create configmap mock-rsocket-mappings \
  --from-file=mappings/ \
  --namespace <namespace>

# Create responses ConfigMap
kubectl create configmap mock-rsocket-responses \
  --from-file=mock-responses/ \
  --namespace <namespace>

# Update ConfigMaps when files change
kubectl create configmap mock-rsocket-mappings \
  --from-file=mappings/ \
  --dry-run=client -o yaml | kubectl apply -f -
```

## Tilt Development Deployment

### Tiltfile Configuration

```python
# Mock RSocket Server
k8s_yaml('k8s/mock-rsocket-server.yaml')

# Create ConfigMaps from local files
k8s_yaml(configmap_create('mock-rsocket-mappings', from_file='mappings/'))
k8s_yaml(configmap_create('mock-rsocket-responses', from_file='mock-responses/'))

# Configure resource with port forwarding
k8s_resource(
    'mock-rsocket-server',
    port_forwards='7000:7000',
    resource_deps=['kafka'],
    labels=['infrastructure']
)

# Watch for mapping file changes
watch_file('./mappings')
watch_file('./mock-responses')

# Reload mappings when files change
local_resource(
    'reload-mock-mappings',
    'kubectl rollout restart deployment/mock-rsocket-server',
    deps=['mappings', 'mock-responses'],
    auto_init=False,
    trigger_mode=TRIGGER_MODE_AUTO,
    labels=['utilities']
)

# Interface Exception Collector with dependency on mock server
k8s_resource(
    'interface-exception-collector',
    resource_deps=['postgres', 'kafka', 'mock-rsocket-server'],
    port_forwards='8080:8080',
    labels=['application']
)
```

### Tilt Commands

```bash
# Start development environment
tilt up

# Restart specific service
tilt restart mock-rsocket-server

# Trigger mapping reload
tilt trigger reload-mock-mappings

# View logs
tilt logs mock-rsocket-server

# Stop environment
tilt down
```

## Production Considerations

### Security

1. **Mock Server Disabled**: Ensure mock server is never deployed in production
2. **Configuration Validation**: Startup validation prevents accidental mock server usage
3. **Network Policies**: Restrict network access between services
4. **Resource Limits**: Set appropriate CPU and memory limits

### Monitoring

1. **Health Checks**: Configure comprehensive health checks
2. **Metrics Collection**: Enable Prometheus metrics
3. **Alerting**: Set up alerts for service failures
4. **Logging**: Configure structured logging with correlation IDs

### High Availability

1. **Multiple Replicas**: Deploy multiple application instances
2. **Load Balancing**: Configure proper load balancing
3. **Circuit Breakers**: Enable circuit breaker patterns
4. **Graceful Shutdown**: Configure graceful shutdown procedures

### Production Deployment Checklist

- [ ] Mock server configuration disabled
- [ ] Production service endpoints configured
- [ ] SSL/TLS certificates configured
- [ ] Database connections secured
- [ ] Monitoring and alerting configured
- [ ] Resource limits set appropriately
- [ ] Health checks configured
- [ ] Backup and recovery procedures tested
- [ ] Security scanning completed
- [ ] Performance testing completed

## Monitoring and Health Checks

### Health Check Endpoints

```bash
# Application health
curl https://your-domain.com/actuator/health

# RSocket health (should fail in production)
curl https://your-domain.com/actuator/health/rsocket

# Detailed health information
curl https://your-domain.com/actuator/health?show-details=always
```

### Metrics Monitoring

```bash
# Application metrics
curl https://your-domain.com/actuator/metrics

# Prometheus metrics
curl https://your-domain.com/actuator/prometheus

# Specific RSocket metrics
curl https://your-domain.com/actuator/metrics/rsocket.calls.total
```

### Logging Configuration

```yaml
logging:
  level:
    com.arcone.biopro.exception.collector: INFO
    com.arcone.biopro.exception.collector.infrastructure.client: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/interface-exception-collector.log
```

## Troubleshooting Deployment Issues

### Common Deployment Problems

1. **ConfigMap Not Found**:
   ```bash
   # Check ConfigMap exists
   kubectl get configmap mock-rsocket-mappings
   
   # Recreate if missing
   kubectl create configmap mock-rsocket-mappings --from-file=mappings/
   ```

2. **Image Pull Errors**:
   ```bash
   # Check image availability
   docker pull artifactory.sha.ao.arc-one.com/docker/biopro/utils/rsocket_mock:21.0.1
   
   # Check registry credentials
   kubectl get secret regcred
   ```

3. **Service Discovery Issues**:
   ```bash
   # Check service exists
   kubectl get svc mock-rsocket-server
   
   # Check endpoints
   kubectl get endpoints mock-rsocket-server
   
   # Test DNS resolution
   kubectl exec -it deployment/interface-exception-collector -- nslookup mock-rsocket-server
   ```

4. **Configuration Validation Failures**:
   ```bash
   # Check application logs
   kubectl logs deployment/interface-exception-collector | grep "Configuration validation"
   
   # Verify configuration
   kubectl get configmap interface-exception-collector-config -o yaml
   ```

### Deployment Verification

```bash
# Complete deployment verification script
#!/bin/bash

echo "Verifying Mock RSocket Server deployment..."

# Check pod status
kubectl get pods -l app=mock-rsocket-server
if [ $? -ne 0 ]; then
  echo "ERROR: Mock server pod not found"
  exit 1
fi

# Check service
kubectl get svc mock-rsocket-server
if [ $? -ne 0 ]; then
  echo "ERROR: Mock server service not found"
  exit 1
fi

# Check ConfigMaps
kubectl get configmap mock-rsocket-mappings
kubectl get configmap mock-rsocket-responses

# Test application health
kubectl exec deployment/interface-exception-collector -- \
  curl -f http://localhost:8080/actuator/health/rsocket
if [ $? -eq 0 ]; then
  echo "SUCCESS: Mock RSocket server integration is healthy"
else
  echo "WARNING: RSocket health check failed (expected in production)"
fi

echo "Deployment verification complete"
```

This deployment guide provides comprehensive procedures for deploying the Mock RSocket Server integration across all environments while maintaining security and operational best practices.