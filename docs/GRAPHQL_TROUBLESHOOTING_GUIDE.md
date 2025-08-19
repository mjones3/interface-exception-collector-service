# GraphQL API Troubleshooting Guide

## Interface Exception Collector GraphQL API

### Overview

This guide helps developers and operators troubleshoot common issues when working with the Interface Exception Collector GraphQL API. It covers authentication problems, query issues, performance concerns, and operational challenges.

## Table of Contents

1. [Authentication & Authorization Issues](#authentication--authorization-issues)
2. [Query & Mutation Problems](#query--mutation-problems)
3. [Subscription Issues](#subscription-issues)
4. [Performance Problems](#performance-problems)
5. [Network & Connectivity Issues](#network--connectivity-issues)
6. [Cache-Related Issues](#cache-related-issues)
7. [Error Codes Reference](#error-codes-reference)
8. [Debugging Tools & Techniques](#debugging-tools--techniques)

## Authentication & Authorization Issues

### Problem: "Authorization header missing or invalid"

**Symptoms:**
```json
{
    "errors": [
        {
            "message": "Access denied",
            "extensions": {
                "code": "AUTHORIZATION_ERROR",
                "reason": "INVALID_TOKEN"
            }
        }
    ]
}
```

**Causes & Solutions:**

1. **Missing Authorization Header**
   ```javascript
   // ❌ Incorrect - no authorization header
   fetch('/graphql', {
       method: 'POST',
       headers: { 'Content-Type': 'application/json' },
       body: JSON.stringify({ query })
   });

   // ✅ Correct - include authorization header
   fetch('/graphql', {
       method: 'POST',
       headers: {
           'Content-Type': 'application/json',
           'Authorization': `Bearer ${token}`
       },
       body: JSON.stringify({ query })
   });
   ```

2. **Expired JWT Token**
   ```javascript
   // Check token expiration
   const isTokenExpired = (token) => {
       try {
           const payload = JSON.parse(atob(token.split('.')[1]));
           return payload.exp * 1000 < Date.now();
       } catch (error) {
           return true;
       }
   };

   // Refresh token if expired
   if (isTokenExpired(currentToken)) {
       const newToken = await refreshToken();
       localStorage.setItem('jwt-token', newToken);
   }
   ```

3. **Invalid Token Format**
   ```javascript
   // ❌ Incorrect token format
   const token = "invalid-token-format";

   // ✅ Valid JWT token format
   const token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
   ```

### Problem: "Insufficient permissions for operation"

**Symptoms:**
```json
{
    "errors": [
        {
            "message": "Access denied",
            "extensions": {
                "code": "AUTHORIZATION_ERROR",
                "reason": "INSUFFICIENT_PERMISSIONS",
                "requiredRole": "OPERATIONS",
                "userRoles": ["VIEWER"]
            }
        }
    ]
}
```

**Solutions:**

1. **Check User Roles**
   ```javascript
   // Decode JWT to check roles
   const getUserRoles = (token) => {
       try {
           const payload = JSON.parse(atob(token.split('.')[1]));
           return payload.roles || [];
       } catch (error) {
           return [];
       }
   };

   const roles = getUserRoles(localStorage.getItem('jwt-token'));
   console.log('User roles:', roles);
   ```

2. **Role Requirements by Operation**
   | Operation | Required Role | Description |
   |-----------|---------------|-------------|
   | Query exceptions | VIEWER+ | Read access to exception data |
   | Retry exception | OPERATIONS+ | Initiate retry operations |
   | View payload | OPERATIONS+ | Access original payload data |
   | System health | ADMIN | System administration |

3. **Request Role Upgrade**
   - Contact system administrator to upgrade user role
   - Ensure user has business justification for elevated permissions

## Query & Mutation Problems

### Problem: "Query complexity exceeded maximum limit"

**Symptoms:**
```json
{
    "errors": [
        {
            "message": "Query complexity 1250 exceeds maximum of 1000",
            "extensions": {
                "code": "QUERY_COMPLEXITY_EXCEEDED",
                "complexity": 1250,
                "maxComplexity": 1000
            }
        }
    ]
}
```

**Solutions:**

1. **Reduce Query Depth**
   ```graphql
   # ❌ Too complex - deep nesting
   query ComplexQuery {
       exceptions {
           edges {
               node {
                   originalPayload { content }
                   retryHistory {
                       resultErrorDetails
                   }
                   statusHistory {
                       notes
                   }
               }
           }
       }
   }

   # ✅ Simplified - request only needed fields
   query SimpleQuery {
       exceptions {
           edges {
               node {
                   id
                   transactionId
                   status
                   severity
               }
           }
       }
   }
   ```

2. **Use Pagination**
   ```graphql
   # ❌ Requesting too many items
   query TooManyItems {
       exceptions(pagination: { first: 1000 }) {
           edges { node { id } }
       }
   }

   # ✅ Use reasonable page sizes
   query ReasonablePageSize {
       exceptions(pagination: { first: 20 }) {
           edges { node { id } }
       }
   }
   ```

3. **Lazy Load Expensive Fields**
   ```javascript
   // Load basic data first
   const { data } = useQuery(GET_EXCEPTIONS_BASIC);

   // Load detailed data on demand
   const loadDetails = (transactionId) => {
       client.query({
           query: GET_EXCEPTION_DETAIL,
           variables: { transactionId }
       });
   };
   ```

### Problem: "Validation errors in mutation input"

**Symptoms:**
```json
{
    "errors": [
        {
            "message": "Validation failed",
            "extensions": {
                "code": "VALIDATION_ERROR",
                "field": "reason",
                "rejectedValue": "",
                "message": "Reason cannot be empty"
            }
        }
    ]
}
```

**Solutions:**

1. **Input Validation**
   ```javascript
   const validateRetryInput = (input) => {
       const errors = [];
       
       if (!input.transactionId?.trim()) {
           errors.push('Transaction ID is required');
       }
       
       if (!input.reason?.trim()) {
           errors.push('Reason is required');
       }
       
       if (input.reason && input.reason.length < 10) {
           errors.push('Reason must be at least 10 characters');
       }
       
       return errors;
   };

   // Use before mutation
   const errors = validateRetryInput(retryInput);
   if (errors.length > 0) {
       showValidationErrors(errors);
       return;
   }
   ```

2. **Required Fields Reference**
   | Mutation | Required Fields | Optional Fields |
   |----------|----------------|-----------------|
   | retryException | transactionId, reason | priority, notes |
   | acknowledgeException | transactionId, reason | notes, estimatedResolutionTime |
   | resolveException | transactionId, resolutionMethod | notes |

### Problem: "Exception not found"

**Symptoms:**
```json
{
    "data": {
        "exception": null
    },
    "errors": [
        {
            "message": "Exception not found",
            "extensions": {
                "code": "NOT_FOUND",
                "transactionId": "TXN-12345"
            }
        }
    ]
}
```

**Solutions:**

1. **Verify Transaction ID**
   ```javascript
   // Check transaction ID format
   const isValidTransactionId = (id) => {
       return /^TXN-[A-Z0-9]{6,}$/.test(id);
   };

   if (!isValidTransactionId(transactionId)) {
       console.error('Invalid transaction ID format:', transactionId);
   }
   ```

2. **Check Exception Status**
   ```javascript
   // Exception might be archived or purged
   const checkExceptionExists = async (transactionId) => {
       try {
           const result = await client.query({
               query: GET_EXCEPTION_DETAIL,
               variables: { transactionId },
               errorPolicy: 'all'
           });
           
           return result.data?.exception !== null;
       } catch (error) {
           return false;
       }
   };
   ```

## Subscription Issues

### Problem: "WebSocket connection failed"

**Symptoms:**
- Subscriptions not receiving updates
- Connection errors in browser console
- "WebSocket connection failed" messages

**Solutions:**

1. **Check WebSocket URL**
   ```javascript
   // ❌ Incorrect protocol
   const wsLink = new GraphQLWsLink(createClient({
       url: 'http://localhost:8080/subscriptions', // Wrong protocol
   }));

   // ✅ Correct WebSocket protocol
   const wsLink = new GraphQLWsLink(createClient({
       url: 'ws://localhost:8080/subscriptions', // HTTP -> WS
       // or
       url: 'wss://your-domain.com/subscriptions', // HTTPS -> WSS
   }));
   ```

2. **Authentication for WebSocket**
   ```javascript
   const wsLink = new GraphQLWsLink(createClient({
       url: 'ws://localhost:8080/subscriptions',
       connectionParams: () => ({
           // Include auth token in connection params
           authorization: `Bearer ${localStorage.getItem('jwt-token')}`,
       }),
   }));
   ```

3. **Connection Retry Logic**
   ```javascript
   const wsLink = new GraphQLWsLink(createClient({
       url: 'ws://localhost:8080/subscriptions',
       retryAttempts: 5,
       retryWait: async (retries) => {
           await new Promise(resolve => 
               setTimeout(resolve, Math.min(1000 * 2 ** retries, 30000))
           );
       },
       on: {
           error: (error) => {
               console.error('WebSocket error:', error);
           },
           closed: () => {
               console.log('WebSocket connection closed');
           }
       }
   }));
   ```

### Problem: "Subscription not receiving updates"

**Solutions:**

1. **Check Subscription Filters**
   ```javascript
   // Ensure filters match the data you expect
   const { data } = useSubscription(EXCEPTION_UPDATES, {
       variables: {
           filters: {
               severities: ['HIGH', 'CRITICAL'], // Only high/critical
               interfaceTypes: ['ORDER_COLLECTION'], // Specific interface
               includeResolved: false // Exclude resolved exceptions
           }
       }
   });
   ```

2. **Verify Subscription Permissions**
   ```javascript
   // Check if user has VIEWER role for subscriptions
   const hasSubscriptionPermission = (userRoles) => {
       return userRoles.includes('VIEWER') || 
              userRoles.includes('OPERATIONS') || 
              userRoles.includes('ADMIN');
   };
   ```

## Performance Problems

### Problem: "Slow query response times"

**Symptoms:**
- Queries taking longer than 5 seconds
- Timeout errors
- Poor user experience

**Solutions:**

1. **Optimize Query Structure**
   ```graphql
   # ❌ Inefficient - requests unnecessary data
   query IneffientQuery {
       exceptions {
           edges {
               node {
                   id
                   transactionId
                   originalPayload { content } # Expensive field
                   retryHistory { # Expensive nested data
                       resultErrorDetails
                   }
               }
           }
       }
   }

   # ✅ Efficient - only request needed fields
   query EfficientQuery {
       exceptions {
           edges {
               node {
                   id
                   transactionId
                   status
                   severity
                   timestamp
               }
           }
       }
   }
   ```

2. **Use Appropriate Pagination**
   ```javascript
   // ❌ Large page sizes
   const { data } = useQuery(GET_EXCEPTIONS, {
       variables: {
           pagination: { first: 100 } // Too large
       }
   });

   // ✅ Reasonable page sizes
   const { data } = useQuery(GET_EXCEPTIONS, {
       variables: {
           pagination: { first: 20 } // Optimal size
       }
   });
   ```

3. **Implement Caching**
   ```javascript
   const { data } = useQuery(GET_EXCEPTION_SUMMARY, {
       variables: { timeRange: { period: 'LAST_24_HOURS' } },
       fetchPolicy: 'cache-first', // Use cache when available
       pollInterval: 60000, // Refresh every minute
   });
   ```

### Problem: "High memory usage in browser"

**Solutions:**

1. **Limit Cache Size**
   ```javascript
   const cache = new InMemoryCache({
       typePolicies: {
           Query: {
               fields: {
                   exceptions: {
                       keyArgs: ['filters'],
                       merge(existing, incoming, { args }) {
                           // Limit cached items
                           const maxItems = 1000;
                           const allEdges = existing?.edges || [];
                           const newEdges = incoming.edges;
                           
                           const combined = [...allEdges, ...newEdges];
                           if (combined.length > maxItems) {
                               combined.splice(0, combined.length - maxItems);
                           }
                           
                           return {
                               ...incoming,
                               edges: combined
                           };
                       }
                   }
               }
           }
       }
   });
   ```

2. **Clean Up Subscriptions**
   ```javascript
   useEffect(() => {
       const subscription = client.subscribe({
           query: EXCEPTION_UPDATES
       }).subscribe({
           next: handleUpdate,
           error: handleError
       });

       // Clean up on unmount
       return () => {
           subscription.unsubscribe();
       };
   }, []);
   ```

## Network & Connectivity Issues

### Problem: "Network request failed"

**Solutions:**

1. **Implement Retry Logic**
   ```javascript
   const retryLink = new RetryLink({
       delay: {
           initial: 300,
           max: Infinity,
           jitter: true
       },
       attempts: {
           max: 3,
           retryIf: (error, _operation) => {
               return !!error && (
                   error.networkError?.statusCode >= 500 ||
                   !error.networkError // Network connectivity issues
               );
           }
       }
   });
   ```

2. **Handle Offline Scenarios**
   ```javascript
   const [isOnline, setIsOnline] = useState(navigator.onLine);

   useEffect(() => {
       const handleOnline = () => setIsOnline(true);
       const handleOffline = () => setIsOnline(false);

       window.addEventListener('online', handleOnline);
       window.addEventListener('offline', handleOffline);

       return () => {
           window.removeEventListener('online', handleOnline);
           window.removeEventListener('offline', handleOffline);
       };
   }, []);

   if (!isOnline) {
       return <div>You are offline. Please check your connection.</div>;
   }
   ```

### Problem: "CORS errors"

**Symptoms:**
```
Access to fetch at 'http://localhost:8080/graphql' from origin 'http://localhost:3000' 
has been blocked by CORS policy
```

**Solutions:**

1. **Server-Side CORS Configuration**
   ```java
   @Configuration
   public class CorsConfig {
       @Bean
       public CorsConfigurationSource corsConfigurationSource() {
           CorsConfiguration configuration = new CorsConfiguration();
           configuration.setAllowedOriginPatterns(Arrays.asList("*"));
           configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
           configuration.setAllowedHeaders(Arrays.asList("*"));
           configuration.setAllowCredentials(true);
           
           UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
           source.registerCorsConfiguration("/graphql", configuration);
           return source;
       }
   }
   ```

2. **Development Proxy Setup**
   ```json
   // package.json
   {
       "name": "my-app",
       "proxy": "http://localhost:8080",
       "scripts": {
           "start": "react-scripts start"
       }
   }
   ```

## Cache-Related Issues

### Problem: "Stale data in cache"

**Solutions:**

1. **Cache Invalidation**
   ```javascript
   // Invalidate specific cache entries
   const invalidateExceptionCache = (transactionId) => {
       client.cache.evict({
           id: client.cache.identify({ __typename: 'Exception', id: transactionId })
       });
       client.cache.gc(); // Garbage collect
   };

   // Invalidate all exceptions
   const invalidateAllExceptions = () => {
       client.cache.evict({ fieldName: 'exceptions' });
       client.cache.evict({ fieldName: 'exceptionSummary' });
   };
   ```

2. **Refetch Queries**
   ```javascript
   const { data, refetch } = useQuery(GET_EXCEPTIONS);

   // Refetch when needed
   const handleRefresh = () => {
       refetch();
   };

   // Or refetch all active queries
   const refetchAllQueries = () => {
       client.refetchQueries({
           include: 'active'
       });
   };
   ```

### Problem: "Cache normalization issues"

**Solutions:**

1. **Proper Type Policies**
   ```javascript
   const cache = new InMemoryCache({
       typePolicies: {
           Exception: {
               keyFields: ['transactionId'], // Use transactionId as key
           },
           RetryAttempt: {
               keyFields: ['id'],
           }
       }
   });
   ```

2. **Fragment Matching**
   ```javascript
   // Ensure fragments match the actual data structure
   const EXCEPTION_FRAGMENT = gql`
       fragment ExceptionInfo on Exception {
           id
           transactionId
           status
           # Include __typename for proper cache normalization
           __typename
       }
   `;
   ```

## Error Codes Reference

| Error Code | Description | Common Causes | Solutions |
|------------|-------------|---------------|-----------|
| `AUTHORIZATION_ERROR` | Authentication/authorization failed | Invalid token, insufficient permissions | Check token, verify roles |
| `VALIDATION_ERROR` | Input validation failed | Missing required fields, invalid format | Validate inputs before sending |
| `NOT_FOUND` | Resource not found | Invalid ID, deleted resource | Verify resource exists |
| `RATE_LIMIT_EXCEEDED` | Too many requests | Exceeding rate limits | Implement request throttling |
| `QUERY_COMPLEXITY_EXCEEDED` | Query too complex | Deep nesting, large result sets | Simplify query, use pagination |
| `EXTERNAL_SERVICE_ERROR` | External service unavailable | Service downtime, network issues | Implement retry logic, fallbacks |
| `BUSINESS_RULE_ERROR` | Business logic violation | Invalid operation state | Check business rules |
| `INTERNAL_ERROR` | Server error | Server bugs, configuration issues | Check server logs, contact support |

## Debugging Tools & Techniques

### 1. GraphQL Playground/GraphiQL

Access the interactive GraphQL explorer at `/graphiql` (development only):

```javascript
// Enable GraphiQL in development
if (process.env.NODE_ENV === 'development') {
    window.location.href = '/graphiql';
}
```

### 2. Apollo Client DevTools

Install the Apollo Client DevTools browser extension for:
- Query inspection
- Cache exploration
- Mutation tracking
- Subscription monitoring

### 3. Network Debugging

```javascript
// Log all GraphQL operations
const loggingLink = new ApolloLink((operation, forward) => {
    console.log(`GraphQL Operation: ${operation.operationName}`);
    console.log('Variables:', operation.variables);
    
    return forward(operation).map((response) => {
        console.log('Response:', response);
        return response;
    });
});
```

### 4. Server-Side Logging

Enable detailed logging on the server:

```yaml
# application.yml
logging:
  level:
    com.arcone.biopro.exception.collector.api.graphql: DEBUG
    graphql.execution: DEBUG
    org.springframework.security: DEBUG
```

### 5. Health Check Queries

```graphql
query HealthCheck {
    systemHealth {
        status
        database {
            status
            responseTime
        }
        cache {
            status
            responseTime
        }
        externalServices {
            serviceName
            status
            lastChecked
        }
    }
}
```

This troubleshooting guide should help resolve most common issues encountered when working with the GraphQL API. For issues not covered here, check the server logs and contact the development team with specific error messages and reproduction steps.