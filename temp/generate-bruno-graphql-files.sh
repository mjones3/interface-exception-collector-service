#!/bin/bash

# Generate Bruno GraphQL Files
# This script creates .bru files for all GraphQL queries, mutations, and subscriptions

set -e

BRUNO_DIR="bruno/interface-exception-collection-service/local-graphql"
GRAPHQL_ENDPOINT="http://localhost:8080/graphql"

echo "🚀 Generating Bruno GraphQL files..."
echo "Target directory: $BRUNO_DIR"

# Create directory if it doesn't exist
mkdir -p "$BRUNO_DIR"

# Function to create a .bru file
create_bru_file() {
    local filename="$1"
    local name="$2"
    local query="$3"
    local variables="$4"
    local description="$5"
    
    cat > "$BRUNO_DIR/$filename.bru" << EOF
meta {
  name: $name
  type: graphql
  seq: 1
}

post {
  url: $GRAPHQL_ENDPOINT
  body: graphql
  auth: bearer
}

auth:bearer {
  token: {{api-token}}
}

body:graphql {
  $query
}

body:graphql:vars {
  $variables
}

docs {
  $description
}
EOF
    echo "✅ Created $filename.bru"
}

echo ""
echo "📋 Creating Query files..."

# Query: Get all exceptions
create_bru_file "query-exceptions" "Get Exceptions" \
'query GetExceptions($filters: ExceptionFilters, $pagination: PaginationInput, $sorting: SortingInput) {
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
}' \
'{
  "filters": {
    "interfaceTypes": ["ORDER_COLLECTION"],
    "statuses": ["NEW", "ACKNOWLEDGED"],
    "severities": ["HIGH", "CRITICAL"],
    "excludeResolved": true,
    "retryable": true
  },
  "pagination": {
    "first": 10
  },
  "sorting": {
    "field": "timestamp",
    "direction": "DESC"
  }
}' \
"Retrieves a paginated list of exceptions with optional filtering and sorting."

# Query: Get single exception
create_bru_file "query-exception-by-id" "Get Exception by Transaction ID" \
'query GetException($transactionId: String!) {
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
      resultErrorDetails
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
}' \
'{
  "transactionId": "test-transaction-123"
}' \
"Retrieves detailed information for a specific exception including retry history and status changes."

# Query: Exception summary
create_bru_file "query-exception-summary" "Get Exception Summary" \
'query GetExceptionSummary($timeRange: TimeRange!, $filters: ExceptionFilters) {
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
}' \
'{
  "timeRange": {
    "period": "LAST_7_DAYS"
  },
  "filters": {
    "severities": ["HIGH", "CRITICAL"]
  }
}' \
"Retrieves aggregated exception statistics and trends for a specified time range."

# Query: Search exceptions
create_bru_file "query-search-exceptions" "Search Exceptions" \
'query SearchExceptions($search: SearchInput!, $pagination: PaginationInput, $sorting: SortingInput) {
  searchExceptions(search: $search, pagination: $pagination, sorting: $sorting) {
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
        retryCount
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
}' \
'{
  "search": {
    "query": "timeout",
    "fields": ["EXCEPTION_REASON", "OPERATION"],
    "fuzzy": true
  },
  "pagination": {
    "first": 20
  },
  "sorting": {
    "field": "timestamp",
    "direction": "DESC"
  }
}' \
"Performs full-text search across exception fields with fuzzy matching support."

# Query: System health
create_bru_file "query-system-health" "Get System Health" \
'query GetSystemHealth {
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
}' \
'{}' \
"Retrieves current system health status including database, cache, and external service connectivity."

echo ""
echo "🔧 Creating Mutation files..."

# Mutation: Retry exception
create_bru_file "mutation-retry-exception" "Retry Exception" \
'mutation RetryException($input: RetryExceptionInput!) {
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
      extensions
    }
  }
}' \
'{
  "input": {
    "transactionId": "test-transaction-123",
    "reason": "Customer requested retry after fixing data issue",
    "priority": "HIGH",
    "notes": "Retrying after customer confirmed data correction"
  }
}' \
"Initiates a retry operation for a specific exception."

