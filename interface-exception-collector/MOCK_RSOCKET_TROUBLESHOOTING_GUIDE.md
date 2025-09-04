# Mock RSocket Server Integration Troubleshooting Guide

This guide provides comprehensive troubleshooting information for the Mock RSocket Server integration in the BioPro Interface Exception Collector service.

## Table of Contents

- [Common Issues](#common-issues)
- [Configuration Problems](#configuration-problems)
- [Connection Issues](#connection-issues)
- [Mapping and Response Issues](#mapping-and-response-issues)
- [Performance Issues](#performance-issues)
- [Debugging Tools](#debugging-tools)
- [Monitoring and Health Checks](#monitoring-and-health-checks)
- [Environment-Specific Issues](#environment-specific-issues)

## Common Issues

### 1. Mock Server Not Starting

**Symptoms:**
- Tilt shows mock-rsocket-server as failing
- Connection refused errors in application logs
- Health check fails for RSocket

**Diagnosis:**
```bash
# Check mock server pod status
kubectl get pods -l app=mock-rsocket-server

# Check mock server logs
tilt logs mock-rsocket-server

# Check port availability
kubectl get svc mock-rsocket-server
```

**Solutions:**
```bash
# Restart mock server
tilt restart mock-rsocket-server

# Check for port conflicts
lsof -i :7000

# Verify container image availability
docker pull artifactory.sha.ao.arc-one.com/docker/biopro/utils/rsocket_mock:21.0.1
```

### 2. Order Data Not Retrieved

**Symptoms:**
- Exception records created without `order_received` data
- `order_retrieval_attempted` is false
- No RSocket call logs

**Diagnosis:**
```bash
# Check if mock server is enabled
curl -H @auth_header.txt "http://localhost:8080/actuator/configprops" | grep -A 10 rsocket

# Check application configuration
kubectl get configmap interface-exception-collector-config -o yaml

# Check for configuration validation errors
kubectl logs deployment/interface-exception-collector | grep "Configuration validation"
```

**Solutions:**
```bash
# Enable mock server in configuration
kubectl patch configmap interface-exception-collector-config --patch '{"data":{"application.yml":"app:\n  rsocket:\n    mock-server:\n      enabled: true"}}'

# Restart application to pick up config changes
kubectl rollout restart deployment/interface-exception-collector

# Verify configuration is loaded
curl -H @auth_header.txt "http://localhost:8080/actuator/health/rsocket"
```

### 3. Circuit Breaker Open

**Symptoms:**
- "Circuit breaker is open" errors in logs
- RSocket calls failing immediately
- Metrics show circuit breaker in OPEN state

**Diagnosis:**
```bash
# Check circuit breaker state
curl -H @auth_header.txt "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state"

# Check failure rate
curl -H @auth_header.txt "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.failure.rate"

# Check recent call results
kubectl logs deployment/interface-exception-collector | grep "Circuit breaker"
```

**Solutions:**
```bash
# Wait for circuit breaker to recover (default: 30s)
# Or restart the application to reset circuit breaker
kubectl rollout restart deployment/interface-exception-collector

# Adjust circuit breaker settings for development
kubectl patch configmap interface-exception-collector-config --patch '{"data":{"application.yml":"resilience4j:\n  circuitbreaker:\n    instances:\n      mock-rsocket-server:\n        failure-rate-threshold: 80\n        wait-duration-in-open-state: 10s"}}'
```

## Configuration Problems

### 1. Mock Server Enabled in Production

**Error:**
```
Configuration validation failed: Mock RSocket server cannot be enabled in production environment
```

**Solution:**
```yaml
# Ensure production configuration disables mock server
app:
  rsocket:
    mock-server:
      enabled: false
    partner-order-service:
      enabled: true
```

### 2. Invalid Host/Port Configuration

**Error:**
```
Invalid RSocket configuration: port must be between 1 and 65535
```

**Solution:**
```yaml
app:
  rsocket:
    mock-server:
      host: localhost  # Valid hostname or IP
      port: 7000      # Valid port number
```

### 3. Conflicting Service Configuration

**Warning:**
```
Both mock server and partner service are enabled
```

**Solution:**
```yaml
# Enable only one service at a time
app:
  rsocket:
    mock-server:
      enabled: true
    partner-order-service:
      enabled: false
```

## Connection Issues

### 1. Connection Timeout

**Symptoms:**
- "Connection timeout" errors
- Long delays before failures
- Intermittent connection issues

**Diagnosis:**
```bash
# Test direct connection to mock server
kubectl port-forward svc/mock-rsocket-server 7000:7000
# In another terminal:
telnet localhost 7000

# Check network policies
kubectl get networkpolicies

# Check service endpoints
kubectl get endpoints mock-rsocket-server
```

**Solutions:**
```bash
# Increase connection timeout
kubectl patch configmap interface-exception-collector-config --patch '{"data":{"application.yml":"app:\n  rsocket:\n    mock-server:\n      connection-timeout: 20s"}}'

# Check for network issues
kubectl exec -it deployment/interface-exception-collector -- nslookup mock-rsocket-server

# Restart both services
tilt restart mock-rsocket-server
tilt restart interface-exception-collector
```

### 2. DNS Resolution Issues

**Error:**
```
java.net.UnknownHostException: mock-rsocket-server
```

**Solutions:**
```bash
# Check DNS resolution
kubectl exec -it deployment/interface-exception-collector -- nslookup mock-rsocket-server

# Use IP address instead of hostname
kubectl get svc mock-rsocket-server -o jsonpath='{.spec.clusterIP}'

# Check service discovery
kubectl get svc -l app=mock-rsocket-server
```

### 3. Port Forwarding Issues

**Symptoms:**
- Local development connection failures
- Port already in use errors

**Solutions:**
```bash
# Check port usage
lsof -i :7000

# Kill conflicting processes
kill -9 $(lsof -t -i:7000)

# Use different port
tilt up -- --mock-server-port=7001
```

## Mapping and Response Issues

### 1. Mapping Files Not Loaded

**Symptoms:**
- All requests return default fallback response
- Specific test patterns not working
- 404 for valid order IDs

**Diagnosis:**
```bash
# Check mapping files in container
kubectl exec -it deployment/mock-rsocket-server -- ls -la /app/mappings/

# Check mapping file content
kubectl exec -it deployment/mock-rsocket-server -- cat /app/mappings/order-success-mapping.json

# Check container logs for mapping errors
kubectl logs deployment/mock-rsocket-server | grep -i mapping
```

**Solutions:**
```bash
# Reload mapping files
tilt trigger reload-mock-mappings

# Check ConfigMap content
kubectl get configmap mock-rsocket-mappings -o yaml

# Manually update mappings
kubectl create configmap mock-rsocket-mappings --from-file=mappings/ --dry-run=client -o yaml | kubectl apply -f -
```

### 2. Response Files Missing

**Error:**
```
Response file not found: orders/complete-order-with-items.json
```

**Solutions:**
```bash
# Check response files
kubectl exec -it deployment/mock-rsocket-server -- ls -la /app/__files/

# Update response files ConfigMap
kubectl create configmap mock-rsocket-responses --from-file=mock-responses/ --dry-run=client -o yaml | kubectl apply -f -

# Restart mock server to reload files
kubectl rollout restart deployment/mock-rsocket-server
```

### 3. Invalid JSON in Response Files

**Error:**
```
JSON parse error in response file
```

**Solutions:**
```bash
# Validate JSON files locally
find mock-responses/ -name "*.json" -exec jq . {} \;

# Check specific file
jq . mock-responses/orders/complete-order-with-items.json

# Fix JSON syntax and reload
tilt trigger reload-mock-mappings
```

## Performance Issues

### 1. Slow Response Times

**Symptoms:**
- High RSocket call duration metrics
- Timeout warnings in logs
- Poor application performance

**Diagnosis:**
```bash
# Check RSocket metrics
curl -H @auth_header.txt "http://localhost:8080/actuator/metrics/rsocket.call.duration"

# Check mock server resource usage
kubectl top pod -l app=mock-rsocket-server

# Check network latency
kubectl exec -it deployment/interface-exception-collector -- ping mock-rsocket-server
```

**Solutions:**
```bash
# Increase mock server resources
kubectl patch deployment mock-rsocket-server -p '{"spec":{"template":{"spec":{"containers":[{"name":"mock-rsocket-server","resources":{"limits":{"memory":"512Mi","cpu":"500m"}}}]}}}}'

# Optimize timeout settings
kubectl patch configmap interface-exception-collector-config --patch '{"data":{"application.yml":"app:\n  rsocket:\n    mock-server:\n      timeout: 10s"}}'

# Enable connection pooling
kubectl patch configmap interface-exception-collector-config --patch '{"data":{"application.yml":"app:\n  rsocket:\n    mock-server:\n      keep-alive-interval: 30s"}}'
```

### 2. Memory Issues

**Symptoms:**
- OutOfMemoryError in mock server
- Pod restarts due to memory limits
- High memory usage metrics

**Solutions:**
```bash
# Increase memory limits
kubectl patch deployment mock-rsocket-server -p '{"spec":{"template":{"spec":{"containers":[{"name":"mock-rsocket-server","resources":{"limits":{"memory":"1Gi"}}}]}}}}'

# Check for memory leaks
kubectl exec -it deployment/mock-rsocket-server -- ps aux

# Monitor memory usage
kubectl top pod mock-rsocket-server --containers
```

## Debugging Tools

### 1. Enable Debug Logging

```bash
# Enable RSocket debug logging
kubectl patch deployment interface-exception-collector -p '{"spec":{"template":{"spec":{"containers":[{"name":"interface-exception-collector","env":[{"name":"LOGGING_LEVEL_IO_RSOCKET","value":"DEBUG"}]}]}}}}'

# Enable application debug logging
kubectl patch deployment interface-exception-collector -p '{"spec":{"template":{"spec":{"containers":[{"name":"interface-exception-collector","env":[{"name":"LOGGING_LEVEL_COM_ARCONE_BIOPRO_EXCEPTION_COLLECTOR_INFRASTRUCTURE_CLIENT","value":"DEBUG"}]}]}}}}'

# Check debug logs
kubectl logs -f deployment/interface-exception-collector | grep -i rsocket
```

### 2. Test RSocket Connection Manually

```bash
# Port forward to mock server
kubectl port-forward svc/mock-rsocket-server 7000:7000

# Test with RSocket CLI (if available)
rsc --request --route=orders.TEST-ORDER-1 --data='{}' tcp://localhost:7000

# Test with curl (HTTP endpoint if available)
curl -X POST http://localhost:7000/orders/TEST-ORDER-1
```

### 3. Monitor Circuit Breaker

```bash
# Watch circuit breaker state
watch -n 1 'curl -s -H @auth_header.txt "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state" | jq'

# Monitor call success rate
watch -n 1 'curl -s -H @auth_header.txt "http://localhost:8080/actuator/metrics/rsocket.calls.total" | jq'
```

## Monitoring and Health Checks

### 1. Health Check Endpoints

```bash
# Overall application health
curl -H @auth_header.txt "http://localhost:8080/actuator/health"

# RSocket-specific health
curl -H @auth_header.txt "http://localhost:8080/actuator/health/rsocket"

# Detailed health information
curl -H @auth_header.txt "http://localhost:8080/actuator/health?show-details=always"
```

### 2. Metrics Monitoring

```bash
# RSocket call metrics
curl -H @auth_header.txt "http://localhost:8080/actuator/metrics/rsocket.calls.total"
curl -H @auth_header.txt "http://localhost:8080/actuator/metrics/rsocket.call.duration"
curl -H @auth_header.txt "http://localhost:8080/actuator/metrics/rsocket.errors.total"

# Circuit breaker metrics
curl -H @auth_header.txt "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state"
curl -H @auth_header.txt "http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.failure.rate"

# Retry metrics
curl -H @auth_header.txt "http://localhost:8080/actuator/metrics/resilience4j.retry.calls"
```

### 3. Log Analysis

```bash
# Search for RSocket-related logs
kubectl logs deployment/interface-exception-collector | grep -i rsocket

# Search for circuit breaker events
kubectl logs deployment/interface-exception-collector | grep -i "circuit breaker"

# Search for order retrieval attempts
kubectl logs deployment/interface-exception-collector | grep "order data retrieval"

# Follow logs in real-time
kubectl logs -f deployment/interface-exception-collector | grep -E "(rsocket|order.*retrieval)"
```

## Environment-Specific Issues

### 1. Development Environment

**Common Issues:**
- Localhost connection issues
- Port conflicts with other services
- Hot reload not working

**Solutions:**
```bash
# Use Tilt for development
tilt up

# Check for port conflicts
lsof -i :7000 -i :8080

# Restart development environment
tilt down && tilt up
```

### 2. Test Environment

**Common Issues:**
- Service discovery problems
- Network policies blocking connections
- Resource constraints

**Solutions:**
```bash
# Check service discovery
kubectl get svc -n test-namespace

# Check network policies
kubectl get networkpolicies -n test-namespace

# Increase resource limits
kubectl patch deployment mock-rsocket-server -n test-namespace -p '{"spec":{"template":{"spec":{"containers":[{"name":"mock-rsocket-server","resources":{"limits":{"memory":"1Gi","cpu":"500m"}}}]}}}}'
```

### 3. Production Environment

**Expected Behavior:**
- Mock server should be disabled
- Partner service should be enabled
- Configuration validation should prevent mock server usage

**Verification:**
```bash
# Verify mock server is disabled
kubectl get deployment mock-rsocket-server -n production-namespace
# Should return: Error from server (NotFound)

# Verify configuration
kubectl get configmap interface-exception-collector-config -n production-namespace -o yaml | grep -A 5 rsocket
```

## Quick Reference Commands

### Restart Services
```bash
# Restart mock server
tilt restart mock-rsocket-server

# Restart application
tilt restart interface-exception-collector

# Restart all services
tilt down && tilt up
```

### Check Status
```bash
# Service status
kubectl get pods -l app=mock-rsocket-server
kubectl get pods -l app=interface-exception-collector

# Health checks
curl -H @auth_header.txt "http://localhost:8080/actuator/health/rsocket"

# Configuration
kubectl get configmap interface-exception-collector-config -o yaml
```

### Debug Information
```bash
# Enable debug logging
kubectl patch deployment interface-exception-collector -p '{"spec":{"template":{"spec":{"containers":[{"name":"interface-exception-collector","env":[{"name":"LOGGING_LEVEL_COM_ARCONE_BIOPRO_EXCEPTION_COLLECTOR_INFRASTRUCTURE","value":"DEBUG"}]}]}}}}'

# View debug logs
kubectl logs -f deployment/interface-exception-collector | grep DEBUG

# Check metrics
curl -H @auth_header.txt "http://localhost:8080/actuator/prometheus" | grep rsocket
```

This troubleshooting guide covers the most common issues and their solutions. For additional support, check the application logs and metrics for specific error messages and patterns.