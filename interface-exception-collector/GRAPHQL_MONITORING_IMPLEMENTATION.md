# GraphQL Monitoring and Observability Implementation

This document summarizes the monitoring and observability components implemented for the GraphQL API in task 18.

## Components Implemented

### 1. GraphQLMetrics Class
**Location:** `src/main/java/com/arcone/biopro/exception/collector/api/graphql/config/GraphQLMetrics.java`

**Features:**
- Comprehensive metrics collection for GraphQL operations
- Query, mutation, and subscription performance tracking
- Field-level fetch time monitoring
- Error rate and complexity tracking
- Cache hit/miss metrics
- DataLoader batch performance metrics
- Correlation ID generation for request tracking
- Integration with Micrometer for Prometheus export

**Key Metrics:**
- `graphql.query.count` - Total GraphQL queries executed
- `graphql.query.duration` - Query execution time distribution
- `graphql.error.count` - GraphQL errors by type
- `graphql.field.fetch.duration` - Field resolver performance
- `graphql.cache.access` - Cache hit/miss rates
- `graphql.dataloader.batch` - DataLoader batch performance

### 2. GraphQLHealthIndicator Class
**Location:** `src/main/java/com/arcone/biopro/exception/collector/api/graphql/config/GraphQLHealthIndicator.java`

**Features:**
- Database connectivity health checks with performance monitoring
- Redis cache connectivity and response time monitoring
- GraphQL schema validation health checks
- API performance metrics collection
- System resource monitoring (memory, CPU)
- Detailed health status reporting

**Health Checks:**
- Database response time monitoring (threshold: 1000ms)
- Cache response time monitoring (threshold: 100ms)
- Recent exception processing metrics
- Pending retry operations count

### 3. GraphQLLoggingConfig Class
**Location:** `src/main/java/com/arcone/biopro/exception/collector/api/graphql/config/GraphQLLoggingConfig.java`

**Features:**
- Structured logging with correlation IDs
- Operation start/completion logging
- Error logging with detailed context
- MDC (Mapped Diagnostic Context) setup for request tracking
- Configurable query and variable logging (disabled by default for security)
- Performance warning logging for slow operations
- Audit event logging capabilities

**Logging Context:**
- `correlationId` - Unique request identifier
- `graphql.operation` - Operation name
- `graphql.operationType` - Query/Mutation/Subscription
- `graphql.userId` - User identifier (when available)
- `graphql.executionId` - Execution identifier

### 4. GraphQLMicrometerConfig Class
**Location:** `src/main/java/com/arcone/biopro/exception/collector/api/graphql/config/GraphQLMicrometerConfig.java`

**Features:**
- Prometheus meter registry configuration
- Custom meter filters for GraphQL metrics
- Common tags for all metrics (application, environment, version)
- Histogram bucket configuration for timing metrics
- Alerting threshold configuration as gauges
- Metric renaming and organization

**Alerting Thresholds:**
- Query response time: 500ms (P95)
- Mutation response time: 3000ms (P95)
- Field fetch time: 100ms (P95)
- Error rate: 5%
- Cache miss rate: 20%

### 5. GraphQLAlertingConfig Class
**Location:** `src/main/java/com/arcone/biopro/exception/collector/api/graphql/config/GraphQLAlertingConfig.java`

**Features:**
- Automated alerting based on metric thresholds
- Scheduled metric monitoring (every 30 seconds)
- Alert cooldown logic (5 minutes)
- Multiple alert types (performance, errors, cache, throughput)
- Health indicator integration
- Alert state tracking and reporting

**Alert Types:**
- Query/Mutation response time alerts
- Error rate threshold alerts
- Cache miss rate alerts
- High throughput alerts
- Health check failure alerts

### 6. Configuration Updates

#### Application Configuration (`application.yml`)
Added comprehensive monitoring configuration:
```yaml
graphql:
  monitoring:
    metrics:
      enabled: true
      detailed-field-metrics: false
      cache-metrics: true
      dataloader-metrics: true
    alerting:
      enabled: true
      check-interval-seconds: 30
      cooldown-minutes: 5
      thresholds:
        query-response-time-ms: 500
        mutation-response-time-ms: 3000
        error-rate-percent: 5.0
        cache-miss-rate-percent: 20.0
    health-checks:
      enabled: true
      database-timeout-ms: 1000
      cache-timeout-ms: 100
    logging:
      structured: true
      correlation-id: true
      performance-warnings: true
```

#### Management Endpoints
Extended Actuator endpoints:
- `/actuator/health/graphql` - GraphQL-specific health checks
- `/actuator/metrics` - All GraphQL metrics
- `/actuator/prometheus` - Prometheus format metrics

### 7. Monitoring Infrastructure

#### Prometheus Alerting Rules
**Location:** `monitoring/graphql-alerting-rules.yml`

Comprehensive alerting rules for:
- Performance monitoring (response times, throughput)
- Error rate monitoring
- Cache performance monitoring
- Health check monitoring
- Security monitoring (rate limiting, complexity)
- Business metrics monitoring

#### Grafana Dashboard
**Location:** `monitoring/grafana/dashboards/graphql-monitoring-dashboard.json`

Complete monitoring dashboard with:
- Query performance metrics and trends
- Error rate and breakdown visualization
- Cache performance monitoring
- DataLoader performance tracking
- Field-level performance analysis
- Health status indicators
- Alert status monitoring
- Business metrics visualization

### 8. Integration with Existing GraphQL Configuration

Updated `GraphQLConfig.java` to include monitoring instrumentation:
- Added GraphQLMetrics instrumentation
- Added GraphQLLoggingConfig instrumentation
- Integrated with existing security and rate limiting

## Usage

### Accessing Metrics
1. **Prometheus Format:** `GET /actuator/prometheus`
2. **JSON Format:** `GET /actuator/metrics`
3. **Health Checks:** `GET /actuator/health`
4. **GraphQL Health:** `GET /actuator/health/graphql`

### Viewing Logs
Structured logs include correlation IDs and operation context:
```json
{
  "timestamp": "2025-08-18T19:00:00.000Z",
  "level": "INFO",
  "message": "GraphQL operation completed",
  "correlationId": "abc12345",
  "graphql.operation": "exceptions",
  "graphql.operationType": "query",
  "durationMs": 150
}
```

### Monitoring Alerts
Alerts are automatically triggered when thresholds are exceeded:
- Response time alerts for slow operations
- Error rate alerts for high failure rates
- Cache performance alerts for poor hit rates
- Health check alerts for service issues

## Performance Impact

The monitoring implementation is designed for minimal performance impact:
- Asynchronous metric collection
- Efficient correlation ID generation
- Configurable logging levels
- Optimized health check intervals
- Cached metric calculations

## Security Considerations

- Query and variable logging disabled by default
- Sensitive field detection and redaction
- Correlation IDs for audit trails
- Security event logging
- Rate limiting monitoring

## Testing

Comprehensive test coverage includes:
- Metrics collection verification
- Health check functionality
- Alerting threshold testing
- Configuration validation
- Integration testing with existing components

The monitoring system provides complete observability for the GraphQL API while maintaining high performance and security standards.