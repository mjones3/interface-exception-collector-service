# GraphQL Authentication & Authorization Guide

## Interface Exception Collector GraphQL API

### Overview

The GraphQL API implements JWT-based authentication with role-based access control (RBAC) to ensure secure access to exception data and operations. This guide covers authentication setup, authorization patterns, and security best practices.

## Authentication

### JWT Token Structure

The API uses JSON Web Tokens (JWT) for authentication. Each token must contain the following claims:

```json
{
    "sub": "user123",                    // Subject (User ID)
    "roles": ["OPERATIONS", "VIEWER"],   // User roles array
    "exp": 1640995200,                   // Expiration timestamp
    "iat": 1640908800,                   // Issued at timestamp
    "iss": "biopro-auth-service",        // Issuer
    "aud": "interface-exception-api",    // Audience
    "name": "John Doe",                  // User display name
    "email": "john.doe@company.com",     // User email
    "department": "Operations",          // User department (optional)
    "location": "US-EAST"               // User location (optional)
}
```

### Token Validation

The API validates JWT tokens using the following criteria:

1. **Signature Verification**: Tokens must be signed with the correct secret/key
2. **Expiration Check**: Tokens must not be expired (`exp` claim)
3. **Issuer Validation**: Must match expected issuer (`iss` claim)
3. **Audience Validation**: Must match API audience (`aud` claim)
4. **Required Claims**: Must contain `sub`, `roles`, `exp`, and `iat` claims

### Authentication Headers

Include the JWT token in the Authorization header for all requests:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Token Refresh

Implement token refresh logic to handle expired tokens:

```javascript
import { setContext } from '@apollo/client/link/context';
import { onError } from '@apollo/client/link/error';

const authLink = setContext(async (_, { headers }) => {
    let token = localStorage.getItem('jwt-token');
    
    // Check if token is expired
    if (token && isTokenExpired(token)) {
        try {
            token = await refreshToken();
            localStorage.setItem('jwt-token', token);
        } catch (error) {
            // Redirect to login if refresh fails
            window.location.href = '/login';
            return { headers };
        }
    }
    
    return {
        headers: {
            ...headers,
            authorization: token ? `Bearer ${token}` : "",
        }
    };
});

const errorLink = onError(({ graphQLErrors, operation, forward }) => {
    if (graphQLErrors) {
        for (let err of graphQLErrors) {
            if (err.extensions?.code === 'AUTHORIZATION_ERROR') {
                // Clear invalid token and redirect to login
                localStorage.removeItem('jwt-token');
                window.location.href = '/login';
            }
        }
    }
});
```

## Authorization

### Role Hierarchy

The API implements a hierarchical role system:

```
ADMIN
├── Full system access
├── All query and mutation operations
├── System health and configuration access
└── User management capabilities

OPERATIONS
├── All VIEWER permissions
├── Exception retry operations
├── Exception acknowledgment operations
├── Original payload access
└── Bulk operations

VIEWER
├── Read-only access to exceptions
├── Exception list and detail queries
├── Summary statistics access
└── Real-time subscription access
```

### Role-Based Access Control

#### Query-Level Authorization

```graphql
# Queries available to all authenticated users
type Query {
    exceptions: ExceptionConnection!     # VIEWER+
    exception: Exception                 # VIEWER+
    exceptionSummary: ExceptionSummary! # VIEWER+
    searchExceptions: ExceptionConnection! # VIEWER+
    
    # Admin-only queries
    systemHealth: SystemHealth!          # ADMIN only
    userActivity: [UserActivity!]!       # ADMIN only
}

# Mutations require elevated permissions
type Mutation {
    retryException: RetryExceptionResult!           # OPERATIONS+
    acknowledgeException: AcknowledgeExceptionResult! # OPERATIONS+
    bulkRetryExceptions: BulkRetryResult!          # OPERATIONS+
    resolveException: ResolveExceptionResult!       # OPERATIONS+
    
    # Admin-only mutations
    updateSystemConfig: ConfigResult!               # ADMIN only
    purgeOldExceptions: PurgeResult!               # ADMIN only
}
```

#### Field-Level Authorization

Some fields require specific permissions:

```graphql
type Exception {
    # Basic fields - available to all roles
    id: ID!
    transactionId: String!
    interfaceType: InterfaceType!
    status: ExceptionStatus!
    severity: ExceptionSeverity!
    
    # Sensitive fields - require OPERATIONS+ role
    originalPayload: OriginalPayload    # @requiresRole("OPERATIONS")
    
    # Audit fields - require ADMIN role
    internalNotes: String              # @requiresRole("ADMIN")
    systemMetadata: JSON               # @requiresRole("ADMIN")
}
```

