# GraphQL API Documentation

## Interface Exception Collector GraphQL API

### Overview

The Interface Exception Collector GraphQL API provides a unified, type-safe interface for querying and managing interface exception data. This API serves as the primary data layer for the BioPro Operations Dashboard, offering real-time updates, comprehensive filtering, and operational capabilities.

### API Endpoint

- **GraphQL Endpoint**: `https://your-domain.com/graphql`
- **GraphQL Subscriptions**: `wss://your-domain.com/subscriptions`
- **GraphiQL Interface** (Development): `https://your-domain.com/graphiql`

### Authentication

All GraphQL operations require JWT authentication. Include the JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

### Schema Overview

The API is organized into three main operation types:

- **Queries**: Read operations for fetching exception data and statistics
- **Mutations**: Write operations for retrying exceptions and updating status
- **Subscriptions**: Real-time updates for live dashboard functionality

## Core Types

### Exception

The main exception entity containing all exception details:

```graphql
type Exception {
    id: ID!
    transactionId: String!
    externalId: String
    interfaceType: InterfaceType!
    exceptionReason: String!
    operation: String!
    status: ExceptionStatus!
    severity: ExceptionSeverity!
    category: ExceptionCategory!
    customerId: String
    locationCode: String
    timestamp: DateTime!
    processedAt: DateTime!
    retryable: Boolean!
    retryCount: Int!
    maxRetries: Int!
    lastRetryAt: DateTime
    acknowledgedBy: String
    acknowledgedAt: DateTime
    
    # Nested objects (lazy loaded)
    originalPayload: OriginalPayload
    retryHistory: [RetryAttempt!]!
    statusHistory: [StatusChange!]!
}
```

### Key Enums

#### InterfaceType
```graphql
enum InterfaceType {
    ORDER_COLLECTION
    ORDER_DISTRIBUTION
    CUSTOMER_SYNC
    INVENTORY_UPDATE
    PAYMENT_PROCESSING
    NOTIFICATION_DELIVERY
}
```

#### ExceptionStatus
```graphql
enum ExceptionStatus {
    NEW
    ACKNOWLEDGED
    IN_PROGRESS
    RESOLVED
    FAILED
    CANCELLED
}
```

#### ExceptionSeverity
```graphql
enum ExceptionSeverity {
    LOW
    MEDIUM
    HIGH
    CRITICAL
}
```

## Queries

### 1. List Exceptions

Retrieve a paginated list of exceptions with filtering and sorting:

```graphql
query GetExceptions($filters: ExceptionFilters, $pagination: PaginationInput, $sorting: SortingInput) {
    exceptions(filters: $filters, pagination: $pagination, sorting: $sorting) {
        edges {
            node {
                id
                transactionId
                interfaceType
                exceptionReason
                status
                severity
                timestamp
                customerId
                locationCode
            }
            cursor
        }
        pageInfo {
            hasNextPage
            hasPreviousPage
            startCursor
            endCursor
        }
        totalCount
    }
}
```

**Variables:**
```json
{
    "filters": {
        "interfaceTypes": ["ORDER_COLLECTION", "ORDER_DISTRIBUTION"],
        "statuses": ["NEW", "ACKNOWLEDGED"],
        "severities": ["HIGH", "CRITICAL"],
        "dateRange": {
            "from": "2024-01-01T00:00:00Z",
            "to": "2024-01-31T23:59:59Z"
        },
        "excludeResolved": true
    },
    "pagination": {
        "first": 20
    },
    "sorting": {
        "field": "timestamp",
        "direction": "DESC"
    }
}
```

### 2. Get Exception Details

Retrieve detailed information for a specific exception:

```graphql
query GetExceptionDetails($transactionId: String!) {
    exception(transactionId: $transactionId) {
        id
        transactionId
        externalId
        interfaceType
        exceptionReason
        operation
        status
        severity
        category
        customerId
        locationCode
        timestamp
        processedAt
        retryable
        retryCount
        maxRetries
        lastRetryAt
        acknowledgedBy
        acknowledgedAt
        
        originalPayload {
            content
            contentType
            retrievedAt
            sourceService
        }
        
        retryHistory {
            id
            attemptNumber
            status
            initiatedBy
            initiatedAt
            completedAt
            resultSuccess
            resultMessage
            resultResponseCode
        }
        
        statusHistory {
            id
            fromStatus
            toStatus
            changedBy
            changedAt
            reason
            notes
        }
    }
}
```

