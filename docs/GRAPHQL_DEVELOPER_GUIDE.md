# GraphQL API Developer Guide

## Overview

The Interface Exception Collector Service provides a comprehensive GraphQL API for managing and monitoring interface exceptions across various business systems. This guide covers all available operations, authentication, and usage examples for developers consuming the GraphQL API.

## Table of Contents

- [Getting Started](#getting-started)
- [Authentication](#authentication)
- [Endpoints](#endpoints)
- [Queries](#queries)
- [Mutations](#mutations)
- [Subscriptions](#subscriptions)
- [Types and Enums](#types-and-enums)
- [Error Handling](#error-handling)
- [Rate Limiting](#rate-limiting)
- [Examples](#examples)

## Getting Started

### GraphQL Endpoint
- **URL**: `https://your-domain.com/graphql`
- **Method**: POST
- **Content-Type**: `application/json`

### GraphiQL Interface (Development Only)
- **URL**: `https://your-domain.com/graphiql`
- Interactive query interface for testing and exploration

### WebSocket Subscriptions
- **URL**: `wss://your-domain.com/subscriptions`
- Real-time updates via WebSocket connection

## Authentication

All GraphQL operations require JWT authentication via the `Authorization` header:

```
Authorization: Bearer <your-jwt-token>
```

### Role-Based Access Control

- **VIEWER**: Can query exception data and subscribe to updates
- **OPERATIONS**: Can perform retry and acknowledgment operations
- **ADMIN**: Full access to all operations including resolution and cancellation

## Endpoints

### Schema Documentation
- **GET** `/graphql/schema` - Returns the complete GraphQL schema (development only)
- **GET** `/graphql/info` - API documentation and examples (development only)
- **GET** `/graphql/examples` - Query examples in JSON format (development only)

## Queries

### 1. List Exceptions with Filtering and Pagination

Retrieve a paginated list of exceptions with comprehensive filtering options.

**Operation**: `exceptions`

**Parameters**:
- `filters` (ExceptionFilters, optional): Filtering criteria
- `pagination` (PaginationInput, optional): Cursor-based pagination
- `sorting` (SortingInput, optional): Sort configuration

**Example**:
```graphql
query ListExceptions($filters: ExceptionFilters, $pagination: PaginationInput, $sorting: SortingInput) {
  exceptions(filters: $filters, pagination: $pagination, sorting: $sorting) {
    edges {
      node {
        id
        transactionId
        interfaceType
        exceptionReason
        status
        severity
        category
        timestamp
        retryCount
        maxRetries
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

**Variables**:
```json
{
  "filters": {
    "interfaceTypes": ["ORDER_COLLECTION", "ORDER_DISTRIBUTION"],
    "statuses": ["NEW", "ACKNOWLEDGED"],
    "severities": ["HIGH", "CRITICAL"],
    "dateRange": {
      "from": "2024-01-01T00:00:00Z",
      "to": "2024-12-31T23:59:59Z"
    },
    "customerIds": ["CUST001", "CUST002"],
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

### 2. Get Single Exception Details

Retrieve detailed information about a specific exception.

**Operation**: `exception`

**Parameters**:
- `transactionId` (String!, required): Unique transaction identifier

**Example**:
```graphql
query GetException($transactionId: String!) {
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

**Variables**:
```json
{
  "transactionId": "TXN-12345-67890"
}
```

### 3. Exception Summary Statistics

Get aggregated statistics and trends for dashboard displays.

**Operation**: `exceptionSummary`

**Parameters**:
- `timeRange` (TimeRange!, required): Time period for statistics
- `filters` (ExceptionFilters, optional): Additional filtering

**Example**:
```graphql
query ExceptionSummary($timeRange: TimeRange!, $filters: ExceptionFilters) {
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

**Variables**:
```json
{
  "timeRange": {
    "period": "LAST_7_DAYS"
  },
  "filters": {
    "interfaceTypes": ["ORDER_COLLECTION"]
  }
}
```

### 4. Search Exceptions

Advanced search functionality with fuzzy matching.

**Operation**: `searchExceptions`

**Parameters**:
- `search` (SearchInput!, required): Search criteria
- `pagination` (PaginationInput, optional): Pagination
- `sorting` (SortingInput, optional): Sort configuration

**Example**:
```graphql
query SearchExceptions($search: SearchInput!, $pagination: PaginationInput) {
  searchExceptions(search: $search, pagination: $pagination) {
    edges {
      node {
        transactionId
        exceptionReason
        interfaceType
        status
        severity
        timestamp
      }
    }
    totalCount
  }
}
```

**Variables**:
```json
{
  "search": {
    "query": "timeout error",
    "fields": ["EXCEPTION_REASON", "OPERATION"],
    "fuzzy": true
  },
  "pagination": {
    "first": 10
  }
}
```

### 5. System Health

Check the health status of the system and its components.

**Operation**: `systemHealth`

**Example**:
```graphql
query SystemHealth {
  systemHealth {
    status
    database {
      status
      responseTime
      details
    }
    cache {
      status
      responseTime
      details
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

## Mutations

### 1. Retry Single Exception

Initiate a retry operation for a specific exception.

**Operation**: `retryException`

**Parameters**:
- `input` (RetryExceptionInput!, required): Retry configuration

**Example**:
```graphql
mutation RetryException($input: RetryExceptionInput!) {
  retryException(input: $input) {
    success
    exception {
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

**Variables**:
```json
{
  "input": {
    "transactionId": "TXN-12345-67890",
    "reason": "Manual retry after system fix",
    "priority": "HIGH",
    "notes": "Retrying after resolving connectivity issue"
  }
}
```

### 2. Bulk Retry Exceptions

Retry multiple exceptions in a single operation.

**Operation**: `bulkRetryExceptions`

**Parameters**:
- `input` (BulkRetryInput!, required): Bulk retry configuration

**Example**:
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
    errors {
      message
      code
    }
  }
}
```

**Variables**:
```json
{
  "input": {
    "transactionIds": ["TXN-001", "TXN-002", "TXN-003"],
    "reason": "Bulk retry after system maintenance",
    "priority": "NORMAL"
  }
}
```

### 3. Acknowledge Exception

Acknowledge an exception to indicate it's being handled.

**Operation**: `acknowledgeException`

**Parameters**:
- `input` (AcknowledgeExceptionInput!, required): Acknowledgment details

**Example**:
```graphql
mutation AcknowledgeException($input: AcknowledgeExceptionInput!) {
  acknowledgeException(input: $input) {
    success
    exception {
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

**Variables**:
```json
{
  "input": {
    "transactionId": "TXN-12345-67890",
    "reason": "Issue acknowledged by operations team",
    "notes": "Investigating root cause with external service provider",
    "estimatedResolutionTime": "2024-01-15T14:00:00Z",
    "assignedTo": "ops-team-lead"
  }
}
```

### 4. Bulk Acknowledge Exceptions

Acknowledge multiple exceptions simultaneously.

**Operation**: `bulkAcknowledgeExceptions`

**Parameters**:
- `input` (BulkAcknowledgeInput!, required): Bulk acknowledgment details

**Example**:
```graphql
mutation BulkAcknowledgeExceptions($input: BulkAcknowledgeInput!) {
  bulkAcknowledgeExceptions(input: $input) {
    successCount
    failureCount
    results {
      success
      exception {
        transactionId
        status
        acknowledgedBy
      }
      errors {
        message
        code
      }
    }
  }
}
```

**Variables**:
```json
{
  "input": {
    "transactionIds": ["TXN-001", "TXN-002", "TXN-003"],
    "reason": "Bulk acknowledgment for maintenance window",
    "notes": "All exceptions related to scheduled maintenance",
    "assignedTo": "maintenance-team"
  }
}
```

### 5. Resolve Exception

Mark an exception as resolved with resolution details.

**Operation**: `resolveException`

**Parameters**:
- `input` (ResolveExceptionInput!, required): Resolution details

**Example**:
```graphql
mutation ResolveException($input: ResolveExceptionInput!) {
  resolveException(input: $input) {
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
```

**Variables**:
```json
{
  "input": {
    "transactionId": "TXN-12345-67890",
    "resolutionMethod": "MANUAL_RESOLUTION",
    "resolutionNotes": "Issue resolved by updating customer data in external system"
  }
}
```

### 6. Cancel Retry

Cancel a pending retry operation.

**Operation**: `cancelRetry`

**Parameters**:
- `transactionId` (String!, required): Transaction identifier
- `reason` (String!, required): Cancellation reason

**Example**:
```graphql
mutation CancelRetry($transactionId: String!, $reason: String!) {
  cancelRetry(transactionId: $transactionId, reason: $reason) {
    success
    exception {
      transactionId
      status
      retryCount
    }
    errors {
      message
      code
    }
  }
}
```

**Variables**:
```json
{
  "transactionId": "TXN-12345-67890",
  "reason": "Retry cancelled due to business rule change"
}
```

## Subscriptions

### 1. Real-time Exception Updates

Subscribe to real-time exception events with filtering.

**Operation**: `exceptionUpdated`

**Parameters**:
- `filters` (SubscriptionFilters, optional): Event filtering criteria

**Example**:
```graphql
subscription ExceptionUpdates($filters: SubscriptionFilters) {
  exceptionUpdated(filters: $filters) {
    eventType
    exception {
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

**Variables**:
```json
{
  "filters": {
    "interfaceTypes": ["ORDER_COLLECTION"],
    "severities": ["HIGH", "CRITICAL"],
    "includeResolved": false
  }
}
```

### 2. Retry Status Updates

Subscribe to retry operation status changes.

**Operation**: `retryStatusUpdated`

**Parameters**:
- `transactionId` (String, optional): Filter by specific transaction

**Example**:
```graphql
subscription RetryStatusUpdates($transactionId: String) {
  retryStatusUpdated(transactionId: $transactionId) {
    transactionId
    retryAttempt {
      attemptNumber
      status
      initiatedBy
      initiatedAt
      completedAt
      resultSuccess
    }
    eventType
    timestamp
  }
}
```

**Variables**:
```json
{
  "transactionId": "TXN-12345-67890"
}
```

### 3. Summary Statistics Updates

Subscribe to real-time summary statistics updates.

**Operation**: `summaryUpdated`

**Parameters**:
- `timeRange` (TimeRange!, required): Time range for statistics

**Example**:
```graphql
subscription SummaryUpdates($timeRange: TimeRange!) {
  summaryUpdated(timeRange: $timeRange) {
    totalExceptions
    byInterfaceType {
      interfaceType
      count
      percentage
    }
    keyMetrics {
      retrySuccessRate
      criticalExceptionCount
    }
  }
}
```

**Variables**:
```json
{
  "timeRange": {
    "period": "LAST_HOUR"
  }
}
```

## Types and Enums

### Interface Types
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

### Exception Status
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

### Exception Severity
```graphql
enum ExceptionSeverity {
  LOW
  MEDIUM
  HIGH
  CRITICAL
}
```

### Exception Categories
```graphql
enum ExceptionCategory {
  VALIDATION_ERROR
  CONNECTIVITY_ERROR
  TIMEOUT_ERROR
  AUTHENTICATION_ERROR
  BUSINESS_RULE_ERROR
  DATA_FORMAT_ERROR
  EXTERNAL_SERVICE_ERROR
}
```

### Retry Priority
```graphql
enum RetryPriority {
  LOW
  NORMAL
  HIGH
  URGENT
}
```

### Time Periods
```graphql
enum TimePeriod {
  LAST_HOUR
  LAST_24_HOURS
  LAST_7_DAYS
  LAST_30_DAYS
  CUSTOM
}
```

## Error Handling

### Error Codes
- `VALIDATION_ERROR`: Input validation failed
- `AUTHORIZATION_ERROR`: Insufficient permissions
- `NOT_FOUND`: Resource not found
- `EXTERNAL_SERVICE_ERROR`: External service unavailable
- `BUSINESS_RULE_ERROR`: Business rule violation
- `INTERNAL_ERROR`: Internal system error
- `RATE_LIMIT_EXCEEDED`: Rate limit exceeded
- `QUERY_COMPLEXITY_EXCEEDED`: Query too complex

### Error Response Format
```json
{
  "errors": [
    {
      "message": "Exception not found",
      "code": "NOT_FOUND",
      "path": ["exception"],
      "extensions": {
        "transactionId": "TXN-12345-67890"
      }
    }
  ]
}
```

## Rate Limiting

### Limits by Role
- **ADMIN**: 300 requests/minute, 10,000 requests/hour
- **OPERATIONS**: 120 requests/minute, 3,600 requests/hour
- **VIEWER**: 60 requests/minute, 1,800 requests/hour

### Query Complexity Limits
- **Development**: Maximum depth 15, complexity 2000
- **Production**: Maximum depth 10, complexity 1000

## Performance Guidelines

### Response Time Targets
- **List queries**: 500ms (95th percentile)
- **Detail queries**: 1s (95th percentile)
- **Summary queries**: 200ms (95th percentile)
- **Mutations**: 2s (95th percentile)

### Best Practices
1. Use pagination for large result sets
2. Apply filters to reduce data volume
3. Request only needed fields
4. Use subscriptions for real-time updates
5. Cache summary data when possible

## WebSocket Connection

### Connection Setup
```javascript
const client = new ApolloClient({
  uri: 'https://your-domain.com/graphql',
  wsUri: 'wss://your-domain.com/subscriptions',
  connectionParams: {
    Authorization: 'Bearer your-jwt-token'
  }
});
```

### Subscription Lifecycle
1. Establish WebSocket connection
2. Send subscription query
3. Receive real-time events
4. Handle connection errors and reconnection
5. Clean up on component unmount

## Development Tools

### GraphiQL Interface
Access the interactive GraphiQL interface at `/graphiql` for:
- Schema exploration
- Query testing
- Documentation browsing
- Real-time query execution

### Schema Introspection
Query the schema programmatically:
```graphql
query IntrospectionQuery {
  __schema {
    types {
      name
      description
      fields {
        name
        type {
          name
        }
      }
    }
  }
}
```

## Support and Resources

### Documentation Endpoints
- **Schema**: `GET /graphql/schema`
- **API Info**: `GET /graphql/info`
- **Examples**: `GET /graphql/examples`

### Monitoring
- Query performance metrics available via `/actuator/metrics`
- Health checks at `/actuator/health`
- GraphQL-specific metrics at `/actuator/graphql`

For additional support, contact the API team or refer to the main project documentation.