# Production Deployment Guide

## Interface Exception Collector Service - GraphQL API

This guide covers the production deployment configuration for the Interface Exception Collector Service with GraphQL API, including zero-downtime blue-green deployments.

## Table of Contents

1. [Production Configuration](#production-configuration)
2. [JVM Tuning](#jvm-tuning)
3. [Blue-Green Deployment](#blue-green-deployment)
4. [WebSocket Graceful Shutdown](#websocket-graceful-shutdown)
5. [Monitoring and Health Checks](#monitoring-and-health-checks)
6. [Security Configuration](#security-configuration)
7. [Performance Optimization](#performance-optimization)
8. [Troubleshooting](#troubleshooting)

## Production Configuration

### GraphQL Security Settings

The production configuration (`application-prod.yml`) includes the following security enhancements:

```yaml
spring:
  graphql:
    graphiql:
      enabled: false  # Disable GraphiQL in production
    schema:
      introspection:
        enabled: false  # Disable GraphQL introspection in production
```

### Connection Pool Configuration

#### Database Connection Pool (HikariCP)
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50          # Increased for production load
      minimum-idle: 10               # Higher minimum for production
      connection-timeout: 20000      # 20 seconds
      idle-timeout: 300000           # 5 minutes
      max-lifetime: 1200000          # 20 minutes
      leak-detection-threshold: 60000 # 1 minute leak detection
      validation-timeout: 5000       # 5 seconds
      initialization-fail-timeout: 1 # Fail fast on startup
      isolate-internal-queries: true
      allow-pool-suspension: false
      read-only: false
      register-mbeans: true
      pool-name: "ExceptionCollectorCP"
```

#### Redis Connection Pool (Lettuce)
```yaml
spring:
  data:
    redis:
      timeout: 3000ms              # 3 seconds timeout
      connect-timeout: 2000ms      # 2 seconds connection timeout
      lettuce:
        pool:
          max-active: 20           # Maximum active connections
          max-idle: 10             # Maximum idle connections
          min-idle: 5              # Minimum idle connections
          max-wait: 5000ms         # Maximum wait time for connection
          time-between-eviction-runs: 30000ms # 30 seconds
        shutdown-timeout: 100ms
```

## JVM Tuning

### JVM Configuration File

The production JVM configuration is defined in `jvm-production.conf`:

#### Memory Configuration
- **Heap Size**: 2GB initial, 4GB maximum
- **Metaspace**: 512MB maximum
- **Compressed Class Space**: 128MB

#### Garbage Collection (G1GC)
- **Max GC Pause**: 100ms
- **Heap Region Size**: 16MB
- **New Generation**: 30-40% of heap
- **Mixed GC Target**: 8 collections

#### Performance Optimizations
- String deduplication enabled
- Compressed OOPs enabled
- Tiered compilation enabled
- Code cache optimizations

### Usage

#### Docker Deployment
```bash
# Build production image
docker build -f Dockerfile.production -t biopro/interface-exception-collector:latest .

# Run with production JVM settings
docker run -d \
  --name interface-exception-collector \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JAVA_TOOL_OPTIONS="@/app/jvm-production.conf" \
  biopro/interface-exception-collector:latest
```

#### Direct Java Execution
```bash
# Use startup script
./start-production.sh start

# Or run directly with JVM config
java @jvm-production.conf -jar interface-exception-collector.jar
```

## Blue-Green Deployment

### Overview

Blue-green deployment enables zero-downtime updates by maintaining two identical production environments (blue and green) and switching traffic between them.

### Deployment Process

1. **Deploy to Inactive Environment**: Deploy new version to the inactive environment
2. **Health Checks**: Verify application health and readiness
3. **Smoke Tests**: Run automated tests against the new deployment
4. **Traffic Switch**: Switch load balancer to point to new environment
5. **Validation**: Verify traffic is flowing correctly
6. **Cleanup**: Scale down old environment

### Using the Deployment Script

```bash
# Deploy new version
./scripts/blue-green-deploy.sh deploy v1.2.3

# Deploy with custom replica count
./scripts/blue-green-deploy.sh deploy v1.2.3 5

# Check deployment status
./scripts/blue-green-deploy.sh status

# Rollback if needed
./scripts/blue-green-deploy.sh rollback blue

# Manual traffic switch
./scripts/blue-green-deploy.sh switch green
```

### Kubernetes Configuration

The Kubernetes deployment includes:

- **Blue and Green Deployments**: Separate deployments for each environment
- **Service Selector**: Routes traffic based on version label
- **Health Checks**: Liveness, readiness, and startup probes
- **Resource Limits**: CPU and memory constraints
- **Pod Disruption Budget**: Ensures minimum availability
- **Horizontal Pod Autoscaler**: Automatic scaling based on metrics

### Deployment Verification

The deployment script performs the following checks:

1. **Deployment Readiness**: All pods are ready and available
2. **Health Endpoint**: `/actuator/health` returns 200 OK
3. **Readiness Endpoint**: `/actuator/health/readiness` returns 200 OK
4. **GraphQL Security**: Introspection is disabled
5. **Authentication**: GraphQL requires proper authentication
6. **Metrics**: Prometheus metrics endpoint is accessible

## WebSocket Graceful Shutdown

### Configuration

WebSocket connections are gracefully closed during shutdown to prevent data loss and ensure smooth user experience.

```yaml
websocket:
  graceful-shutdown:
    enabled: true
    timeout: 30s
    close-code: 1001  # Going Away
    close-reason: "Server shutting down"
  connection-limits:
    max-connections: 2000
    max-connections-per-user: 10
    connection-timeout: 60s
    heartbeat-interval: 30s
```

### Shutdown Process

1. **Shutdown Notification**: Send notification to all connected clients
2. **Grace Period**: Wait 2 seconds for clients to receive notification
3. **Connection Closure**: Close all WebSocket connections gracefully
4. **Timeout Handling**: Force close connections after timeout

### Client Handling

Clients should handle the shutdown notification and automatically reconnect:

```javascript
// WebSocket client example
const ws = new WebSocket('ws://localhost:8080/subscriptions');

ws.onmessage = function(event) {
    const message = JSON.parse(event.data);
    
    if (message.type === 'shutdown') {
        console.log('Server shutting down:', message.message);
        // Implement reconnection logic
        setTimeout(() => reconnect(), 5000);
    }
};
```

## Monitoring and Health Checks

### Health Check Endpoints

- **Liveness**: `/actuator/health/liveness` - Application is running
- **Readiness**: `/actuator/health/readiness` - Application is ready to serve traffic
- **Overall Health**: `/actuator/health` - Comprehensive health status

### Metrics

Prometheus metrics are available at `/actuator/prometheus`:

- **GraphQL Metrics**: Query duration, error rates, complexity
- **Connection Pool Metrics**: Database and Redis connection usage
- **JVM Metrics**: Memory usage, garbage collection, threads
- **WebSocket Metrics**: Active connections, message rates

### Alerting Thresholds

Configure alerts for the following metrics:

- **Query Response Time**: > 500ms (95th percentile)
- **Mutation Response Time**: > 3000ms (95th percentile)
- **Error Rate**: > 5%
- **Cache Miss Rate**: > 20%
- **Database Connection Usage**: > 80%
- **Memory Usage**: > 85%

## Security Configuration

### Production Security Features

1. **GraphQL Introspection Disabled**: Prevents schema discovery
2. **Query Allowlist**: Only pre-approved queries are allowed
3. **Rate Limiting**: Per-user and per-role request limits
4. **Audit Logging**: All GraphQL operations are logged
5. **TLS Encryption**: All traffic encrypted in transit
6. **JWT Authentication**: Secure token-based authentication

### Environment Variables

```bash
# Security
export TLS_ENABLED=true
export JWT_SECRET=your-production-jwt-secret
export GRAPHQL_QUERY_ALLOWLIST_ENABLED=true
export GRAPHQL_RATE_LIMITING_ENABLED=true

# Database
export DB_SSL_MODE=require
export DB_SSL_CERT=/path/to/client-cert.pem
export DB_SSL_KEY=/path/to/client-key.pem
export DB_SSL_ROOT_CERT=/path/to/ca-cert.pem

# Kafka
export KAFKA_SECURITY_PROTOCOL=SSL
export KAFKA_SSL_TRUSTSTORE_LOCATION=/path/to/kafka.client.truststore.jks
export KAFKA_SSL_KEYSTORE_LOCATION=/path/to/kafka.client.keystore.jks
```

## Performance Optimization

### Database Optimization

1. **Connection Pooling**: Optimized HikariCP configuration
2. **Query Optimization**: Proper indexing and query patterns
3. **Connection Limits**: Balanced pool sizes for load

### Caching Strategy

1. **Redis Caching**: Multi-level caching with appropriate TTLs
2. **DataLoader Pattern**: Batch loading to prevent N+1 queries
3. **Query Result Caching**: Cache expensive aggregations

### JVM Optimization

1. **G1 Garbage Collector**: Low-latency GC for real-time requirements
2. **Memory Tuning**: Optimized heap and metaspace sizes
3. **Compilation Optimization**: Tiered compilation for faster startup

## Troubleshooting

### Common Issues

#### High Memory Usage
```bash
# Check heap dump
jcmd <pid> GC.run_finalization
jcmd <pid> VM.gc
jmap -dump:format=b,file=heapdump.hprof <pid>
```

#### Database Connection Issues
```bash
# Check connection pool status
curl http://localhost:8080/actuator/health/db

# Check HikariCP metrics
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

#### WebSocket Connection Problems
```bash
# Check active WebSocket connections
curl http://localhost:8080/actuator/metrics/websocket.connections.active

# Check WebSocket health
curl http://localhost:8080/actuator/health/websocket
```

#### GraphQL Performance Issues
```bash
# Check GraphQL metrics
curl http://localhost:8080/actuator/metrics/graphql.query.duration

# Check query complexity
curl http://localhost:8080/actuator/metrics/graphql.query.complexity
```

### Log Analysis

#### Application Logs
```bash
# Follow application logs
tail -f /var/log/app/application.log

# Search for errors
grep -i error /var/log/app/application.log

# Check GraphQL operations
grep "GraphQL" /var/log/app/application.log
```

#### GC Logs
```bash
# Analyze GC performance
tail -f /var/log/gc/gc.log

# GC analysis tools
gcviewer /var/log/gc/gc.log
```

#### JFR Analysis
```bash
# Analyze JFR recording
jfr print /var/log/jfr/app-recording.jfr

# Generate JFR report
jfr summary /var/log/jfr/app-recording.jfr
```

### Performance Tuning

#### JVM Tuning
```bash
# Adjust heap size based on usage
export JAVA_MEMORY_LIMIT=6g

# Tune GC parameters
export GC_MAX_PAUSE_MILLIS=50
export GC_HEAP_REGION_SIZE=32m
```

#### Database Tuning
```bash
# Adjust connection pool
export HIKARI_MAXIMUM_POOL_SIZE=100
export HIKARI_MINIMUM_IDLE=20
```

#### Cache Tuning
```bash
# Adjust Redis pool
export REDIS_LETTUCE_POOL_MAX_ACTIVE=50
export REDIS_LETTUCE_POOL_MAX_IDLE=25
```

## Deployment Checklist

### Pre-Deployment

- [ ] JVM configuration reviewed and tested
- [ ] Database connection pool sized appropriately
- [ ] Redis cache configuration validated
- [ ] Security settings verified
- [ ] Health check endpoints tested
- [ ] Monitoring and alerting configured

### Deployment

- [ ] Blue-green deployment script tested
- [ ] Smoke tests passing
- [ ] Health checks passing
- [ ] WebSocket graceful shutdown tested
- [ ] Performance metrics within acceptable ranges

### Post-Deployment

- [ ] Traffic successfully switched
- [ ] All health checks green
- [ ] No error spikes in logs
- [ ] Performance metrics stable
- [ ] WebSocket connections stable
- [ ] Old deployment scaled down

### Rollback Plan

- [ ] Rollback procedure documented
- [ ] Rollback script tested
- [ ] Database migration rollback plan
- [ ] Cache invalidation strategy
- [ ] Communication plan for rollback

## Support and Maintenance

### Regular Maintenance Tasks

1. **Log Rotation**: Ensure log files don't fill disk space
2. **Metric Collection**: Monitor key performance indicators
3. **Security Updates**: Keep dependencies up to date
4. **Performance Review**: Regular performance analysis
5. **Capacity Planning**: Monitor resource usage trends

### Emergency Procedures

1. **Service Restart**: Use graceful shutdown procedures
2. **Traffic Diversion**: Use blue-green deployment for emergency fixes
3. **Database Issues**: Connection pool adjustment and query optimization
4. **Memory Issues**: Heap dump analysis and JVM tuning
5. **Security Incidents**: Immediate response and audit procedures

For additional support, refer to the monitoring dashboards and alerting systems configured for the production environment.