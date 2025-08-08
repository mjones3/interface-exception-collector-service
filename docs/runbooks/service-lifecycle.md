# Service Lifecycle Management Runbook

## Overview

This runbook covers procedures for starting, stopping, and managing the Interface Exception Collector Service lifecycle in different environments.

## Service Startup

### Prerequisites Check

Before starting the service, verify all dependencies are available:

```bash
# Check database connectivity
pg_isready -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER

# Check Kafka connectivity
kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --list

# Check Redis connectivity
redis-cli -h $REDIS_HOST -p $REDIS_PORT ping
```

### Local Development Startup

1. **Start infrastructure services**:
   ```bash
   docker-compose up -d postgres kafka redis
   ```

2. **Wait for services to be ready**:
   ```bash
   # Wait for PostgreSQL
   until pg_isready -h localhost -p 5432; do sleep 1; done
   
   # Wait for Kafka
   until kafka-topics.sh --bootstrap-server localhost:9092 --list; do sleep 1; done
   
   # Wait for Redis
   until redis-cli ping; do sleep 1; done
   ```

3. **Run database migrations**:
   ```bash
   ./scripts/run-migrations.sh
   ```

4. **Create Kafka topics**:
   ```bash
   ./scripts/create-kafka-topics.sh
   ```

5. **Start the application**:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

### Kubernetes Startup

1. **Deploy using Helm**:
   ```bash
   helm upgrade --install interface-exception-collector ./helm \
     --namespace biopro-services \
     --values helm/values-${ENVIRONMENT}.yaml
   ```

2. **Verify deployment**:
   ```bash
   kubectl get pods -n biopro-services -l app=interface-exception-collector
   kubectl logs -f deployment/interface-exception-collector -n biopro-services
   ```

3. **Check health endpoints**:
   ```bash
   kubectl port-forward svc/interface-exception-collector 8080:8080 -n biopro-services
   curl http://localhost:8080/actuator/health
   ```

### Startup Verification

Verify the service is running correctly:

```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Check readiness
curl http://localhost:8080/actuator/health/readiness

# Check liveness
curl http://localhost:8080/actuator/health/liveness

# Verify API endpoints
curl http://localhost:8080/api/v1/exceptions?page=0&size=1

# Check metrics
curl http://localhost:8080/actuator/metrics
```

## Service Shutdown

### Graceful Shutdown

1. **Stop accepting new requests**:
   ```bash
   # Remove from load balancer or service mesh
   kubectl patch service interface-exception-collector -p '{"spec":{"selector":{"shutdown":"true"}}}'
   ```

2. **Allow current requests to complete**:
   ```bash
   # Wait for active connections to drain (default 30 seconds)
   sleep 30
   ```

3. **Stop the application**:
   ```bash
   # For local development
   kill -TERM $PID
   
   # For Kubernetes
   kubectl delete pod -l app=interface-exception-collector
   ```

### Emergency Shutdown

If graceful shutdown fails:

```bash
# Force kill the process
kill -KILL $PID

# For Kubernetes
kubectl delete pod -l app=interface-exception-collector --force --grace-period=0
```

## Service Restart

### Rolling Restart (Zero Downtime)

```bash
# Kubernetes rolling restart
kubectl rollout restart deployment/interface-exception-collector -n biopro-services

# Verify rollout
kubectl rollout status deployment/interface-exception-collector -n biopro-services
```

### Full Restart

```bash
# Stop the service
kubectl scale deployment interface-exception-collector --replicas=0 -n biopro-services

# Wait for pods to terminate
kubectl wait --for=delete pod -l app=interface-exception-collector --timeout=60s -n biopro-services

# Start the service
kubectl scale deployment interface-exception-collector --replicas=3 -n biopro-services

# Verify startup
kubectl wait --for=condition=ready pod -l app=interface-exception-collector --timeout=300s -n biopro-services
```

## Configuration Updates

### Environment Variables

1. **Update ConfigMap**:
   ```bash
   kubectl patch configmap interface-exception-collector-config \
     --patch '{"data":{"KAFKA_BOOTSTRAP_SERVERS":"new-kafka:9092"}}'
   ```

2. **Restart pods to pick up changes**:
   ```bash
   kubectl rollout restart deployment/interface-exception-collector
   ```

