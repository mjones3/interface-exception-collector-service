# RSocket Configuration Examples

This directory contains example configuration files for different deployment scenarios of the BioPro Interface Exception Collector with Mock RSocket Server integration.

## Configuration Files Overview

### 1. application-local-dev.yml
**Purpose**: Minimal configuration for local development
**Use Case**: Individual developer workstation setup
**Key Features**:
- Simplified RSocket configuration
- Debug logging enabled
- Circuit breaker and retry disabled for immediate feedback
- Minimal management endpoints
- Fast timeout for quick iteration

**When to Use**:
- Local development on developer machines
- Quick testing and debugging
- Prototype development
- Learning and experimentation

### 2. application-integration-test.yml
**Purpose**: Configuration optimized for automated integration testing
**Use Case**: CI/CD pipelines and automated test suites
**Key Features**:
- TestContainers-friendly configuration
- Longer timeouts for test stability
- Lenient circuit breaker settings
- Comprehensive metrics collection
- Minimal logging for performance

**When to Use**:
- Automated integration tests
- CI/CD pipeline execution
- TestContainers-based testing
- Performance testing scenarios

### 3. application-docker-compose.yml
**Purpose**: Configuration for Docker Compose development environments
**Use Case**: Multi-container development setup
**Key Features**:
- Service name-based host configuration
- Container-friendly timeouts
- Enhanced logging for debugging
- Docker networking considerations
- Volume mount support

**When to Use**:
- Docker Compose development environments
- Multi-service integration testing
- Container-based development workflows
- Team development environments

### 4. application-staging.yml
**Purpose**: Production-like configuration for staging environments
**Use Case**: Pre-production validation and testing
**Key Features**:
- Production-like resilience settings
- Enhanced monitoring and metrics
- Comprehensive audit logging
- Security-conscious configuration
- Performance optimization

**When to Use**:
- Staging environment deployment
- Pre-production validation
- Load testing scenarios
- Security testing

### 5. application-mock-server-examples.yml
**Purpose**: Comprehensive configuration examples for all environments
**Use Case**: Reference guide and template for Mock RSocket Server integration
**Key Features**:
- Complete configuration examples for development, testing, staging, and production
- Performance testing and troubleshooting configurations
- Environment variable override examples
- Docker Compose and Kubernetes configurations
- Detailed comments explaining each setting

**When to Use**:
- As a reference when creating new configurations
- Understanding all available configuration options
- Troubleshooting configuration issues
- Setting up new environments
- Learning Mock RSocket Server integration patterns

**Configuration Profiles Included**:
- `dev-example` - Development environment with debug logging
- `test-example` - Testing environment with Kubernetes service discovery
- `staging-example` - Staging environment with production-like settings
- `prod-example` - Production environment with mock server disabled
- `performance-test-example` - Optimized for load testing
- `troubleshooting-example` - Full debug logging for issue diagnosis
- `docker-compose-example` - Docker Compose networking
- `kubernetes-example` - Kubernetes DNS configuration

## Configuration Selection Guide

### Development Environments

| Scenario | Recommended Configuration | Rationale |
|----------|--------------------------|-----------|
| Local IDE Development | `application-local-dev.yml` | Fast feedback, minimal complexity |
| Docker Compose Setup | `application-docker-compose.yml` | Container networking, service discovery |
| Team Development | `application-docker-compose.yml` | Consistent multi-developer environment |

### Testing Environments

| Scenario | Recommended Configuration | Rationale |
|----------|--------------------------|-----------|
| Unit Tests | Default test configuration | Minimal overhead |
| Integration Tests | `application-integration-test.yml` | TestContainers compatibility |
| End-to-End Tests | `application-staging.yml` | Production-like behavior |
| Performance Tests | `application-staging.yml` | Realistic performance characteristics |

### Deployment Environments

| Scenario | Recommended Configuration | Rationale |
|----------|--------------------------|-----------|
| Development Deployment | `application-docker-compose.yml` | Container orchestration |
| Staging Deployment | `application-staging.yml` | Production validation |
| Production Deployment | `application-prod.yml` | Security and reliability |

## Customization Guidelines

### 1. Timeout Configuration

Adjust timeouts based on your environment characteristics:

```yaml
app:
  rsocket:
    mock-server:
      timeout: 5s                    # Request timeout
      connection-timeout: 10s        # Connection establishment timeout
```