### 3. Exception Summary Statistics

Get aggregated statistics for dashboard displays:

```graphql
query GetExceptionSummary($timeRange: TimeRange!, $filters: ExceptionFilters) {
    exceptionSummary(timeRange: $timeRange, filters: $filters) {
        totalExceptions
        
        byInterfaceType {
            interfaceType
            count
            percentage
        }
        
        bySeverity {
            severity
            count
            percentage
        }
        
        byStatus {
            status
            count
            percentage
        }
        
        trends {
            timestamp
            count
            interfaceType
        }
        
        keyMetrics {
            retrySuccessRate
            averageResolutionTime
            customerImpactCount
            criticalExceptionCount
        }
    }
}
```

### 4. Search Exceptions

Perform advanced text search across exception data:

```graphql
query SearchExceptions($search: SearchInput!, $pagination: PaginationInput) {
    searchExceptions(search: $search, pagination: $pagination) {
        edges {
            node {
                id
                transactionId
                interfaceType
                exceptionReason
                status
                severity
                timestamp
            }
            cursor
        }
        pageInfo {
            hasNextPage
            hasPreviousPage
        }
        totalCount
    }
}
```

## Mutations

### 1. Retry Exception

Initiate a retry operation for a failed exception:

```graphql
mutation RetryException($input: RetryExceptionInput!) {
    retryException(input: $input) {
        success
        exception {
            id
            transactionId
            status
            retryCount
            lastRetryAt
        }
        retryAttempt {
            id
            attemptNumber
            status
            initiatedBy
            initiatedAt
        }
        errors {
            message
            code
            path
        }
    }
}
```

**Variables:**
```json
{
    "input": {
        "transactionId": "TXN-12345",
        "reason": "Manual retry after service restoration",
        "priority": "HIGH",
        "notes": "Service is back online, retrying failed order"
    }
}
```

### 2. Acknowledge Exception

Acknowledge an exception to indicate it's being handled:

```graphql
mutation AcknowledgeException($input: AcknowledgeExceptionInput!) {
    acknowledgeException(input: $input) {
        success
        exception {
            id
            transactionId
            status
            acknowledgedBy
            acknowledgedAt
        }
        errors {
            message
            code
        }
    }
}
```

### 3. Bulk Operations

Perform operations on multiple exceptions:

```graphql
mutation BulkRetryExceptions($input: BulkRetryInput!) {
    bulkRetryExceptions(input: $input) {
        successCount
        failureCount
        results {
            success
            exception {
                transactionId
                status
            }
            errors {
                message
                code
            }
        }
    }
}
```

### 4. Resolve Exception

Mark an exception as resolved:

```graphql
mutation ResolveException($transactionId: String!, $resolutionMethod: ResolutionMethod!, $notes: String) {
    resolveException(
        transactionId: $transactionId
        resolutionMethod: $resolutionMethod
        notes: $notes
    ) {
        success
        exception {
            id
            transactionId
            status
        }
        errors {
            message
            code
        }
    }
}
```

## Subscriptions

### 1. Exception Updates

Subscribe to real-time exception updates:

```graphql
subscription ExceptionUpdates($filters: SubscriptionFilters) {
    exceptionUpdated(filters: $filters) {
        eventType
        exception {
            id
            transactionId
            interfaceType
            status
            severity
            timestamp
        }
        timestamp
        triggeredBy
    }
}
```

### 2. Summary Updates

Subscribe to real-time summary statistics:

```graphql
subscription SummaryUpdates($timeRange: TimeRange!) {
    summaryUpdated(timeRange: $timeRange) {
        totalExceptions
        byInterfaceType {
            interfaceType
            count
        }
        keyMetrics {
            retrySuccessRate
            criticalExceptionCount
        }
    }
}
```

