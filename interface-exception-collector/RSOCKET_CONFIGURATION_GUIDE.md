# RSocket Configuration Guide

This guide provides comprehensive documentation for configuring the Mock RSocket Server integration in the BioPro Interface Exception Collector service.

## Overview

The RSocket integration enables the Interface Exception Collector to retrieve order data from a mock RSocket server during OrderRejected event processing. This integration supports development and testing workflows without dependencies on external Partner Order Service instances.

## Configuration Structure

### Application Properties Structure

```yaml
app:
  rsocket:
    mock-server:
      enabled: true                           # Enable/disable mock server integration
      host: localhost                         # Mock server hostname or IP
      port: 7000                             # Mock server port
      timeout: 5s                            # Request timeout
      connection-timeout: 10s                # Connection establishment timeout
      keep-alive-interval: 30s               # Keep-alive ping interval
      keep-alive-max-lifetime: 300s          # Maximum connection lifetime
      debug-logging: false                   # Enable debug logging
      circuit-breaker:                       # Circuit breaker configuration
        enabled: true
        failure-rate-threshold: 50           # Failure rate percentage (1-100)
        wait-duration-in-open-state: 30s     # Wait time when circuit is open
        sliding-window-size: 10              # Number of calls in sliding window
        minimum-number-of-calls: 5           # Minimum calls before circuit evaluation
        permitted-calls-in-half-open: 3      # Calls allowed in half-open state
      retry:                                 # Retry configuration
        enabled: true
        max-attempts: 3                      # Maximum retry attempts
        wait-duration: 1s                    # Initial wait between retries
        exponential-backoff-multiplier: 2.0  # Backoff multiplier
    partner-order-service:                   # Production service configuration
      enabled: false                         # Enable/disable partner service
      host: partner-order-service            # Partner service hostname
      port: 8090                            # Partner service port
      timeout: 30s                          # Request timeout
      connection-timeout: 20s               # Connection timeout

  features:                                  # Feature flags
    enhanced-logging: true                   # Enable enhanced logging
    debug-mode: false                        # Enable debug mode
    payload-caching: true                    # Enable payload caching
    circuit-breaker: true                    # Enable circuit breaker
    retry-mechanism: true                    # Enable retry mechanism
    metrics-collection: true                 # Enable metrics collection
    audit-logging: true                      # Enable audit logging
```

## Environment-Specific Configuration

### Development Environment (application-dev.yml)

```yaml
spring:
  profiles:
    active: dev

app:
  rsocket:
    mock-server:
      enabled: true
      host: localhost
      port: 7000
      timeout: 5s
      connection-timeout: 10s
      debug-logging: true
      circuit-breaker:
        failure-rate-threshold: 30          # More lenient for development
        wait-duration-in-open-state: 10s    # Shorter wait for faster cycles
        sliding-window-size: 5
        minimum-number-of-calls: 3
        permitted-calls-in-half-open: 2
      retry:
        max-attempts: 2                      # Fewer retries for faster feedback
        wait-duration: 500ms
        exponential-backoff-multiplier: 1.5
    partner-order-service:
      enabled: false

  features:
    enhanced-logging: true
    debug-mode: true
    payload-caching: true
    circuit-breaker: true
    retry-mechanism: true
    metrics-collection: true
    audit-logging: false                     # Reduced logging for development

logging:
  level:
    com.arcone.biopro.exception.collector.infrastructure.client: DEBUG
    com.arcone.biopro.exception.collector.infrastructure.config: DEBUG
    io.rsocket: DEBUG
```

### Test Environment (application-test.yml)

```yaml
spring:
  profiles:
    active: test

app:
  rsocket:
    mock-server:
      enabled: true
      host: mock-rsocket-server              # Service name in test environment
      port: 7000
      timeout: 10s                           # Longer timeout for test stability
      connection-timeout: 15s
      debug-logging: false
      circuit-breaker:
        failure-rate-threshold: 40
        wait-duration-in-open-state: 15s
        sliding-window-size: 8
        minimum-number-of-calls: 4
        permitted-calls-in-half-open: 3
      retry:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2
    partner-order-service:
      enabled: false

  features:
    enhanced-logging: true
    debug-mode: false
    payload-caching: true
    circuit-breaker: true
    retry-mechanism: true
    metrics-collection: true
    audit-logging: true

logging:
  level:
    com.arcone.biopro.exception.collector: INFO
    com.arcone.biopro.exception.collector.infrastructure.client: DEBUG
    io.rsocket: WARN
```

### Production Environment (application-prod.yml)

```yaml
spring:
  profiles:
    active: prod

app:
  rsocket:
    mock-server:
      enabled: false                         # DISABLED in production
    partner-order-service:
      enabled: true                          # ENABLED in production
      host: partner-order-service
      port: 8090
      timeout: 30s                          # Longer timeout for production
      connection-timeout: 20s

  features:
    enhanced-logging: false
    debug-mode: false
    payload-caching: true
    circuit-breaker: true
    retry-mechanism: true
    metrics-collection: true
    audit-logging: true

logging:
  level:
    com.arcone.biopro.exception.collector: INFO
    com.arcone.biopro.exception.collector.infrastructure.client: WARN
    io.rsocket: ERROR
```

## Configuration Validation

The system performs comprehensive configuration validation at startup:

### Validation Rules

1. **Environment Safety**
   - Mock server cannot be enabled in production environments
   - Debug mode warnings in production
   - Required environment variables validation

2. **Connection Parameters**
   - Host: Must be valid hostname or IP address
   - Port: Must be between 1 and 65535
   - Timeouts: Must be positive durations
   - Connection timeout: Must be positive and reasonable

3. **Circuit Breaker Settings**
   - Failure rate threshold: 1-100%
   - Wait duration: Must be positive
   - Sliding window size: Must be positive
   - Minimum calls: Must be positive

4. **Retry Settings**
   - Max attempts: Must be positive (recommended: 1-10)
   - Wait duration: Must be non-negative
   - Backoff multiplier: Must be >= 1.0

### Validation Errors vs Warnings

**Errors (Application fails to start):**
- Invalid host/port combinations
- Null or negative timeouts
- Mock server enabled in production
- Invalid circuit breaker parameters

**Warnings (Application starts with warnings):**
- Suboptimal timeout values
- Debug mode in production
- Both mock and partner service enabled
- Very high retry attempts

## Error Handling and Fallback Mechanisms

### Connection Failure Handling

1. **Initial Connection Failure**
   - Application starts in fallback mode
   - Order data retrieval is skipped
   - Exception processing continues normally
   - Automatic reconnection attempts

2. **Runtime Connection Loss**
   - Automatic reconnection with exponential backoff
   - Circuit breaker protection
   - Graceful degradation to fallback mode
   - Comprehensive error logging

3. **Timeout Handling**
   - Configurable request timeouts
   - Connection establishment timeouts
   - Keep-alive settings for connection health
   - Timeout-specific error reporting

### Fallback Strategies

1. **Service Unavailable**
   - Exception records created without order data
   - Marked as non-retryable initially
   - Can be retried manually when service recovers

2. **Circuit Breaker Open**
   - Fast-fail for subsequent requests
   - Automatic recovery attempts
   - Detailed circuit breaker state logging

3. **Configuration Errors**
   - Application startup failure for critical errors
   - Warning logs for non-critical issues
   - Validation error details in logs

## Monitoring and Health Checks

### Health Check Configuration

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  health:
    rsocket:
      enabled: true                          # Enable RSocket health checks
```

### Metrics Configuration

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      environment: ${spring.profiles.active}
      service: interface-exception-collector
```

### Available Metrics

- `rsocket.calls.total` - Total RSocket calls made
- `rsocket.call.duration` - RSocket call duration
- `rsocket.errors.total` - RSocket call errors
- `rsocket.circuit.breaker.state` - Circuit breaker state
- `rsocket.connection.status` - Connection status

## Troubleshooting

### Common Configuration Issues

1. **Connection Refused**
   ```
   Error: Connection refused to localhost:7000
   Solution: Ensure mock server is running and port is correct
   ```

2. **Timeout Issues**
   ```
   Error: Request timeout after 5s
   Solution: Increase timeout or check server performance
   ```

3. **Circuit Breaker Open**
   ```
   Error: Circuit breaker is open
   Solution: Wait for recovery or check server health
   ```

4. **Invalid Configuration**
   ```
   Error: Mock server port must be between 1 and 65535
   Solution: Fix port configuration in application.yml
   ```

### Debug Configuration

Enable debug logging for troubleshooting:

```yaml
logging:
  level:
    com.arcone.biopro.exception.collector.infrastructure.client: DEBUG
    com.arcone.biopro.exception.collector.infrastructure.config: DEBUG
    com.arcone.biopro.exception.collector.infrastructure.health: DEBUG
    io.rsocket: DEBUG
    reactor.netty: INFO
```

### Health Check Endpoints

- `/actuator/health` - Overall application health
- `/actuator/health/rsocket` - RSocket connection health
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

## Best Practices

### Development Environment

1. Use shorter timeouts for faster feedback
2. Enable debug logging
3. Use lenient circuit breaker settings
4. Enable hot reload features

### Test Environment

1. Use service names instead of localhost
2. Increase timeouts for stability
3. Enable comprehensive logging
4. Use realistic circuit breaker settings

### Production Environment

1. Disable mock server completely
2. Use conservative timeout values
3. Enable audit logging
4. Monitor circuit breaker metrics
5. Set up proper alerting

### Security Considerations

1. Never enable mock server in production
2. Validate configuration at startup
3. Use secure communication channels
4. Implement proper access controls
5. Monitor for configuration drift

## Example Configurations

### Minimal Development Configuration

```yaml
app:
  rsocket:
    mock-server:
      enabled: true
      host: localhost
      port: 7000
```

### Comprehensive Test Configuration

```yaml
app:
  rsocket:
    mock-server:
      enabled: true
      host: mock-rsocket-server
      port: 7000
      timeout: 10s
      connection-timeout: 15s
      circuit-breaker:
        failure-rate-threshold: 40
        wait-duration-in-open-state: 15s
      retry:
        max-attempts: 3
        wait-duration: 1s
```

### Production Safety Configuration

```yaml
app:
  rsocket:
    mock-server:
      enabled: false
    partner-order-service:
      enabled: true
      host: partner-order-service
      port: 8090
      timeout: 30s
```

This configuration guide ensures proper setup and operation of the RSocket integration across all environments while maintaining security and reliability standards.