### Permission Checking

#### Server-Side Implementation

```java
@Component
public class GraphQLSecurityService {
    
    public boolean hasRole(Authentication auth, String requiredRole) {
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        return authorities.stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + requiredRole));
    }
    
    public boolean canViewPayload(Exception exception, Authentication auth) {
        // OPERATIONS and ADMIN can view all payloads
        if (hasRole(auth, "OPERATIONS") || hasRole(auth, "ADMIN")) {
            return true;
        }
        
        // VIEWER can only view non-sensitive payloads
        return !exception.getCategory().equals(ExceptionCategory.AUTHENTICATION_ERROR);
    }
    
    public boolean canRetryException(String transactionId, Authentication auth) {
        if (!hasRole(auth, "OPERATIONS") && !hasRole(auth, "ADMIN")) {
            return false;
        }
        
        // Additional business logic checks
        Exception exception = exceptionService.findByTransactionId(transactionId);
        return exception != null && exception.isRetryable();
    }
}
```

#### Resolver-Level Security

```java
@Component
public class ExceptionQueryResolver {
    
    @QueryMapping
    @PreAuthorize("hasRole('VIEWER')")
    public CompletableFuture<ExceptionConnection> exceptions(
            @Argument ExceptionFilters filters,
            Authentication authentication) {
        
        // Apply user-specific filters based on role/location
        filters = applyUserContextFilters(filters, authentication);
        return exceptionService.findExceptions(filters);
    }
    
    @SchemaMapping
    @PreAuthorize("@securityService.canViewPayload(#exception, authentication)")
    public CompletableFuture<OriginalPayload> originalPayload(
            Exception exception,
            Authentication authentication) {
        return payloadService.getOriginalPayload(exception.getTransactionId());
    }
}

@Component
public class RetryMutationResolver {
    
    @MutationMapping
    @PreAuthorize("@securityService.canRetryException(#input.transactionId, authentication)")
    public CompletableFuture<RetryExceptionResult> retryException(
            @Argument RetryExceptionInput input,
            Authentication authentication) {
        
        String userId = authentication.getName();
        return retryService.retryException(input.getTransactionId(), userId, input);
    }
}
```

### Data Filtering by User Context

#### Location-Based Filtering

Users may be restricted to data from specific locations:

```java
private ExceptionFilters applyUserContextFilters(ExceptionFilters filters, Authentication auth) {
    JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
    String userLocation = jwtAuth.getToken().getClaimAsString("location");
    
    // If user has location restriction, apply it
    if (userLocation != null && !hasRole(auth, "ADMIN")) {
        if (filters.getLocationCodes() == null) {
            filters.setLocationCodes(List.of(userLocation));
        } else {
            // Intersect requested locations with user's allowed locations
            filters.setLocationCodes(
                filters.getLocationCodes().stream()
                    .filter(loc -> loc.equals(userLocation))
                    .collect(Collectors.toList())
            );
        }
    }
    
    return filters;
}
```

#### Customer-Based Filtering

```java
private ExceptionFilters applyCustomerFilters(ExceptionFilters filters, Authentication auth) {
    // Customer service representatives can only see their assigned customers
    if (hasRole(auth, "CUSTOMER_REP")) {
        List<String> assignedCustomers = getAssignedCustomers(auth.getName());
        
        if (filters.getCustomerIds() == null) {
            filters.setCustomerIds(assignedCustomers);
        } else {
            filters.setCustomerIds(
                filters.getCustomerIds().stream()
                    .filter(assignedCustomers::contains)
                    .collect(Collectors.toList())
            );
        }
    }
    
    return filters;
}
```

## Security Best Practices

### 1. Token Security

```javascript
// Store tokens securely
class TokenManager {
    static setToken(token) {
        // Use httpOnly cookies for better security
        document.cookie = `jwt-token=${token}; HttpOnly; Secure; SameSite=Strict`;
        
        // Or use sessionStorage for SPA
        sessionStorage.setItem('jwt-token', token);
    }
    
    static getToken() {
        // Read from cookie or sessionStorage
        return sessionStorage.getItem('jwt-token');
    }
    
    static clearToken() {
        document.cookie = 'jwt-token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        sessionStorage.removeItem('jwt-token');
    }
}
```

### 2. Request Validation

```javascript
// Validate requests before sending
const validateRequest = (operation, variables) => {
    // Check if user has permission for this operation
    const userRoles = getCurrentUserRoles();
    const requiredRole = getRequiredRole(operation);
    
    if (!userRoles.includes(requiredRole)) {
        throw new Error('Insufficient permissions');
    }
    
    // Validate input parameters
    if (operation.includes('retry') && !variables.input?.reason) {
        throw new Error('Reason is required for retry operations');
    }
    
    return true;
};
```