# Mutation: Bulk retry exceptions
create_bru_file "mutation-bulk-retry-exceptions" "Bulk Retry Exceptions" \
'mutation BulkRetryExceptions($input: BulkRetryInput!) {
  bulkRetryExceptions(input: $input) {
    successCount
    failureCount
    results {
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
    errors {
      message
      code
      path
    }
  }
}' \
'{
  "input": {
    "transactionIds": ["test-transaction-123", "test-transaction-456", "test-transaction-789"],
    "reason": "Bulk retry after system maintenance",
    "priority": "NORMAL"
  }
}' \
"Initiates retry operations for multiple exceptions simultaneously."

# Mutation: Acknowledge exception
create_bru_file "mutation-acknowledge-exception" "Acknowledge Exception" \
'mutation AcknowledgeException($input: AcknowledgeExceptionInput!) {
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
      path
      extensions
    }
  }
}' \
'{
  "input": {
    "transactionId": "test-transaction-123",
    "reason": "Reviewed and assigned to development team",
    "notes": "Issue appears to be related to timeout configuration",
    "estimatedResolutionTime": "2025-08-21T10:00:00Z",
    "assignedTo": "dev-team@company.com"
  }
}' \
"Acknowledges an exception and assigns it for resolution."

# Mutation: Bulk acknowledge exceptions
create_bru_file "mutation-bulk-acknowledge-exceptions" "Bulk Acknowledge Exceptions" \
'mutation BulkAcknowledgeExceptions($input: BulkAcknowledgeInput!) {
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
    errors {
      message
      code
      path
    }
  }
}' \
'{
  "input": {
    "transactionIds": ["test-transaction-123", "test-transaction-456"],
    "reason": "Bulk acknowledgment for similar timeout issues",
    "notes": "All related to external service timeout - investigating",
    "assignedTo": "ops-team@company.com"
  }
}' \
"Acknowledges multiple exceptions simultaneously."

# Mutation: Resolve exception
create_bru_file "mutation-resolve-exception" "Resolve Exception" \
'mutation ResolveException($transactionId: String!, $resolutionMethod: ResolutionMethod!, $notes: String) {
  resolveException(transactionId: $transactionId, resolutionMethod: $resolutionMethod, notes: $notes) {
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
      path
      extensions
    }
  }
}' \
'{
  "transactionId": "test-transaction-123",
  "resolutionMethod": "MANUAL_INTERVENTION",
  "notes": "Fixed timeout configuration and verified with customer"
}' \
"Marks an exception as resolved with specified resolution method."

# Mutation: Cancel retry
create_bru_file "mutation-cancel-retry" "Cancel Retry" \
'mutation CancelRetry($transactionId: String!, $reason: String!) {
  cancelRetry(transactionId: $transactionId, reason: $reason) {
    success
    exception {
      id
      transactionId
      status
      retryCount
    }
    errors {
      message
      code
      path
      extensions
    }
  }
}' \
'{
  "transactionId": "test-transaction-123",
  "reason": "Customer requested cancellation - issue resolved externally"
}' \
"Cancels an ongoing retry operation for an exception."

echo ""
echo "📡 Creating Subscription files..."

# Subscription: Exception updates
create_bru_file "subscription-exception-updated" "Exception Updates" \
'subscription ExceptionUpdated($filters: SubscriptionFilters) {
  exceptionUpdated(filters: $filters) {
    eventType
    exception {
      id
      transactionId
      externalId
      interfaceType
      exceptionReason
      status
      severity
      category
      customerId
      locationCode
      timestamp
      retryCount
    }
    timestamp
    triggeredBy
  }
}' \
'{
  "filters": {
    "interfaceTypes": ["ORDER_COLLECTION", "ORDER_DISTRIBUTION"],
    "severities": ["HIGH", "CRITICAL"],
    "customerIds": ["CUST-001", "CUST-002"],
    "includeResolved": false
  }
}' \
"Real-time updates for exception events with optional filtering."

# Subscription: Summary updates
create_bru_file "subscription-summary-updated" "Summary Updates" \
'subscription SummaryUpdated($timeRange: TimeRange!) {
  summaryUpdated(timeRange: $timeRange) {
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
    keyMetrics {
      retrySuccessRate
      averageResolutionTime
      customerImpactCount
      criticalExceptionCount
    }
  }
}' \
'{
  "timeRange": {
    "period": "LAST_24_HOURS"
  }
}' \
"Real-time updates for exception summary statistics."

