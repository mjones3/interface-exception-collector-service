# JWT Authentication Fix - Design Document

## Overview

The JWT authentication system in the interface-exception-collector service requires fixes to resolve signature validation failures. The current implementation has algorithm mismatches between token generation (Node.js scripts) and validation (Java JJWT library), inconsistent secret keys, and insufficient error logging. This design addresses these issues by standardizing the JWT implementation across all components.

## Architecture

### Current State Analysis

**Problems Identified:**
1. **Algorithm Mismatch**: Token generation scripts use different secret keys and potentially different algorithms
2. **Missing Security Configuration**: No Spring Security configuration for JWT validation
3. **Inconsistent Secret Management**: Multiple secret keys across different scripts and configuration
4. **Insufficient Error Logging**: No detailed JWT validation error logging
5. **Missing JWT Validation Components**: No JWT filter, token provider, or authentication entry point

### Target Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   API Client    │───▶│  JWT Filter      │───▶│  Protected      │
│                 │    │  (Validation)    │    │  Endpoints      │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │  JWT Token       │
                       │  Provider        │
                       │  (Validation)    │
                       └──────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │  JJWT Library    │
                       │  (HS256)         │
                       └──────────────────┘
```

## Components and Interfaces

### 1. JWT Security Configuration

**Class**: `JwtSecurityConfig`
- **Purpose**: Configure Spring Security with JWT authentication
- **Responsibilities**:
  - Define security filter chain
  - Configure JWT authentication filter
  - Set up authentication entry point
  - Define endpoint access rules

**Key Configuration**:
```java
@Configuration
@EnableWebSecurity
public class JwtSecurityConfig {
    // Security filter chain configuration
    // JWT authentication filter registration
    // Endpoint access control
}
```

### 2. JWT Authentication Filter

**Class**: `JwtAuthenticationFilter`
- **Purpose**: Intercept requests and validate JWT tokens
- **Responsibilities**:
  - Extract JWT token from Authorization header
  - Validate token using JwtTokenProvider
  - Set authentication context
  - Handle validation errors

**Interface**:
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain);
}
```

### 3. JWT Token Provider

**Class**: `JwtTokenProvider`
- **Purpose**: Handle JWT token validation and claims extraction
- **Responsibilities**:
  - Validate JWT signature using JJWT library
  - Extract user claims (username, roles)
  - Check token expiration
  - Provide detailed error information

**Interface**:
```java
@Component
public class JwtTokenProvider {
    public boolean validateToken(String token);
    public String getUsernameFromToken(String token);
    public List<String> getRolesFromToken(String token);
    public Claims getClaimsFromToken(String token);
}
```

### 4. JWT Authentication Entry Point

**Class**: `JwtAuthenticationEntryPoint`
- **Purpose**: Handle authentication failures
- **Responsibilities**:
  - Return 401 Unauthorized responses
  - Log authentication failures
  - Provide consistent error format

### 5. Standardized Token Generation

**Updated Scripts**:
- `generate-jwt.js` - Node.js token generation
- `generate-jwt.py` - Python token generation (backup)

**Standardization Requirements**:
- Use same secret key as application configuration
- Use HS256 algorithm consistently
- Generate tokens with consistent payload structure
- Include proper expiration times

## Data Models

### JWT Token Structure

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "username",
    "roles": ["ADMIN", "OPERATOR"],
    "iat": 1692307200,
    "exp": 1692310800
  }
}
```

### Authentication Context

```java
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final String token;
    private final String username;
    private final List<String> roles;
}
```

### Configuration Properties

```yaml
app:
  security:
    jwt:
      secret: ${JWT_SECRET:mySecretKey1234567890123456789012345678901234567890}
      expiration-hours: ${JWT_EXPIRATION_HOURS:1}
      algorithm: HS256
