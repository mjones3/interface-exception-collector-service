# Interface Exception Collector - GraphQL API Guide for AngularJS Developers

## Overview

This comprehensive guide provides AngularJS developers with everything needed to integrate with the Interface Exception Collector GraphQL API. The API provides real-time exception management, retry operations, and comprehensive monitoring capabilities for interface systems.

**Base URL**: `http://your-service-host:8080/graphql`  
**WebSocket URL**: `ws://your-service-host:8080/subscriptions`

## Table of Contents

1. [Authentication & Setup](#authentication--setup)
2. [Queries](#queries)
3. [Mutations](#mutations)
4. [Subscriptions](#subscriptions)
5. [Types & Enums](#types--enums)
6. [Error Handling](#error-handling)
7. [AngularJS Integration Examples](#angularjs-integration-examples)

---

## Authentication & Setup

### Required Headers
```javascript
{
  'Content-Type': 'application/json',
  'Authorization': 'Bearer YOUR_JWT_TOKEN'
}
```

### AngularJS HTTP Configuration
```javascript
// Configure your AngularJS app
angular.module('exceptionApp', [])
.config(['$httpProvider', function($httpProvider) {
  $httpProvider.defaults.headers.common['Authorization'] = 'Bearer ' + getJwtToken();
  $httpProvider.defaults.headers.post['Content-Type'] = 'application/json';
}]);
```

---

## Queries

### 1. Get Paginated Exception List

**Business Purpose**: Retrieve a filtered, paginated list of interface exceptions for dashboard display and management.

**Query**:
```graphql
query GetExceptions($filters: ExceptionFilters, $pagination: PaginationInput, $sorting: SortingInput) {
  exceptions(filters: $filters, pagination: $pagination, sorting: $sorting) {
    edges {
      node {
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
    "interfaceTypes": ["ORDER_COLLECTION", "PAYMENT_PROCESSING"],
    "statuses": ["NEW", "ACKNOWLEDGED"],
    "severities": ["HIGH", "CRITICAL"],
    "dateRange": {
      "from": "2024-01-01T00:00:00Z",
      "to": "2024-01-31T23:59:59Z"
    },
    "customerIds": ["CUST001", "CUST002"],
    "excludeResolved": true
  },
  "pagination": {
    "first": 20,
    "after": null
  },
  "sorting": {
    "field": "timestamp",
    "direction": "DESC"
  }
}
```

**Expected Response**:
```json
{
  "data": {
    "exceptions": {
      "edges": [
        {
          "node": {
            "id": "12345",
            "transactionId": "TXN-2024-001",
            "externalId": "EXT-001",
            "interfaceType": "ORDER_COLLECTION",
            "exceptionReason": "Invalid customer ID format",
            "operation": "CREATE_ORDER",
            "status": "NEW",
            "severity": "HIGH",
            "category": "VALIDATION_ERROR",
            "customerId": "CUST001",
            "locationCode": "NYC01",
            "timestamp": "2024-01-15T10:30:00Z",
            "processedAt": "2024-01-15T10:30:05Z",
            "retryable": true,
            "retryCount": 0,
            "maxRetries": 3,
            "lastRetryAt": null,
            "acknowledgedBy": null,
            "acknowledgedAt": null
          },
          "cursor": "Y3Vyc29yMQ=="
        }
      ],
      "pageInfo": {
        "hasNextPage": true,
        "hasPreviousPage": false,
        "startCursor": "Y3Vyc29yMQ==",
        "endCursor": "Y3Vyc29yMjA="
      },
      "totalCount": 150
    }
  }
}
```

**AngularJS Example**:
```javascript
// Service
angular.module('exceptionApp').service('ExceptionService', ['$http', function($http) {
  this.getExceptions = function(filters, pagination, sorting) {
    return $http.post('/graphql', {
      query: `query GetExceptions($filters: ExceptionFilters, $pagination: PaginationInput, $sorting: SortingInput) {
        exceptions(filters: $filters, pagination: $pagination, sorting: $sorting) {
          edges { node { id transactionId interfaceType exceptionReason status severity timestamp } cursor }
          pageInfo { hasNextPage hasPreviousPage startCursor endCursor }
          totalCount
        }
      }`,
      variables: { filters: filters, pagination: pagination, sorting: sorting }
    });
  };
}]);

// Controller
angular.module('exceptionApp').controller('ExceptionListController', 
['$scope', 'ExceptionService', function($scope, ExceptionService) {
  $scope.exceptions = [];
  $scope.loading = false;
  
  $scope.loadExceptions = function() {
    $scope.loading = true;
    ExceptionService.getExceptions($scope.filters, $scope.pagination, $scope.sorting)
      .then(function(response) {
        $scope.exceptions = response.data.data.exceptions.edges.map(edge => edge.node);
        $scope.pageInfo = response.data.data.exceptions.pageInfo;
        $scope.totalCount = response.data.data.exceptions.totalCount;
      })
      .finally(function() {
        $scope.loading = false;
      });
  };
}]);
```

### 2. Get Single Exception Details

**Business Purpose**: Retrieve detailed information about a specific exception for investigation and management.

**Query**:
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
  "transactionId": "TXN-2024-001"
}
```

### 3. Search Exceptions

**Business Purpose**: Perform full-text search across exception data for quick problem identification.

**Query**:
```graphql
query SearchExceptions($search: SearchInput!, $pagination: PaginationInput, $sorting: SortingInput) {
  searchExceptions(search: $search, pagination: $pagination, sorting: $sorting) {
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

**Variables**:
```json
{
  "search": {
    "query": "payment timeout",
    "fields": ["EXCEPTION_REASON", "OPERATION"],
    "fuzzy": true
  },
  "pagination": {
    "first": 10
  }
}
```

### 4. Get Exception Summary Statistics

**Business Purpose**: Retrieve aggregated metrics and trends for dashboard displays and reporting.

**Query**:
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

**Variables**:
```json
{
  "timeRange": {
    "period": "LAST_7_DAYS"
  },
  "filters": {
    "severities": ["HIGH", "CRITICAL"]
  }
}
```

### 5. System Health Check

**Business Purpose**: Monitor system health including database, cache, and external service connectivity.

**Query**:
```graphql
query GetSystemHealth {
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

---

## Mutations

### 1. Retry Exception

**Business Purpose**: Initiate a retry operation for a failed interface transaction.

**Mutation**:
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
    operationId
    timestamp
    performedBy
    retryPriority
    retryReason
    attemptNumber
  }
}
```

**Variables**:
```json
{
  "input": {
    "transactionId": "TXN-2024-001",
    "reason": "Customer data has been corrected, retrying order processing",
    "priority": "HIGH",
    "notes": "Customer contacted support, issue resolved on their end"
  }
}
```

**Expected Response**:
```json
{
  "data": {
    "retryException": {
      "success": true,
      "exception": {
        "id": "12345",
        "transactionId": "TXN-2024-001",
        "status": "IN_PROGRESS",
        "retryCount": 1,
        "lastRetryAt": "2024-01-15T14:30:00Z"
      },
      "retryAttempt": {
        "id": "67890",
        "attemptNumber": 1,
        "status": "IN_PROGRESS",
        "initiatedBy": "john.doe@company.com",
        "initiatedAt": "2024-01-15T14:30:00Z"
      },
      "errors": [],
      "operationId": "OP-2024-001",
      "timestamp": "2024-01-15T14:30:00Z",
      "performedBy": "john.doe@company.com",
      "retryPriority": "HIGH",
      "retryReason": "Customer data has been corrected, retrying order processing",
      "attemptNumber": 1
    }
  }
}
```

### 2. Bulk Retry Exceptions

**Business Purpose**: Retry multiple exceptions simultaneously for batch processing efficiency.

**Mutation**:
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
    "transactionIds": ["TXN-2024-001", "TXN-2024-002", "TXN-2024-003"],
    "reason": "System maintenance completed, retrying failed transactions",
    "priority": "NORMAL"
  }
}
```

### 3. Acknowledge Exception

**Business Purpose**: Mark an exception as acknowledged to indicate it's being investigated.

**Mutation**:
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
    operationId
    timestamp
    performedBy
    acknowledgmentReason
    acknowledgmentNotes
  }
}
```

**Variables**:
```json
{
  "input": {
    "transactionId": "TXN-2024-001",
    "reason": "Investigating customer data validation issue",
    "notes": "Customer reported incorrect address format, checking validation rules"
  }
}
```

### 4. Resolve Exception

**Business Purpose**: Mark an exception as resolved when the underlying issue has been fixed.

**Mutation**:
```graphql
mutation ResolveException($input: ResolveExceptionInput!) {
  resolveException(input: $input) {
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
    operationId
    timestamp
    performedBy
    resolutionMethod
    resolutionNotes
  }
}
```

**Variables**:
```json
{
  "input": {
    "transactionId": "TXN-2024-001",
    "resolutionMethod": "MANUAL_RESOLUTION",
    "resolutionNotes": "Customer data corrected manually, order processed successfully"
  }
}
```

### 5. Cancel Retry

**Business Purpose**: Cancel a pending retry operation when it's no longer needed.

**Mutation**:
```graphql
mutation CancelRetry($transactionId: String!, $reason: String!) {
  cancelRetry(transactionId: $transactionId, reason: $reason) {
    success
    exception {
      id
      transactionId
      status
    }
    cancelledRetryAttempt {
      id
      attemptNumber
      status
    }
    errors {
      message
      code
    }
    operationId
    timestamp
    performedBy
    cancellationReason
  }
}
```

**Variables**:
```json
{
  "transactionId": "TXN-2024-001",
  "reason": "Customer cancelled order, retry no longer needed"
}
```

---

## Subscriptions

### 1. Real-time Exception Updates

**Business Purpose**: Receive real-time notifications when exceptions are created, updated, or resolved.

**Subscription**:
```graphql
subscription ExceptionUpdated($filters: SubscriptionFilters) {
  exceptionUpdated(filters: $filters) {
    eventType
    exception {
      id
      transactionId
      interfaceType
      exceptionReason
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
    "interfaceTypes": ["ORDER_COLLECTION", "PAYMENT_PROCESSING"],
    "severities": ["HIGH", "CRITICAL"],
    "includeResolved": false
  }
}
```

**AngularJS WebSocket Example**:
```javascript
// WebSocket Service
angular.module('exceptionApp').service('SubscriptionService', ['$rootScope', function($rootScope) {
  var ws = new WebSocket('ws://localhost:8080/subscriptions');
  
  this.subscribeToExceptions = function(filters) {
    var subscription = {
      type: 'start',
      payload: {
        query: `subscription ExceptionUpdated($filters: SubscriptionFilters) {
          exceptionUpdated(filters: $filters) {
            eventType
            exception { id transactionId interfaceType status severity timestamp }
            timestamp
            triggeredBy
          }
        }`,
        variables: { filters: filters }
      }
    };
    
    ws.send(JSON.stringify(subscription));
  };
  
  ws.onmessage = function(event) {
    var data = JSON.parse(event.data);
    if (data.type === 'data') {
      $rootScope.$broadcast('exceptionUpdated', data.payload.data.exceptionUpdated);
    }
  };
}]);

// Controller
angular.module('exceptionApp').controller('DashboardController', 
['$scope', 'SubscriptionService', function($scope, SubscriptionService) {
  $scope.$on('exceptionUpdated', function(event, update) {
    console.log('Exception update:', update.eventType, update.exception.transactionId);
    // Update your UI accordingly
    $scope.handleExceptionUpdate(update);
    $scope.$apply();
  });
  
  // Start subscription
  SubscriptionService.subscribeToExceptions({
    severities: ['HIGH', 'CRITICAL'],
    includeResolved: false
  });
}]);
```

### 2. Retry Status Updates

**Business Purpose**: Monitor retry operation progress in real-time.

**Subscription**:
```graphql
subscription RetryStatusUpdated($transactionId: String) {
  retryStatusUpdated(transactionId: $transactionId) {
    transactionId
    retryAttempt {
      id
      attemptNumber
      status
      initiatedAt
      completedAt
      resultSuccess
      resultMessage
    }
    eventType
    timestamp
  }
}
```

### 3. Dashboard Summary Updates

**Business Purpose**: Receive real-time dashboard metrics for monitoring system health.

**Subscription**:
```graphql
subscription DashboardSummary {
  dashboardSummary {
    activeExceptions
    todayExceptions
    failedRetries
    successfulRetries
    totalRetries
    retrySuccessRate
    apiSuccessRate
    totalApiCallsToday
    lastUpdated
  }
}
```

### 4. Mutation Completion Events

**Business Purpose**: Get notified when mutation operations complete for UI feedback.

**Subscription**:
```graphql
subscription MutationCompleted($mutationType: String, $transactionId: String) {
  mutationCompleted(mutationType: $mutationType, transactionId: $transactionId) {
    mutationType
    transactionId
    success
    performedBy
    timestamp
    message
    operationId
  }
}
```

---

## Types & Enums

### Interface Types
```
ORDER_COLLECTION
ORDER_DISTRIBUTION  
CUSTOMER_SYNC
INVENTORY_UPDATE
PAYMENT_PROCESSING
NOTIFICATION_DELIVERY
```

### Exception Status
```
NEW - Newly created exception
ACKNOWLEDGED - Exception has been acknowledged by operations team
IN_PROGRESS - Exception is being actively worked on
RESOLVED - Exception has been successfully resolved
FAILED - Exception resolution failed
CANCELLED - Exception processing was cancelled
```

### Exception Severity
```
LOW - Minor issue, low business impact
MEDIUM - Moderate issue, some business impact
HIGH - Significant issue, notable business impact
CRITICAL - Severe issue, major business impact
```

### Retry Priority
```
LOW - Process when resources available
NORMAL - Standard priority processing
HIGH - Expedited processing
URGENT - Immediate processing required
```

---

## Error Handling

### Error Response Structure
```json
{
  "errors": [
    {
      "message": "Transaction ID is required",
      "code": "VALIDATION_ERROR",
      "path": ["retryException", "input", "transactionId"],
      "extensions": {
        "field": "transactionId",
        "rejectedValue": null
      }
    }
  ]
}
```

### Common Error Codes
- `VALIDATION_ERROR` - Input validation failed
- `AUTHORIZATION_ERROR` - Insufficient permissions
- `NOT_FOUND` - Requested resource not found
- `BUSINESS_RULE_ERROR` - Business logic validation failed
- `RATE_LIMIT_EXCEEDED` - Too many requests
- `INTERNAL_ERROR` - System error occurred

### AngularJS Error Handling
```javascript
angular.module('exceptionApp').service('GraphQLService', ['$http', function($http) {
  this.executeQuery = function(query, variables) {
    return $http.post('/graphql', {
      query: query,
      variables: variables
    }).then(function(response) {
      if (response.data.errors) {
        throw new Error('GraphQL Error: ' + response.data.errors[0].message);
      }
      return response.data.data;
    });
  };
}]);
```

---

## AngularJS Integration Examples

### Complete Service Implementation
```javascript
angular.module('exceptionApp').service('ExceptionGraphQLService', ['$http', '$q', function($http, $q) {
  var baseUrl = '/graphql';
  
  var executeQuery = function(query, variables) {
    return $http.post(baseUrl, {
      query: query,
      variables: variables || {}
    }).then(function(response) {
      if (response.data.errors) {
        return $q.reject(response.data.errors);
      }
      return response.data.data;
    });
  };
  
  // Exception queries
  this.getExceptions = function(filters, pagination, sorting) {
    var query = `
      query GetExceptions($filters: ExceptionFilters, $pagination: PaginationInput, $sorting: SortingInput) {
        exceptions(filters: $filters, pagination: $pagination, sorting: $sorting) {
          edges {
            node {
              id transactionId interfaceType exceptionReason status severity 
              timestamp customerId retryCount retryable
            }
            cursor
          }
          pageInfo { hasNextPage hasPreviousPage startCursor endCursor }
          totalCount
        }
      }
    `;
    return executeQuery(query, { filters: filters, pagination: pagination, sorting: sorting });
  };
  
  this.getException = function(transactionId) {
    var query = `
      query GetException($transactionId: String!) {
        exception(transactionId: $transactionId) {
          id transactionId interfaceType exceptionReason status severity
          timestamp processedAt retryCount acknowledgedBy acknowledgedAt
          originalPayload { content contentType retrievedAt }
          retryHistory { attemptNumber status initiatedBy initiatedAt completedAt resultSuccess }
        }
      }
    `;
    return executeQuery(query, { transactionId: transactionId });
  };
  
  // Exception mutations
  this.retryException = function(input) {
    var mutation = `
      mutation RetryException($input: RetryExceptionInput!) {
        retryException(input: $input) {
          success
          exception { id transactionId status retryCount }
          retryAttempt { id attemptNumber status }
          errors { message code }
        }
      }
    `;
    return executeQuery(mutation, { input: input });
  };
  
  this.acknowledgeException = function(input) {
    var mutation = `
      mutation AcknowledgeException($input: AcknowledgeExceptionInput!) {
        acknowledgeException(input: $input) {
          success
          exception { id transactionId status acknowledgedBy acknowledgedAt }
          errors { message code }
        }
      }
    `;
    return executeQuery(mutation, { input: input });
  };
  
  // Summary queries
  this.getExceptionSummary = function(timeRange, filters) {
    var query = `
      query GetExceptionSummary($timeRange: TimeRange!, $filters: ExceptionFilters) {
        exceptionSummary(timeRange: $timeRange, filters: $filters) {
          totalExceptions
          byInterfaceType { interfaceType count percentage }
          bySeverity { severity count percentage }
          keyMetrics { retrySuccessRate averageResolutionTime customerImpactCount }
        }
      }
    `;
    return executeQuery(query, { timeRange: timeRange, filters: filters });
  };
}]);
```

### Dashboard Controller Example
```javascript
angular.module('exceptionApp').controller('ExceptionDashboardController', 
['$scope', '$interval', 'ExceptionGraphQLService', 'SubscriptionService', 
function($scope, $interval, ExceptionGraphQLService, SubscriptionService) {
  
  $scope.dashboard = {
    exceptions: [],
    summary: {},
    loading: false,
    filters: {
      severities: ['HIGH', 'CRITICAL'],
      excludeResolved: true
    }
  };
  
  // Load initial data
  $scope.loadDashboard = function() {
    $scope.dashboard.loading = true;
    
    // Load exceptions
    ExceptionGraphQLService.getExceptions($scope.dashboard.filters, { first: 10 }, { field: 'timestamp', direction: 'DESC' })
      .then(function(data) {
        $scope.dashboard.exceptions = data.exceptions.edges.map(function(edge) { return edge.node; });
      });
    
    // Load summary
    ExceptionGraphQLService.getExceptionSummary({ period: 'LAST_24_HOURS' }, $scope.dashboard.filters)
      .then(function(data) {
        $scope.dashboard.summary = data.exceptionSummary;
      })
      .finally(function() {
        $scope.dashboard.loading = false;
      });
  };
  
  // Handle real-time updates
  $scope.$on('exceptionUpdated', function(event, update) {
    if (update.eventType === 'CREATED' && update.exception.severity === 'CRITICAL') {
      $scope.dashboard.exceptions.unshift(update.exception);
      $scope.showNotification('New critical exception: ' + update.exception.transactionId);
    }
  });
  
  // Retry exception
  $scope.retryException = function(exception) {
    var input = {
      transactionId: exception.transactionId,
      reason: 'Manual retry from dashboard',
      priority: 'NORMAL'
    };
    
    ExceptionGraphQLService.retryException(input)
      .then(function(result) {
        if (result.retryException.success) {
          $scope.showNotification('Retry initiated for ' + exception.transactionId);
          $scope.loadDashboard(); // Refresh data
        } else {
          $scope.showError('Retry failed: ' + result.retryException.errors[0].message);
        }
      });
  };
  
  // Initialize
  $scope.loadDashboard();
  
  // Setup real-time subscriptions
  SubscriptionService.subscribeToExceptions($scope.dashboard.filters);
  
  // Auto-refresh every 30 seconds
  var refreshInterval = $interval($scope.loadDashboard, 30000);
  
  $scope.$on('$destroy', function() {
    $interval.cancel(refreshInterval);
  });
}]);
```

This comprehensive guide provides AngularJS developers with all the necessary information to integrate with the Interface Exception Collector GraphQL API, including practical examples and best practices for real-world implementation.