### Application Properties

1. **Update application.yml in ConfigMap**:
   ```bash
   kubectl create configmap interface-exception-collector-config \
     --from-file=application.yml=./config/application-prod.yml \
     --dry-run=client -o yaml | kubectl apply -f -
   ```

2. **Restart deployment**:
   ```bash
   kubectl rollout restart deployment/interface-exception-collector
   ```

## Health Monitoring

### Continuous Health Checks

```bash
#!/bin/bash
# health-monitor.sh
while true; do
  if ! curl -f http://localhost:8080/actuator/health/liveness; then
    echo "$(date): Liveness check failed"
    # Alert operations team
  fi
  
  if ! curl -f http://localhost:8080/actuator/health/readiness; then
    echo "$(date): Readiness check failed"
    # Alert operations team
  fi
  
  sleep 30
done
```

### Resource Monitoring

```bash
# Monitor CPU and memory usage
kubectl top pods -l app=interface-exception-collector -n biopro-services

# Monitor JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/jvm.gc.pause
```

## Troubleshooting Startup Issues

### Database Connection Issues

```bash
# Check database connectivity
pg_isready -h $DATABASE_HOST -p $DATABASE_PORT

# Test database connection with credentials
psql -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER -d $DATABASE_NAME -c "SELECT 1"

# Check database migrations
psql -h $DATABASE_HOST -p $DATABASE_PORT -U $DATABASE_USER -d $DATABASE_NAME \
  -c "SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 1"
```

### Kafka Connection Issues

```bash
# Test Kafka connectivity
kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS --list

# Check consumer group
kafka-consumer-groups.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --describe --group interface-exception-collector

# Verify topic existence
kafka-topics.sh --bootstrap-server $KAFKA_BOOTSTRAP_SERVERS \
  --describe --topic OrderRejected
```

### Redis Connection Issues

```bash
# Test Redis connectivity
redis-cli -h $REDIS_HOST -p $REDIS_PORT ping

# Check Redis memory usage
redis-cli -h $REDIS_HOST -p $REDIS_PORT info memory

# Test cache operations
redis-cli -h $REDIS_HOST -p $REDIS_PORT set test-key test-value
redis-cli -h $REDIS_HOST -p $REDIS_PORT get test-key
```

### Port Conflicts

```bash
# Check if port 8080 is in use
netstat -tulpn | grep :8080
lsof -i :8080

# Find alternative ports
netstat -tulpn | grep LISTEN | sort -n
```

### Memory Issues

```bash
# Check available memory
free -h

# Check JVM memory settings
ps aux | grep java | grep interface-exception-collector

# Monitor memory usage during startup
watch -n 1 'ps -p $PID -o pid,ppid,cmd,%mem,%cpu --no-headers'
```

## Log Analysis During Startup

```bash
# Follow application logs
tail -f /var/log/app/application.log

# Filter for startup-related logs
grep -E "(Started|Failed|Error)" /var/log/app/application.log

# Check for specific error patterns
grep -E "(Connection|Timeout|Exception)" /var/log/app/application.log

# Monitor structured logs
tail -f /var/log/app/application.log | jq 'select(.level == "ERROR")'
```

## Rollback Procedures

### Application Rollback

```bash
# Rollback to previous version
kubectl rollout undo deployment/interface-exception-collector -n biopro-services

# Rollback to specific revision
kubectl rollout undo deployment/interface-exception-collector --to-revision=2 -n biopro-services

# Check rollback status
kubectl rollout status deployment/interface-exception-collector -n biopro-services
```

### Configuration Rollback

```bash
# Restore previous ConfigMap
kubectl apply -f backup/configmap-backup.yaml

# Restart deployment to pick up old config
kubectl rollout restart deployment/interface-exception-collector -n biopro-services
```

## Emergency Contacts

- **On-call Engineer**: +1-555-0123
- **Database Team**: db-team@biopro.com
- **Infrastructure Team**: infra-team@biopro.com
- **Development Team**: dev-team@biopro.com

## Related Runbooks

- [Database Troubleshooting](database-troubleshooting.md)
- [Kafka Troubleshooting](kafka-troubleshooting.md)
- [Performance Tuning](performance-tuning.md)
- [Monitoring Setup](monitoring-setup.md)