**Recommendations**:
- **Local Development**: 3-5 seconds (fast feedback)
- **Integration Tests**: 10-15 seconds (stability)
- **Docker Compose**: 8-12 seconds (container startup)
- **Staging**: 20-30 seconds (production-like)

### 2. Circuit Breaker Configuration

Tune circuit breaker settings for your failure tolerance:

```yaml
app:
  rsocket:
    mock-server:
      circuit-breaker:
        failure-rate-threshold: 50        # Percentage of failures
        wait-duration-in-open-state: 30s  # Recovery wait time
        sliding-window-size: 10           # Sample size
```

**Recommendations**:
- **Development**: Lenient settings (30-40% threshold)
- **Testing**: Moderate settings (40-50% threshold)
- **Staging**: Conservative settings (50-60% threshold)

### 3. Retry Configuration

Configure retry behavior for your reliability requirements:

```yaml
app:
  rsocket:
    mock-server:
      retry:
        max-attempts: 3                      # Maximum retry attempts
        wait-duration: 1s                    # Initial wait time
        exponential-backoff-multiplier: 2.0  # Backoff multiplier
```

**Recommendations**:
- **Development**: Fewer retries (1-2 attempts)
- **Testing**: Moderate retries (2-3 attempts)
- **Staging**: More retries (3-5 attempts)

### 4. Logging Configuration

Adjust logging levels for your debugging needs:

```yaml
logging:
  level:
    com.arcone.biopro.exception.collector.infrastructure.client: DEBUG
    io.rsocket: INFO
```

**Recommendations**:
- **Development**: DEBUG level for detailed troubleshooting
- **Testing**: INFO level for test execution visibility
- **Staging**: WARN level for performance and security

## Environment Variable Overrides

All configurations support environment variable overrides for deployment flexibility:

```bash
# Override RSocket host
export APP_RSOCKET_MOCK_SERVER_HOST=custom-host

# Override RSocket port
export APP_RSOCKET_MOCK_SERVER_PORT=8000

# Override timeout
export APP_RSOCKET_MOCK_SERVER_TIMEOUT=10s

# Override circuit breaker settings
export APP_RSOCKET_MOCK_SERVER_CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD=60
```

## Security Considerations

### Development Environments
- Mock server enabled for testing
- Debug logging acceptable
- Relaxed security settings

### Staging Environments
- Production-like security settings
- Audit logging enabled
- Restricted management endpoints

### Production Environments
- Mock server MUST be disabled
- Minimal logging for security
- Comprehensive audit trails

## Troubleshooting Common Issues

### Connection Refused Errors
```yaml
# Increase connection timeout
app:
  rsocket:
    mock-server:
      connection-timeout: 20s
```

### Timeout Issues
```yaml
# Increase request timeout
app:
  rsocket:
    mock-server:
      timeout: 15s
```

### Circuit Breaker Opening Frequently
```yaml
# Adjust circuit breaker sensitivity
app:
  rsocket:
    mock-server:
      circuit-breaker:
        failure-rate-threshold: 70
        minimum-number-of-calls: 10
```

### Container Networking Issues
```yaml
# Use service names in Docker Compose
app:
  rsocket:
    mock-server:
      host: mock-rsocket-server  # Service name, not localhost
```

## Validation and Testing

Each configuration file should be validated before use:

1. **Syntax Validation**: Ensure YAML syntax is correct
2. **Configuration Validation**: Run application startup to validate settings
3. **Connection Testing**: Verify RSocket connectivity
4. **Performance Testing**: Validate timeout and retry settings

## Best Practices

1. **Environment Separation**: Use different configurations for different environments
2. **Security First**: Never enable mock server in production
3. **Monitoring**: Enable appropriate metrics and logging for each environment
4. **Documentation**: Document any customizations made to these base configurations
5. **Version Control**: Track configuration changes in version control
6. **Testing**: Test configuration changes in non-production environments first

## Support and Maintenance

- Review configurations quarterly for optimization opportunities
- Update timeout values based on performance monitoring
- Adjust circuit breaker settings based on failure patterns
- Monitor logs for configuration-related warnings or errors

For additional configuration options and detailed explanations, refer to the main [RSocket Configuration Guide](../../RSOCKET_CONFIGURATION_GUIDE.md).