```

## Error Handling

### JWT Validation Errors

**Error Categories**:
1. **Token Missing**: No Authorization header or Bearer token
2. **Token Malformed**: Invalid JWT format
3. **Token Expired**: Token past expiration time
4. **Invalid Signature**: Signature validation failed
5. **Invalid Claims**: Missing or invalid required claims

**Error Response Format**:
```json
{
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "JWT token validation failed: Token expired",
  "path": "/api/v1/exceptions",
  "timestamp": "2025-08-19T10:30:00Z"
}
```

### Logging Strategy

**Log Levels**:
- **DEBUG**: Token validation details (without sensitive data)
- **INFO**: Successful authentications
- **WARN**: Authentication failures
- **ERROR**: Configuration or system errors

**Log Format**:
```
[JWT-AUTH] [correlationId] [username] - Token validation result: SUCCESS/FAILURE - reason
```

## Testing Strategy

### Unit Tests

1. **JwtTokenProviderTest**
   - Test token validation with valid tokens
   - Test token validation with expired tokens
   - Test token validation with invalid signatures
   - Test claims extraction

2. **JwtAuthenticationFilterTest**
   - Test filter with valid Authorization header
   - Test filter with missing Authorization header
   - Test filter with malformed tokens
   - Test authentication context setting

3. **JwtSecurityConfigTest**
   - Test security configuration
   - Test endpoint access rules
   - Test authentication entry point

### Integration Tests

1. **JWT Authentication Integration Test**
   - Test protected endpoint access with valid token
   - Test protected endpoint access with invalid token
   - Test public endpoint access without token
   - Test role-based access control

2. **Token Generation Integration Test**
   - Test generated tokens work with validation
   - Test token expiration handling
   - Test different user roles

### Manual Testing

1. **Token Generation Scripts**
   - Verify scripts generate valid tokens
   - Test tokens work with API endpoints
   - Verify consistent algorithm usage

2. **API Endpoint Testing**
   - Test all protected endpoints with valid tokens
   - Test error responses with invalid tokens
   - Verify proper HTTP status codes

## Security Considerations

### Secret Key Management

**Current Issue**: Multiple different secret keys in scripts and configuration
**Solution**: 
- Standardize on single secret key from application.yml
- Use environment variable for production deployments
- Ensure minimum key length for HS256 (32 bytes)

### Algorithm Consistency

**Requirement**: Use HS256 (HmacSHA256) consistently
**Implementation**:
- Configure JJWT library to only accept HS256
- Update all token generation scripts to use HS256
- Reject tokens with different algorithms

### Token Security

**Best Practices**:
- Short expiration times (1 hour default)
- Secure token transmission (HTTPS in production)
- No sensitive data in JWT payload
- Proper token validation on every request

## Performance Considerations

### JWT Validation Performance

**Optimizations**:
- Cache JWT secret key parsing
- Minimize token parsing overhead
- Use efficient claims extraction
- Avoid unnecessary token re-validation

### Memory Management

**Considerations**:
- Avoid storing tokens in memory
- Efficient string handling for token processing
- Proper cleanup of authentication contexts

## Configuration Management

### Environment-Specific Settings

**Development**:
```yaml
app:
  security:
    jwt:
      secret: mySecretKey1234567890123456789012345678901234567890
      expiration-hours: 1
```

**Production**:
```yaml
app:
  security:
    jwt:
      secret: ${JWT_SECRET}  # From environment variable
      expiration-hours: ${JWT_EXPIRATION_HOURS:1}
```

### Backward Compatibility

**Migration Strategy**:
- Maintain existing endpoint structure
- Keep existing role-based access control
- Preserve current API response formats
- Update only authentication mechanism

## Implementation Dependencies

### Required Libraries

**Already Available**:
- Spring Security
- Spring Boot Security Starter

**Additional Dependencies**:
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

### Integration Points

**Spring Security Integration**:
- Security filter chain configuration
- Authentication manager setup
- Method-level security annotations

**Existing Codebase Integration**:
- Maintain current controller structure
- Preserve existing error handling patterns
- Keep current logging configuration

## Monitoring and Observability

### Metrics

**Authentication Metrics**:
- JWT validation success/failure rates
- Authentication attempt counts by endpoint
- Token expiration rates
- Invalid token attempt patterns

### Health Checks

**JWT Health Indicator**:
- Verify JWT configuration is valid
- Check secret key availability
- Validate algorithm configuration

### Alerting

**Alert Conditions**:
- High authentication failure rates
- Repeated invalid token attempts
- JWT configuration errors
- Token generation script failures