### 3. Rate Limiting

The API implements rate limiting per user and role:

| Role | Queries/min | Mutations/min | Subscriptions |
|------|-------------|---------------|---------------|
| VIEWER | 100 | 0 | 5 concurrent |
| OPERATIONS | 200 | 50 | 10 concurrent |
| ADMIN | 500 | 100 | 20 concurrent |

### 4. Audit Logging

All operations are logged for security auditing:

```java
@Component
public class SecurityAuditLogger {
    
    @EventListener
    public void logGraphQLOperation(GraphQLRequestEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        AuditLog auditLog = AuditLog.builder()
            .userId(auth.getName())
            .operation(event.getOperationName())
            .variables(sanitizeVariables(event.getVariables()))
            .timestamp(Instant.now())
            .ipAddress(getClientIpAddress())
            .userAgent(getUserAgent())
            .success(event.isSuccess())
            .build();
            
        auditLogRepository.save(auditLog);
    }
}
```

## Error Handling

### Authentication Errors

```javascript
const handleAuthError = (error) => {
    if (error.extensions?.code === 'AUTHORIZATION_ERROR') {
        switch (error.extensions?.reason) {
            case 'TOKEN_EXPIRED':
                // Attempt token refresh
                return refreshTokenAndRetry();
                
            case 'INVALID_TOKEN':
                // Clear token and redirect to login
                TokenManager.clearToken();
                window.location.href = '/login';
                break;
                
            case 'INSUFFICIENT_PERMISSIONS':
                showNotification({
                    title: 'Access Denied',
                    message: 'You do not have permission to perform this action.',
                    type: 'warning'
                });
                break;
                
            default:
                showNotification({
                    title: 'Authentication Error',
                    message: error.message,
                    type: 'error'
                });
        }
    }
};
```

### Permission Errors

```graphql
# Error response for insufficient permissions
{
    "errors": [
        {
            "message": "Access denied",
            "extensions": {
                "code": "AUTHORIZATION_ERROR",
                "reason": "INSUFFICIENT_PERMISSIONS",
                "requiredRole": "OPERATIONS",
                "userRoles": ["VIEWER"]
            },
            "path": ["retryException"]
        }
    ]
}
```

## Testing Authentication

### Unit Tests

```javascript
describe('Authentication', () => {
    test('should reject requests without token', async () => {
        const client = createTestClient({ token: null });
        
        const result = await client.query({
            query: GET_EXCEPTIONS
        });
        
        expect(result.errors[0].extensions.code).toBe('AUTHORIZATION_ERROR');
    });
    
    test('should allow OPERATIONS role to retry exceptions', async () => {
        const client = createTestClient({ 
            token: createToken({ roles: ['OPERATIONS'] })
        });
        
        const result = await client.mutate({
            mutation: RETRY_EXCEPTION,
            variables: { input: { transactionId: 'TXN-001', reason: 'Test' } }
        });
        
        expect(result.errors).toBeUndefined();
        expect(result.data.retryException.success).toBe(true);
    });
    
    test('should deny VIEWER role from retry operations', async () => {
        const client = createTestClient({ 
            token: createToken({ roles: ['VIEWER'] })
        });
        
        const result = await client.mutate({
            mutation: RETRY_EXCEPTION,
            variables: { input: { transactionId: 'TXN-001', reason: 'Test' } }
        });
        
        expect(result.errors[0].extensions.code).toBe('AUTHORIZATION_ERROR');
    });
});
```

### Integration Tests

```java
@Test
@WithMockUser(roles = "OPERATIONS")
public void testRetryExceptionWithOperationsRole() throws Exception {
    String mutation = """
        mutation {
            retryException(input: {
                transactionId: "TXN-001"
                reason: "Test retry"
            }) {
                success
            }
        }
        """;
        
    mockMvc.perform(post("/graphql")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createGraphQLRequest(mutation)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.retryException.success").value(true));
}

@Test
@WithMockUser(roles = "VIEWER")
public void testRetryExceptionWithViewerRoleShouldFail() throws Exception {
    String mutation = """
        mutation {
            retryException(input: {
                transactionId: "TXN-001"
                reason: "Test retry"
            }) {
                success
            }
        }
        """;
        
    mockMvc.perform(post("/graphql")
            .contentType(MediaType.APPLICATION_JSON)
            .content(createGraphQLRequest(mutation)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors[0].extensions.code").value("AUTHORIZATION_ERROR"));
}
```

This authentication and authorization guide provides comprehensive coverage of security implementation for the GraphQL API. Follow these patterns to ensure secure access to exception data and operations.