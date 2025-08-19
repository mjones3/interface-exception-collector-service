# Dynatrace Integration Setup Guide

## Overview

The Interface Exception Collector service has been fully instrumented with comprehensive Dynatrace monitoring capabilities. The integration is **disabled by default** to avoid compilation issues in environments where Dynatrace dependencies are not available.

## Current Status

✅ **Fully Implemented Components:**
- Business metrics collection service
- Automatic instrumentation aspects
- Health monitoring and indicators
- Custom actuator endpoints
- Integration with existing services
- Scheduled health monitoring
- Production-ready configuration

🔒 **Currently Disabled** (to ensure compilation works)

## Enabling Dynatrace Integration

### Step 1: Set Environment Variable

```bash
export DYNATRACE_ENABLED=true
```

Or in your deployment configuration:
```yaml
environment:
  - DYNATRACE_ENABLED=true
```

### Step 2: Configure Dynatrace Connection

Set the required Dynatrace environment variables:

```bash
# Required for Dynatrace integration
export DYNATRACE_API_TOKEN=your_api_token_here
export DYNATRACE_URI=https://your_tenant.live.dynatrace.com/api/v2/metrics/ingest
export DYNATRACE_DEVICE_ID=interface-exception-collector

# Optional OpenTelemetry configuration
export OTEL_EXPORTER_OTLP_ENDPOINT=https://your_tenant.live.dynatrace.com/api/v2/otlp
export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Api-Token your_api_token_here"
```

### Step 3: Install Dynatrace OneAgent (Production)

For full Dynatrace functionality in production, install the OneAgent:

```bash
# Download and install OneAgent (example for Linux)
wget -O Dynatrace-OneAgent.sh "https://your_tenant.live.dynatrace.com/api/v1/deployment/installer/agent/unix/default/latest?arch=x86&flavor=default"
sudo /bin/sh Dynatrace-OneAgent.sh --set-app-log-content-access=true
```

### Step 4: Verify Integration

Once enabled, verify the integration is working:

```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Check Dynatrace-specific endpoint
curl http://localhost:8080/actuator/dynatrace

# Check if Dynatrace components are loaded
curl http://localhost:8080/actuator/beans | grep -i dynatrace
```

## What Gets Monitored

### Business Metrics
- Exception lifecycle events (received, processed, resolved, acknowledged)
- Retry operations and success rates
- Payload retrieval performance
- Business impact scoring
- Real-time status and severity counters

### Performance Metrics
- Exception processing times by interface type and severity
- GraphQL query/mutation performance
- Kafka message processing performance
- Database and cache operation metrics

### Custom Attributes
- Transaction IDs, interface types, severity levels
- Customer IDs, location codes, external IDs
- Operation types, retry attempts, user actions
- Business context for enhanced filtering

## Business KPIs Tracked

1. **Exception Resolution Rate**: (Resolved / Total) × 100%
2. **Critical Exception Ratio**: (Critical / Total) × 100%
3. **Processing Efficiency**: (Processed / Received) × 100%
4. **Retry Success Rate**: (Successful Retries / Total Retries) × 100%
5. **Business Impact Score**: Calculated based on severity, interface type, customer impact, retry count, and age

## Health Thresholds

| Metric | Warning | Critical |
|--------|---------|----------|
| Critical Exceptions | > 10 | > 25 |
| Pending Exceptions | > 100 | > 500 |
| Retry Success Rate | < 70% | < 50% |
| Processing Efficiency | < 80% | < 60% |

## Troubleshooting

### Integration Not Working

1. **Check if enabled**: Verify `DYNATRACE_ENABLED=true`
2. **Check logs**: Look for Dynatrace-related log messages
3. **Verify dependencies**: Ensure OneAgent SDK is available
4. **Check connectivity**: Verify network access to Dynatrace tenant

### Missing Metrics

1. **Check API token**: Verify token has metric ingestion permissions
2. **Check endpoint**: Verify Dynatrace URI is correct
3. **Check OneAgent**: Verify OneAgent is installed and active

### Performance Impact

The Dynatrace integration is designed to have minimal performance impact:
- Metrics collection is asynchronous
- Components are conditionally loaded
- Graceful degradation when Dynatrace is unavailable

## Development vs Production

### Development
- Set `DYNATRACE_ENABLED=false` (default)
- No OneAgent required
- No performance impact
- All business logic works normally

### Production
- Set `DYNATRACE_ENABLED=true`
- Install OneAgent for full functionality
- Configure proper API tokens and endpoints
- Monitor business metrics and KPIs

## Next Steps

1. **Enable in Development**: Set `DYNATRACE_ENABLED=true` to test integration
2. **Configure Dashboards**: Set up Dynatrace dashboards for business metrics
3. **Set Up Alerts**: Configure alerts based on business thresholds
4. **Monitor Performance**: Track the impact of monitoring on system performance
5. **Iterate on Metrics**: Refine business metrics based on operational needs

## Support

For issues with Dynatrace integration:
1. Check service logs for Dynatrace-related errors
2. Verify environment variables are set correctly
3. Test connectivity to Dynatrace tenant
4. Review the comprehensive integration guide in `docs/DYNATRACE_INTEGRATION_GUIDE.md`

The integration provides comprehensive business observability while maintaining system stability and performance.