# Mock RSocket Server Monitoring Implementation Summary

## Overview

This document summarizes the implementation of monitoring and health checks for the Mock RSocket Server integration as part of task 7 from the mock-rsocket-server-integration specification.

## Implemented Components

### 1. MockRSocketServerHealthIndicator

**Location**: `src/main/java/com/arcone/biopro/exception/collector/infrastructure/health/MockRSocketServerHealthIndicator.java`

**Purpose**: Provides health check functionality for the Mock RSocket Server connectivity.

**Key Features**:
- Performs health checks by making test RSocket calls to the mock server
- Returns structured health status information
- Provides both detailed health status map and simple boolean health check
- Includes timeout handling (2-second timeout for health checks)
- Only active when `app.rsocket.mock-server.enabled=true`

**Methods**:
- `checkHealth()`: Returns detailed health status as Map
- `isHealthy()`: Returns simple boolean health status
- `performHealthCheck()`: Internal method that performs the actual health check call

### 2. RSocketMetrics

**Location**: `src/main/java/com/arcone/biopro/exception/collector/infrastructure/metrics/RSocketMetrics.java`

**Purpose**: Collects and records Prometheus metrics for RSocket interactions with the mock server.

**Key Features**:
- Records successful and failed RSocket calls with duration tracking
- Tracks timeout events and circuit breaker events
- Provides success rate calculation
- Integrates with Micrometer for Prometheus export
- Only active when `app.rsocket.mock-server.enabled=true`

**Metrics Collected**:
- `rsocket.calls.total`: Total number of RSocket calls
- `rsocket.calls.success`: Number of successful calls
- `rsocket.calls.failure`: Number of failed calls
- `rsocket.call.duration`: Duration of RSocket calls (Timer)
- `rsocket.errors.total`: Total errors by type
- `rsocket.timeouts.total`: Number of timeout events
- `rsocket.circuit_breaker.events`: Circuit breaker events

**Methods**:
- `recordSuccessfulCall()`: Records successful calls with duration
- `recordFailedCall()`: Records failed calls with error details
- `recordTimeout()`: Records timeout events
- `recordCircuitBreakerEvent()`: Records circuit breaker events
- `recordError()`: Records generic errors
- `getSuccessRate()`: Calculates current success rate percentage

### 3. RSocketLoggingInterceptor

**Location**: `src/main/java/com/arcone/biopro/exception/collector/infrastructure/logging/RSocketLoggingInterceptor.java`

**Purpose**: Provides structured logging with correlation IDs for all RSocket interactions.

**Key Features**:
- Generates unique correlation IDs for each RSocket call
- Uses MDC (Mapped Diagnostic Context) for structured logging
- Logs connection events, health checks, and configuration changes
- Supports retry attempt logging and circuit breaker events
- Only active when `app.rsocket.mock-server.enabled=true`

**Logging Methods**:
- `logRSocketCallStart()`: Logs call initiation with correlation ID
- `logRSocketCallSuccess()`: Logs successful call completion
- `logRSocketCallFailure()`: Logs failed calls with error details
- `logRSocketTimeout()`: Logs timeout events
- `logCircuitBreakerEvent()`: Logs circuit breaker state changes
- `logRetryAttempt()`: Logs retry attempts
- `logConnectionEvent()`: Logs connection lifecycle events
- `logHealthCheck()`: Logs health check results
- `logConfigurationEvent()`: Logs configuration changes

**MDC Fields Used**:
- `rsocket.correlation_id`: Unique correlation ID for tracing
- `rsocket.external_id`: Order external ID being processed
- `rsocket.operation`: Type of operation (e.g., GET_ORDER_DATA)
- `rsocket.duration_ms`: Call duration in milliseconds
- `rsocket.success`: Boolean success indicator
- `rsocket.service`: Always "mock-rsocket-server"
- `rsocket.transaction_id`: Transaction ID from the exception

### 4. MockRSocketOrderServiceClient Integration

**Location**: `src/main/java/com/arcone/biopro/exception/collector/infrastructure/client/MockRSocketOrderServiceClient.java`

**Updates Made**:
- Integrated with RSocketMetrics for automatic metrics collection
- Integrated with RSocketLoggingInterceptor for structured logging
- Added correlation ID tracking for all RSocket calls
- Enhanced connection lifecycle logging
- Added metrics recording for successful/failed calls, timeouts, and circuit breaker events

**Integration Points**:
- Connection initialization and shutdown events are logged
- All RSocket calls are wrapped with metrics and logging
- Circuit breaker fallback methods record appropriate metrics
- Timeout and error scenarios are properly tracked

## Configuration Updates

### Application Properties

**Development Environment** (`application-dev.yml`):
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,info
  endpoint:
    health:
      show-details: always
      show-components: always
  health:
    rsocket:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      environment: development
      service: interface-exception-collector

logging:
  level:
    com.arcone.biopro.exception.collector.infrastructure.health: DEBUG
    com.arcone.biopro.exception.collector.infrastructure.metrics: DEBUG
    com.arcone.biopro.exception.collector.infrastructure.logging: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId:-}] [%X{rsocket.correlation_id:-}] %logger{36} - %msg%n"
```

**Test Environment** (`application-test.yml`):
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
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      environment: test
      service: interface-exception-collector
```

**Production Environment** (`application-prod.yml`):
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  health:
    rsocket:
      enabled: false  # Disabled since mock server not used in production
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      environment: production
      service: interface-exception-collector
```

## Testing

### Unit Tests Created

1. **MockRSocketServerHealthIndicatorTest**
   - Tests health check functionality with various scenarios
   - Verifies proper status reporting for healthy/unhealthy states
   - Tests timeout and error handling

2. **RSocketMetricsTest**
   - Tests all metric recording methods
   - Verifies metric counters and timers work correctly
   - Tests success rate calculation

3. **RSocketLoggingInterceptorTest**
   - Tests structured logging with MDC context
   - Verifies correlation ID generation and tracking
   - Tests all logging scenarios (success, failure, timeout, etc.)

## Monitoring Endpoints

When the application is running with monitoring enabled, the following endpoints are available:

- **Health Check**: `/actuator/health` - Shows overall application health including RSocket server status
- **Metrics**: `/actuator/metrics` - Shows all collected metrics
- **Prometheus**: `/actuator/prometheus` - Prometheus-formatted metrics for scraping

## Key Benefits

1. **Observability**: Complete visibility into RSocket call performance and reliability
2. **Troubleshooting**: Correlation IDs enable end-to-end request tracing
3. **Monitoring**: Prometheus metrics enable alerting and dashboards
4. **Health Checks**: Container orchestration can monitor service health
5. **Performance Tracking**: Duration metrics help identify performance issues
6. **Error Analysis**: Detailed error categorization and logging

## Requirements Satisfied

This implementation satisfies the following requirements from the specification:

- **8.1**: Health check endpoints for container orchestration ✅
- **8.2**: Custom metrics for RSocket call tracking (success rate, duration, errors) ✅
- **8.5**: Structured logging with correlation IDs for mock server interactions ✅
- **7.4**: RSocketMetrics component for Prometheus metrics collection ✅

## Usage

The monitoring components are automatically activated when:
1. `app.rsocket.mock-server.enabled=true` is set in configuration
2. The MockRSocketOrderServiceClient is used for RSocket calls
3. Spring Boot Actuator is enabled in the application

All monitoring data is automatically collected and exposed through standard Spring Boot Actuator endpoints, making it easy to integrate with existing monitoring infrastructure.