# Dynatrace Integration Guide

## Overview

The Interface Exception Collector service is comprehensively instrumented with Dynatrace to provide deep observability into both technical performance and business metrics. This integration goes beyond standard infrastructure monitoring to capture business-specific metrics that are critical for understanding the health and performance of interface exception management.

## Architecture

### Components

1. **DynatraceConfig** - Core configuration for Dynatrace SDK and meter registry
2. **DynatraceBusinessMetricsService** - Business-specific metrics collection
3. **DynatraceInstrumentationAspect** - Automatic instrumentation via AOP
4. **DynatraceIntegrationService** - Integration bridge with existing services
5. **DynatraceHealthIndicator** - Health monitoring and status reporting
6. **DynatraceBusinessMetricsEndpoint** - Custom actuator endpoint for metrics
7. **DynatraceHealthMonitoringService** - Scheduled health monitoring

### Integration Points

- **Exception Processing** - Captures metrics for all exception lifecycle events
- **GraphQL Operations** - Monitors query/mutation performance and usage
- **Kafka Message Processing** - Tracks message processing performance
- **Retry Operations** - Monitors retry success rates and patterns
- **Payload Retrieval** - Tracks external service call performance

## Business Metrics Captured

### Exception Lifecycle Metrics

| Metric | Description | Tags |
|--------|-------------|------|
| `biopro.exceptions.received` | Total exceptions received | interface_type, severity, customer_id, location_code |
| `biopro.exceptions.processed` | Total exceptions processed | interface_type, severity, status |
| `biopro.exceptions.resolved` | Total exceptions resolved | interface_type, severity, resolution_method |
| `biopro.exceptions.acknowledged` | Total exceptions acknowledged | interface_type, severity, acknowledged_by |

### Performance Metrics

| Metric | Description | Unit |
|--------|-------------|------|
| `biopro.exceptions.processing.duration` | Exception processing time | milliseconds |
| `biopro.exceptions.resolution.duration` | Time from creation to resolution | milliseconds |
| `biopro.exceptions.acknowledgment.duration` | Time from creation to acknowledgment | milliseconds |

### Retry Metrics

| Metric | Description | Tags |
|--------|-------------|------|
| `biopro.exceptions.retry.attempts` | Total retry attempts | attempt_number, success |
| `biopro.exceptions.retry.success` | Successful retry attempts | - |
| `biopro.exceptions.retry.failure` | Failed retry attempts | - |
| `biopro.exceptions.retry.duration` | Retry operation duration | - |

### Business State Metrics

| Metric | Description | Type |
|--------|-------------|------|
| `biopro.exceptions.pending.count` | Current pending exceptions | Gauge |
| `biopro.exceptions.failed.count` | Current failed exceptions | Gauge |
| `biopro.exceptions.resolved.count` | Current resolved exceptions | Gauge |
| `biopro.exceptions.severity.*.count` | Exceptions by severity level | Gauge |

### Payload Retrieval Metrics

| Metric | Description | Tags |
|--------|-------------|------|
| `biopro.exceptions.payload.retrieval` | Payload retrieval attempts | interface_type, success |
| `biopro.exceptions.payload.retrieval.failure` | Failed payload retrievals | interface_type |
| `biopro.exceptions.payload.retrieval.duration` | Payload retrieval time | - |

## Configuration

### Environment Variables

```bash
# Dynatrace Configuration
DYNATRACE_ENABLED=true
DYNATRACE_API_TOKEN=your_api_token
DYNATRACE_URI=https://your_tenant.live.dynatrace.com/api/v2/metrics/ingest
DYNATRACE_DEVICE_ID=interface-exception-collector

# Business Metrics Thresholds
DYNATRACE_CRITICAL_THRESHOLD=10
DYNATRACE_PENDING_THRESHOLD=100
DYNATRACE_RETRY_SUCCESS_THRESHOLD=70.0
DYNATRACE_PROCESSING_EFFICIENCY_THRESHOLD=80.0

# OpenTelemetry Configuration
OTEL_EXPORTER_OTLP_ENDPOINT=https://your_tenant.live.dynatrace.com/api/v2/otlp
OTEL_EXPORTER_OTLP_HEADERS=Authorization=Api-Token your_api_token
```

### Application Configuration

```yaml
dynatrace:
  enabled: true
  api-token: ${DYNATRACE_API_TOKEN}
  uri: ${DYNATRACE_URI}
  device-id: interface-exception-collector
  
  business-metrics:
    enabled: true
    exception-thresholds:
      critical-count: 10
      pending-count: 100
      retry-success-rate: 70.0
      processing-efficiency: 80.0
  
  custom-attributes:
    include-customer-id: true
    include-location-code: true
    include-external-id: true
    include-interface-type: true
    include-severity: true
```

## Custom Attributes

The integration automatically adds custom attributes to Dynatrace traces:

### Exception Attributes
- `transaction_id` - Unique transaction identifier
- `interface_type` - Type of interface (ORDER, COLLECTION, DISTRIBUTION)
- `severity` - Exception severity level
- `status` - Current exception status
- `customer_id` - Customer identifier (if available)
- `location_code` - Location code (if available)
- `external_id` - External system identifier