# Subscription: Retry status updates
create_bru_file "subscription-retry-status-updated" "Retry Status Updates" \
'subscription RetryStatusUpdated($transactionId: String) {
  retryStatusUpdated(transactionId: $transactionId) {
    transactionId
    retryAttempt {
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
    eventType
    timestamp
  }
}' \
'{
  "transactionId": "test-transaction-123"
}' \
"Real-time updates for retry operation status changes."

echo ""
echo "🔍 Creating Advanced Query files..."

# Advanced Query: Exceptions with complex filters
create_bru_file "query-exceptions-advanced-filters" "Exceptions with Advanced Filters" \
'query GetExceptionsAdvanced($filters: ExceptionFilters!, $pagination: PaginationInput!, $sorting: SortingInput!) {
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
        retryHistory {
          id
          attemptNumber
          status
          initiatedBy
          initiatedAt
          resultSuccess
        }
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
}' \
'{
  "filters": {
    "interfaceTypes": ["ORDER_COLLECTION", "PAYMENT_PROCESSING"],
    "statuses": ["NEW", "ACKNOWLEDGED", "IN_PROGRESS"],
    "severities": ["HIGH", "CRITICAL"],
    "categories": ["TIMEOUT_ERROR", "EXTERNAL_SERVICE_ERROR"],
    "dateRange": {
      "from": "2025-08-15T00:00:00Z",
      "to": "2025-08-20T23:59:59Z"
    },
    "customerIds": ["CUST-MOUNT-SINAI-001", "CUST-MAYO-CLINIC-002"],
    "locationCodes": ["HOSP-NYC-001", "HOSP-MN-001"],
    "searchTerm": "timeout",
    "excludeResolved": true,
    "retryable": true,
    "hasRetries": true
  },
  "pagination": {
    "first": 25,
    "after": null
  },
  "sorting": {
    "field": "severity",
    "direction": "DESC"
  }
}' \
"Advanced exception query with comprehensive filtering options."

# Query: Custom time range summary
create_bru_file "query-summary-custom-range" "Summary with Custom Time Range" \
'query GetSummaryCustomRange($timeRange: TimeRange!, $filters: ExceptionFilters) {
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
}' \
'{
  "timeRange": {
    "period": "CUSTOM",
    "customRange": {
      "from": "2025-08-01T00:00:00Z",
      "to": "2025-08-20T23:59:59Z"
    }
  },
  "filters": {
    "interfaceTypes": ["ORDER_COLLECTION"],
    "customerIds": ["CUST-MOUNT-SINAI-001"]
  }
}' \
"Exception summary with custom date range and specific customer filtering."

echo ""
echo "📝 Creating Test Data Setup files..."

# Create a test data setup query (this would be used to verify test data exists)
create_bru_file "query-test-data-check" "Check Test Data" \
'query CheckTestData {
  exceptions(
    filters: { 
      searchTerm: "test-transaction",
      excludeResolved: false 
    },
    pagination: { first: 5 }
  ) {
    edges {
      node {
        transactionId
        exceptionReason
        status
        severity
        interfaceType
      }
    }
    totalCount
  }
}' \
'{}' \
"Checks if test data exists in the system for testing GraphQL operations."

echo ""
echo "✅ Bruno GraphQL files generation complete!"
echo ""
echo "📁 Generated files in: $BRUNO_DIR"
echo ""
echo "🔧 Setup Instructions:"
echo "1. Copy the generated .bru files to your Bruno collection directory"
echo "2. Set the {{api-token}} variable in your Bruno environment:"
echo "   - Generate a token: node generate-jwt-correct-secret.js \"test-user\" \"ADMIN\""
echo "   - Add to Bruno environment: api-token = <your-generated-token>"
echo "3. Ensure your GraphQL endpoint is running at: $GRAPHQL_ENDPOINT"
echo ""
echo "📋 Generated Operations:"
echo "   Queries: 7 files"
echo "   Mutations: 6 files" 
echo "   Subscriptions: 3 files"
echo "   Total: 16 GraphQL operations"
echo ""
echo "🚀 You can now test all GraphQL operations in Bruno!"

# List all generated files
echo ""
echo "📄 Generated files:"
ls -la "$BRUNO_DIR"/*.bru | awk '{print "   " $9}' | sed 's|.*/||'