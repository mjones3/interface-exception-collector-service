# GraphQL Security Enhancements

This package contains security enhancements for the GraphQL API, implementing rate limiting, query allowlisting, and comprehensive audit logging.

## Components

### Rate Limiting

**RateLimitingInterceptor** - Implements request throttling using Redis counters per user/IP with role-based limits:

- **ADMIN**: 300 requests/minute, 10,000 requests/hour, 5000 query complexity
- **OPERATIONS**: 120 requests/minute, 3,600 requests/hour, 2000 query complexity  
- **VIEWER**: 60 requests/minute, 1,800 requests/hour, 1000 query complexity

**RateLimitingConfig** - Configuration for rate limits per role, customizable via application properties.

**RateLimitExceededException** - Exception thrown when rate limits are exceeded, implements GraphQLError for structured responses.

### Query Allowlist

**QueryAllowlistInterceptor** - Production security feature that only allows pre-approved queries based on SHA-256 hash:

- Normalizes queries by removing comments and extra whitespace
- Calculates SHA-256 hash of normalized query
- Rejects queries not in the allowlist
- Conditionally enabled via `graphql.security.query-allowlist.enabled`

**QueryAllowlistConfig** - Manages the list of approved query hashes.

**QueryNotAllowedException** - Exception for queries not in the allowlist.

### Security Audit Logging

**SecurityAuditLogger** - Comprehensive audit logging for all GraphQL operations:

- Logs operation start and completion with security context
- Captures authentication details, user roles, client IP
- Tracks query performance metrics and error rates
- Identifies security violations (rate limiting, access denied)
- Structured JSON logging for security monitoring

## Configuration

### Application Properties

```yaml
graphql:
  rate-limiting:
    enabled: true
    roles:
      ADMIN:
        requests-per-minute: 300
        requests-per-hour: 10000
        max-query-complexity: 5000
      OPERATIONS:
        requests-per-minute: 120
        requests-per-hour: 3600
        max-query-complexity: 2000
      VIEWER:
        requests-per-minute: 60
        requests-per-hour: 1800
        max-query-complexity: 1000
  
  security:
    query-allowlist:
      enabled: false  # Enable in production
      allowed-query-hashes:
        - "hash1"  # Dashboard queries
        - "hash2"  # Mutation operations
    
    audit-logging:
      enabled: true
      log-queries: false      # Don't log full queries in production
      log-variables: false    # Don't log variables for security
```

### Production Configuration

In production environments:

1. **Enable Query Allowlist**: Set `graphql.security.query-allowlist.enabled=true`
2. **Populate Allowlist**: Add SHA-256 hashes of approved queries
3. **Reduce Rate Limits**: Use stricter limits in production
4. **Disable GraphiQL**: Set `spring.graphql.graphiql.enabled=false`
5. **Enable Audit Logging**: Ensure comprehensive security logging

## CORS Configuration

Enhanced CORS settings in `GraphQLSecurityConfig`:

- Specific origin patterns for dashboard domains
- Security headers (X-Frame-Options, HSTS, Content-Type-Options)
- Exposed headers for rate limiting information
- Credential support for JWT authentication

## Security Headers

Automatically applied security headers:

- `X-Frame-Options: DENY` - Prevents clickjacking
- `X-Content-Type-Options: nosniff` - Prevents MIME sniffing
- `Strict-Transport-Security` - Enforces HTTPS
- `X-Rate-Limit-Remaining` - Rate limit information
- `X-Request-ID` - Request correlation

## Error Handling

All security exceptions implement `GraphQLError` for consistent error responses:

```json
{
  "errors": [
    {
      "message": "Rate limit exceeded: 60 requests per minute allowed for role VIEWER",
      "errorType": "ExecutionAborted",
      "extensions": {
        "errorCode": "RATE_LIMIT_EXCEEDED",
        "classification": "RATE_LIMITING",
        "retryAfter": 60
      }
    }
  ]
}
```

## Monitoring

Security audit logs provide comprehensive monitoring data:

```json
{
  "event": "GRAPHQL_OPERATION_COMPLETE",
  "correlation_id": "uuid",
  "user_id": "username",
  "authenticated": true,
  "authorities": ["ROLE_VIEWER"],
  "client_ip": "192.168.1.100",
  "operation_name": "GetExceptions",
  "success": true,
  "duration_ms": 150,
  "security_violation": false
}
```

## Testing

Comprehensive test coverage includes:

- **Unit Tests**: Individual component testing with mocked dependencies
- **Integration Tests**: End-to-end security feature testing
- **Security Tests**: Rate limiting, authentication, and authorization scenarios
- **Performance Tests**: Rate limiting under load

## Deployment

### Development
- Rate limiting enabled with generous limits
- Query allowlist disabled
- GraphiQL enabled for development
- Full audit logging enabled

### Production
- Strict rate limiting based on user roles
- Query allowlist enabled with approved queries only
- GraphiQL disabled
- Security-focused audit logging
- Enhanced CORS and security headers

## Best Practices

1. **Rate Limit Tuning**: Monitor actual usage patterns and adjust limits accordingly
2. **Query Allowlist Management**: Maintain approved query hashes in secure configuration
3. **Audit Log Monitoring**: Set up alerts for security violations and unusual patterns
4. **Regular Security Reviews**: Periodically review and update security configurations
5. **Performance Impact**: Monitor Redis performance under high rate limiting load

## Troubleshooting

### Common Issues

1. **Rate Limit False Positives**: Check Redis connectivity and counter accuracy
2. **Query Allowlist Rejections**: Verify query normalization and hash calculation
3. **CORS Issues**: Ensure origin patterns match dashboard domains
4. **Performance Impact**: Monitor instrumentation overhead on query execution

### Debug Configuration

```yaml
logging:
  level:
    com.arcone.biopro.exception.collector.api.graphql.security: DEBUG
```

This enables detailed logging for all security components to aid in troubleshooting.