### Operation Attributes
- `operation` - Type of operation being performed
- `processing_time_ms` - Operation duration
- `retry_attempt_number` - Retry attempt number
- `retry_success` - Retry success status
- `query_name` - GraphQL query/mutation name
- `resolver_class` - GraphQL resolver class

## Health Monitoring

### Health Indicators

The service provides comprehensive health indicators accessible via:
- `/actuator/health` - Overall health status
- `/actuator/dynatrace` - Detailed Dynatrace metrics and status

### Health Thresholds

| Indicator | Warning Threshold | Critical Threshold |
|-----------|-------------------|-------------------|
| Critical Exceptions | > 10 | > 25 |
| Pending Exceptions | > 100 | > 500 |
| Retry Success Rate | < 70% | < 50% |
| Processing Efficiency | < 80% | < 60% |

### Scheduled Monitoring

- **Health Checks**: Every 5 minutes
- **Hourly Summary**: Every hour
- **Daily Cleanup**: 2 AM daily
- **Emergency Checks**: On-demand via API

## Business KPIs

### Exception Resolution Rate
```
Resolution Rate = (Resolved Exceptions / Total Exceptions) × 100%
```

### Critical Exception Ratio
```
Critical Ratio = (Critical Exceptions / Total Exceptions) × 100%
```

### Processing Efficiency
```
Processing Efficiency = (Processed Exceptions / Received Exceptions) × 100%
```

### Business Impact Score
Calculated based on:
- Exception severity (40 points max)
- Interface type impact (25 points max)
- Customer impact (15 points max)
- Retry count (20 points max)
- Exception age (15 points max)

## Alerting

### Recommended Alerts

1. **High Critical Exceptions**
   - Condition: `biopro.exceptions.severity.critical.count > 10`
   - Severity: High
   - Action: Immediate investigation

2. **Low Retry Success Rate**
   - Condition: `retry_success_rate < 70%`
   - Severity: Medium
   - Action: Review retry logic

3. **High Processing Time**
   - Condition: `biopro.exceptions.processing.duration > 5000ms`
   - Severity: Medium
   - Action: Performance investigation

4. **Exception Backlog**
   - Condition: `pending_exceptions + failed_exceptions > 100`
   - Severity: High
   - Action: Capacity planning

## Dashboards

### Executive Dashboard
- Exception resolution rate trend
- Business impact score
- Critical exception count
- Service availability status

### Operations Dashboard
- Exception processing performance
- Retry success rates
- Interface-specific metrics
- System health indicators

### Technical Dashboard
- GraphQL query performance
- Database connection metrics
- Cache hit rates
- JVM performance metrics

## Troubleshooting

### Common Issues

1. **Dynatrace SDK Not Active**
   - Check OneAgent installation
   - Verify network connectivity
   - Review configuration

2. **Missing Business Metrics**
   - Verify API token permissions
   - Check metric ingestion endpoint
   - Review custom metric configuration

3. **High Memory Usage**
   - Monitor metric collection frequency
   - Review batch sizes
   - Check for metric accumulation

### Diagnostic Commands

```bash
# Check Dynatrace health
curl http://localhost:8080/actuator/dynatrace

# Check overall health
curl http://localhost:8080/actuator/health

# Check metrics endpoint
curl http://localhost:8080/actuator/metrics
```

## Best Practices

### Performance
- Use appropriate batch sizes for DataLoaders
- Monitor metric collection overhead
- Implement circuit breakers for external calls

### Security
- Secure API tokens in environment variables
- Exclude sensitive data from custom attributes
- Use HTTPS for all Dynatrace communications

### Monitoring
- Set up proactive alerts for business KPIs
- Regular review of dashboard metrics
- Periodic validation of metric accuracy

## Integration Testing

### Unit Tests
- Verify metric collection accuracy
- Test custom attribute generation
- Validate health indicator logic

### Integration Tests
- End-to-end metric flow validation
- Performance impact assessment
- Failover scenario testing

### Load Tests
- Metric collection under load
- Performance degradation analysis
- Resource utilization monitoring

## Maintenance

### Regular Tasks
- Review and update alert thresholds
- Validate metric accuracy
- Update dashboard configurations
- Monitor storage usage

### Quarterly Reviews
- Assess business metric relevance
- Update KPI calculations
- Review dashboard effectiveness
- Optimize performance settings

## Support

For issues related to Dynatrace integration:
1. Check service logs for error messages
2. Verify Dynatrace connectivity
3. Review configuration settings
4. Contact platform team for assistance

## References

- [Dynatrace OneAgent SDK Documentation](https://www.dynatrace.com/support/help/extend-dynatrace/oneagent-sdk/)
- [Micrometer Dynatrace Registry](https://micrometer.io/docs/registry/dynatrace)
- [OpenTelemetry Java Documentation](https://opentelemetry.io/docs/instrumentation/java/)
- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)