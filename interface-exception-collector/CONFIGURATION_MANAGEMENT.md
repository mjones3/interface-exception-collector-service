# Configuration Management and Feature Flags

This document describes the configuration management and feature flags implementation for the Mock RSocket Server Integration.

## Overview

The configuration management system provides:
- Environment-specific configuration files
- Feature flags for enabling/disabling functionality
- Conditional bean registration based on configuration
- Configuration validation at startup
- Type-safe configuration properties

## Configuration Structure

### Main Configuration Files

- `application.yml` - Base configuration with default values
- `application-dev.yml` - Development environment overrides
- `application-test.yml` - Test environment overrides  
- `application-prod.yml` - Production environment overrides

### Configuration Properties Classes

#### RSocketProperties
Binds RSocket-related configuration from `app.rsocket.*`:

```yaml
app:
  rsocket:
    mock-server:
      enabled: true
      host: localhost
      port: 7000
      timeout: 5s
      circuit-breaker:
        enabled: true
        failure-rate-threshold: 50
    partner-order-service:
      enabled: false
      host: partner-order-service
      port: 8090
```

#### FeatureFlagsProperties
Manages feature flags from `app.features.*`:

```yaml
app:
  features:
    enhanced-logging: true
    debug-mode: false
    circuit-breaker: true
    retry-mechanism: true
    metrics-collection: true
```

## Conditional Bean Registration

### SourceServiceClientConfiguration

This configuration class uses `@ConditionalOnProperty` to register the appropriate service client:

- **Mock RSocket Client**: Enabled when `app.rsocket.mock-server.enabled=true`
- **Production Client**: Enabled when `app.rsocket.mock-server.enabled=false` (default)

### Safety Mechanisms

1. **Production Safety**: Mock server cannot be enabled in production profiles
2. **Configuration Validation**: Startup validation ensures proper configuration
3. **Fallback Behavior**: Graceful degradation when services are unavailable

## Environment-Specific Behavior

### Development (`dev` profile)
- Mock server enabled by default
- Debug logging enabled
- Shorter timeouts for faster development cycles
- More lenient circuit breaker settings

### Test (`test` profile)  
- Mock server enabled
- Longer timeouts for test stability
- Audit logging enabled
- Standard resilience settings

### Production (`prod` profile)
- Mock server disabled (enforced)
- Production service endpoints
- Conservative timeout and retry settings
- Enhanced security and monitoring

## Configuration Validation

The `ConfigurationValidator` component performs startup validation:

- Ensures mock server is not enabled in production
- Validates connection parameters (host, port, timeouts)
- Checks that at least one service client is enabled
- Warns about potentially problematic configurations

## Usage Examples

### Enable Mock Server for Development
```yaml
app:
  rsocket:
    mock-server:
      enabled: true
      host: localhost
      port: 7000
```

### Use Production Service
```yaml
app:
  rsocket:
    mock-server:
      enabled: false
    partner-order-service:
      enabled: true
      host: partner-order-service
      port: 8090
```

### Feature Flag Control
```yaml
app:
  features:
    debug-mode: true
    circuit-breaker: false
    retry-mechanism: true
```

## Environment Variables

All configuration can be overridden with environment variables:

- `MOCK_RSOCKET_SERVER_ENABLED=true`
- `MOCK_RSOCKET_SERVER_HOST=mock-server`
- `MOCK_RSOCKET_SERVER_PORT=7001`
- `DEBUG_MODE=true`
- `CIRCUIT_BREAKER=false`

## Testing

The configuration system includes comprehensive tests:

- `SourceServiceClientConfigurationTest` - Tests conditional bean registration
- `ConfigurationValidatorTest` - Tests configuration validation logic
- `ConfigurationIntegrationTest` - Tests full Spring context loading

## Best Practices

1. **Environment Profiles**: Always use appropriate profiles for different environments
2. **Feature Flags**: Use feature flags to control functionality rollout
3. **Configuration Validation**: Rely on startup validation to catch configuration errors early
4. **Environment Variables**: Use environment variables for deployment-specific overrides
5. **Documentation**: Keep configuration documentation up to date

## Troubleshooting

### Common Issues

1. **Mock server enabled in production**: Check profile and configuration
2. **No service client available**: Ensure at least one client is enabled
3. **Connection failures**: Verify host, port, and network connectivity
4. **Configuration not loading**: Check property paths and profile activation

### Debug Configuration

Enable debug logging to troubleshoot configuration issues:

```yaml
logging:
  level:
    com.arcone.biopro.exception.collector.infrastructure.config: DEBUG
```