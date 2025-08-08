# Security Implementation

This document describes the security features implemented in the Interface Exception Collector Service.

## JWT Authentication

### Overview
The service uses JWT (JSON Web Token) bearer token authentication for API access. Tokens must be included in the `Authorization` header with the `Bearer` prefix.

### Token Structure
JWT tokens must contain the following claims:
- `sub`: Username/subject identifier
- `roles`: Array of role names (e.g., ["OPERATOR", "ADMIN"])
- `exp`: Expiration timestamp
- `iat`: Issued at timestamp

### Example Token Payload
```json
{
  "sub": "john.doe",
  "roles": ["OPERATOR", "VIEWER"],
  "iat": 1642680000,
  "exp": 1642683600
}
```

## Role-Based Access Control

### Available Roles
- **VIEWER**: Read-only access to exception data
- **OPERATOR**: Read access + ability to retry, acknowledge, and resolve exceptions
- **ADMIN**: Full access including administrative endpoints

### Endpoint Permissions

| Endpoint | VIEWER | OPERATOR | ADMIN |
|----------|--------|----------|-------|
| `GET /api/v1/exceptions/**` | ✅ | ✅ | ✅ |
| `POST /api/v1/exceptions/*/retry` | ❌ | ✅ | ✅ |
| `PUT /api/v1/exceptions/*/acknowledge` | ❌ | ✅ | ✅ |
| `PUT /api/v1/exceptions/*/resolve` | ❌ | ✅ | ✅ |
| `/actuator/**` | ❌ | ❌ | ✅ |

## Rate Limiting

### Configuration
- **Default Limit**: 60 requests per minute per user/IP
- **Burst Capacity**: 10 requests in 10 seconds
- **Storage**: Redis-backed using bucket4j

### Rate Limit Headers
Responses include rate limiting information:
- `X-Rate-Limit-Remaining`: Remaining requests in current window
- `X-Rate-Limit-Limit`: Total requests allowed per minute

### Rate Limit Response
When rate limit is exceeded, the service returns HTTP 429:
```json
{
  "error": "Rate limit exceeded",
  "message": "Too many requests. Please try again later.",
  "status": 429,
  "timestamp": 1642680000000
}
```

## TLS Configuration

### Database Connections
PostgreSQL connections support TLS encryption:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/db?sslmode=require
```

Environment variables:
- `DB_SSL_MODE`: SSL mode (disable, allow, prefer, require, verify-ca, verify-full)
- `DB_SSL_CERT`: Client certificate path
- `DB_SSL_KEY`: Client private key path
- `DB_SSL_ROOT_CERT`: Root certificate path

### Kafka Connections
Kafka supports TLS encryption:
```yaml
spring:
  kafka:
    consumer:
      properties:
        security.protocol: SSL
        ssl.truststore.location: /path/to/truststore.jks
        ssl.truststore.password: password
        ssl.keystore.location: /path/to/keystore.jks
        ssl.keystore.password: password
```

Environment variables:
- `KAFKA_SECURITY_PROTOCOL`: Security protocol (PLAINTEXT, SSL, SASL_PLAINTEXT, SASL_SSL)
- `KAFKA_SSL_TRUSTSTORE_LOCATION`: Truststore file path
- `KAFKA_SSL_TRUSTSTORE_PASSWORD`: Truststore password
- `KAFKA_SSL_KEYSTORE_LOCATION`: Keystore file path
- `KAFKA_SSL_KEYSTORE_PASSWORD`: Keystore password
- `KAFKA_SSL_KEY_PASSWORD`: Private key password

### HTTPS Server
The service can be configured to use HTTPS:
```yaml
server:
  ssl:
    enabled: true
    key-store: /path/to/keystore.jks
    key-store-password: password
    trust-store: /path/to/truststore.jks
    trust-store-password: password
```

Environment variables:
- `TLS_ENABLED`: Enable TLS for the server (true/false)
- `TLS_KEYSTORE_PATH`: Server keystore path
- `TLS_KEYSTORE_PASSWORD`: Server keystore password
- `TLS_TRUSTSTORE_PATH`: Server truststore path
- `TLS_TRUSTSTORE_PASSWORD`: Server truststore password

## Audit Logging

### Overview
All data access and modifications are logged for audit purposes using AOP (Aspect-Oriented Programming).

### Audit Event Types
- **DATA_ACCESS**: Repository method calls
- **DATA_MODIFICATION**: Create, update, delete operations
- **RETRY_OPERATION**: Exception retry attempts
- **API_ACCESS**: REST API endpoint access

### Audit Log Format
```json
{
  "eventType": "DATA_ACCESS",
  "timestamp": "2025-08-05T10:30:00Z",
  "username": "john.doe",
  "operation": "InterfaceExceptionRepository.findByTransactionId",
  "arguments": ["TXN-12345"]
}
```

### Sensitive Data Handling
- Passwords, tokens, and secrets are masked as `[MASKED]`
- Long arguments are truncated to 200 characters
- PII data is handled according to data protection policies

## Configuration

### Environment Variables

#### JWT Configuration
- `JWT_SECRET`: Secret key for JWT signing (minimum 256 bits)

#### Rate Limiting
- `RATE_LIMIT_RPM`: Requests per minute (default: 60)
- `RATE_LIMIT_BURST`: Burst capacity (default: 10)

#### TLS Configuration
- `TLS_ENABLED`: Enable TLS for server (default: false)
- `TLS_KEYSTORE_PATH`: Server keystore path
- `TLS_KEYSTORE_PASSWORD`: Server keystore password
- `TLS_TRUSTSTORE_PATH`: Server truststore path
- `TLS_TRUSTSTORE_PASSWORD`: Server truststore password

### Security Best Practices

1. **JWT Secret**: Use a strong, randomly generated secret key (minimum 256 bits)
2. **Token Expiration**: Set appropriate token expiration times (recommended: 1-8 hours)
3. **TLS Certificates**: Use certificates from trusted CAs in production
4. **Rate Limiting**: Adjust limits based on expected usage patterns
5. **Audit Logs**: Monitor audit logs for suspicious activity
6. **Role Assignment**: Follow principle of least privilege when assigning roles

### Testing Security

Run security tests:
```bash
mvn test -Dtest=SecurityConfigTest
mvn test -Dtest=JwtServiceTest
```

### Troubleshooting

#### Common Issues

1. **Invalid JWT Token**
   - Check token format and signature
   - Verify JWT secret matches between token issuer and service
   - Ensure token is not expired

2. **Rate Limit Issues**
   - Check Redis connectivity
   - Verify rate limit configuration
   - Monitor rate limit headers in responses

3. **TLS Connection Issues**
   - Verify certificate paths and passwords
   - Check certificate validity and trust chain
   - Ensure proper SSL/TLS protocol versions

#### Debug Logging
Enable debug logging for security components:
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    com.arcone.biopro.exception.collector.config.security: DEBUG
```