### 3. Retry Status Updates

Subscribe to retry operation status changes:

```graphql
subscription RetryStatusUpdates($transactionId: String) {
    retryStatusUpdated(transactionId: $transactionId) {
        transactionId
        retryAttempt {
            attemptNumber
            status
            resultSuccess
        }
        eventType
        timestamp
    }
}
```

## Error Handling

The API uses structured error responses with specific error codes:

```graphql
type GraphQLError {
    message: String!
    code: ErrorCode!
    path: [String!]
    extensions: JSON
}

enum ErrorCode {
    VALIDATION_ERROR
    AUTHORIZATION_ERROR
    NOT_FOUND
    EXTERNAL_SERVICE_ERROR
    BUSINESS_RULE_ERROR
    INTERNAL_ERROR
    RATE_LIMIT_EXCEEDED
    QUERY_COMPLEXITY_EXCEEDED
}
```

### Common Error Scenarios

1. **Validation Error**: Invalid input parameters
2. **Authorization Error**: Insufficient permissions
3. **Not Found**: Exception doesn't exist
4. **Rate Limit Exceeded**: Too many requests
5. **Query Complexity Exceeded**: Query too complex

## Performance Considerations

### Query Complexity

The API implements query complexity analysis with a maximum limit of 1000 complexity points. Complex nested queries may be rejected.

### Rate Limiting

Rate limits are enforced per user:
- **Queries**: 100 requests per minute
- **Mutations**: 50 requests per minute
- **Subscriptions**: 10 concurrent connections

### Caching

The API uses Redis caching for performance:
- **Summary queries**: 5-minute TTL
- **Exception details**: 1-hour TTL
- **Payload data**: 24-hour TTL

### Pagination

Use cursor-based pagination for large result sets:
- Maximum page size: 100 items
- Use `first` and `after` for forward pagination
- Use `last` and `before` for backward pagination

## Security

### Authentication

JWT tokens must include the following claims:
- `sub`: User ID
- `roles`: Array of user roles
- `exp`: Token expiration
- `iat`: Token issued at

### Authorization

Role-based access control:
- **ADMIN**: Full access to all operations
- **OPERATIONS**: Read access + retry/acknowledge operations
- **VIEWER**: Read-only access

### Field-Level Security

Some fields require specific permissions:
- `originalPayload`: Requires OPERATIONS or ADMIN role
- Mutation operations: Require OPERATIONS or ADMIN role
- System health queries: Require ADMIN role

## Best Practices

### 1. Use Fragments for Reusable Fields

```graphql
fragment ExceptionBasicInfo on Exception {
    id
    transactionId
    interfaceType
    status
    severity
    timestamp
}

query GetExceptions {
    exceptions {
        edges {
            node {
                ...ExceptionBasicInfo
                exceptionReason
                customerId
            }
        }
    }
}
```

### 2. Implement Proper Error Handling

```javascript
const result = await client.query({
    query: GET_EXCEPTIONS,
    variables: { filters },
    errorPolicy: 'all'
});

if (result.errors) {
    result.errors.forEach(error => {
        console.error(`GraphQL Error: ${error.message}`, error.extensions);
    });
}
```

### 3. Use Subscriptions Efficiently

```javascript
const subscription = client.subscribe({
    query: EXCEPTION_UPDATES,
    variables: { filters: { severities: ['HIGH', 'CRITICAL'] } }
}).subscribe({
    next: (data) => {
        // Handle real-time update
        updateDashboard(data.exceptionUpdated);
    },
    error: (err) => {
        console.error('Subscription error:', err);
        // Implement reconnection logic
    }
});
```

### 4. Optimize Query Performance

- Request only needed fields
- Use appropriate pagination sizes
- Implement client-side caching
- Batch multiple operations when possible

## Health Monitoring

### System Health Query

```graphql
query SystemHealth {
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
            responseTime
            lastChecked
        }
        lastUpdated
    }
}
```

This query provides real-time system health information for monitoring and alerting purposes.