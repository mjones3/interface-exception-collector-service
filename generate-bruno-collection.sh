#!/bin/bash

# Generate Complete Bruno Collection for GraphQL API
# This script creates a complete Bruno collection that can be imported

set -e

COLLECTION_NAME="Interface Exception Collector GraphQL API"
COLLECTION_DIR="bruno-graphql-collection"
GRAPHQL_ENDPOINT="http://localhost:8080/graphql"

echo "🚀 Generating Bruno Collection: $COLLECTION_NAME"
echo "Target directory: $COLLECTION_DIR"

# Clean and create collection directory
rm -rf "$COLLECTION_DIR"
mkdir -p "$COLLECTION_DIR"

# Create collection root file (bruno.json)
cat > "$COLLECTION_DIR/bruno.json" << 'EOF'
{
  "version": "1",
  "name": "Interface Exception Collector GraphQL API",
  "type": "collection",
  "ignore": [
    "node_modules",
    ".git"
  ]
}
EOF

# Create environments directory and files
mkdir -p "$COLLECTION_DIR/environments"

# Local environment
cat > "$COLLECTION_DIR/environments/Local.bru" << 'EOF'
vars {
  graphql-endpoint: http://localhost:8080/graphql
  api-token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3MjQxNjAwMDAsImV4cCI6MTc1NTY5NjAwMH0.example-token-replace-with-real-token
}
EOF

# Development environment
cat > "$COLLECTION_DIR/environments/Development.bru" << 'EOF'
vars {
  graphql-endpoint: https://dev-api.company.com/graphql
  api-token: {{dev-api-token}}
}
EOF

# Staging environment
cat > "$COLLECTION_DIR/environments/Staging.bru" << 'EOF'
vars {
  graphql-endpoint: https://staging-api.company.com/graphql
  api-token: {{staging-api-token}}
}
EOF

# Create folder structure
mkdir -p "$COLLECTION_DIR/Queries"
mkdir -p "$COLLECTION_DIR/Mutations"
mkdir -p "$COLLECTION_DIR/Subscriptions"
mkdir -p "$COLLECTION_DIR/Advanced Queries"

# Function to create a .bru file
create_bru_file() {
    local filepath="$1"
    local name="$2"
    local query="$3"
    local variables="$4"
    local description="$5"
    local seq="${6:-1}"
    
    cat > "$filepath" << EOF
meta {
  name: $name
  type: graphql
  seq: $seq
}

post {
  url: {{graphql-endpoint}}
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
    echo "✅ Created $(basename "$filepath")"
}

echo ""
echo "📋 Creating Query files..."

# Query: Get all exceptions
create_bru_file "$COLLECTION_DIR/Queries/Get Exceptions.bru" "Get Exceptions" \
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
"Retrieves a paginated list of exceptions with optional filtering and sorting." 1

# Query: Get single exception
create_bru_file "$COLLECTION_DIR/Queries/Get Exception by ID.bru" "Get Exception by ID" \
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
"Retrieves detailed information for a specific exception including retry history and status changes." 2

# Query: Exception summary
create_bru_file "$COLLECTION_DIR/Queries/Get Exception Summary.bru" "Get Exception Summary" \
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
"Retrieves aggregated exception statistics and trends for a specified time range." 3

# Query: Search exceptions
create_bru_file "$COLLECTION_DIR/Queries/Search Exceptions.bru" "Search Exceptions" \
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
"Performs full-text search across exception fields with fuzzy matching support." 4

# Query: System health
create_bru_file "$COLLECTION_DIR/Queries/Get System Health.bru" "Get System Health" \
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
"Retrieves current system health status including database, cache, and external service connectivity." 5

echo ""
echo "🔧 Creating Mutation files..."

# Mutation: Retry exception
create_bru_file "$COLLECTION_DIR/Mutations/Retry Exception.bru" "Retry Exception" \
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
"Initiates a retry operation for a specific exception." 1

# Mutation: Bulk retry exceptions
create_bru_file "$COLLECTION_DIR/Mutations/Bulk Retry Exceptions.bru" "Bulk Retry Exceptions" \
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
"Initiates retry operations for multiple exceptions simultaneously." 2

# Mutation: Acknowledge exception
create_bru_file "$COLLECTION_DIR/Mutations/Acknowledge Exception.bru" "Acknowledge Exception" \
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
"Acknowledges an exception and assigns it for resolution." 3

# Mutation: Bulk acknowledge exceptions
create_bru_file "$COLLECTION_DIR/Mutations/Bulk Acknowledge Exceptions.bru" "Bulk Acknowledge Exceptions" \
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
"Acknowledges multiple exceptions simultaneously." 4

# Mutation: Resolve exception
create_bru_file "$COLLECTION_DIR/Mutations/Resolve Exception.bru" "Resolve Exception" \
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
"Marks an exception as resolved with specified resolution method." 5

# Mutation: Cancel retry
create_bru_file "$COLLECTION_DIR/Mutations/Cancel Retry.bru" "Cancel Retry" \
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
"Cancels an ongoing retry operation for an exception." 6

echo ""
echo "📡 Creating Subscription files..."

# Subscription: Exception updates
create_bru_file "$COLLECTION_DIR/Subscriptions/Exception Updates.bru" "Exception Updates" \
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
"Real-time updates for exception events with optional filtering." 1

# Subscription: Summary updates
create_bru_file "$COLLECTION_DIR/Subscriptions/Summary Updates.bru" "Summary Updates" \
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
"Real-time updates for exception summary statistics." 2

# Subscription: Retry status updates
create_bru_file "$COLLECTION_DIR/Subscriptions/Retry Status Updates.bru" "Retry Status Updates" \
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
"Real-time updates for retry operation status changes." 3

echo ""
echo "🔍 Creating Advanced Query files..."

# Advanced Query: Exceptions with complex filters
create_bru_file "$COLLECTION_DIR/Advanced Queries/Complex Filtering.bru" "Complex Filtering" \
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
"Advanced exception query with comprehensive filtering options." 1

# Query: Custom time range summary
create_bru_file "$COLLECTION_DIR/Advanced Queries/Custom Time Range Summary.bru" "Custom Time Range Summary" \
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
"Exception summary with custom date range and specific customer filtering." 2

# Test data check query
create_bru_file "$COLLECTION_DIR/Advanced Queries/Test Data Check.bru" "Test Data Check" \
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
"Checks if test data exists in the system for testing GraphQL operations." 3

# Create README file for the collection
cat > "$COLLECTION_DIR/README.md" << 'EOF'
# Interface Exception Collector GraphQL API - Bruno Collection

This Bruno collection provides comprehensive testing capabilities for the Interface Exception Collector GraphQL API.

## Setup Instructions

1. **Import Collection**: Import this collection into Bruno
2. **Configure Environment**: 
   - Select the "Local" environment for local development
   - Update the `api-token` variable with a valid JWT token
3. **Generate JWT Token**:
   ```bash
   node generate-jwt-correct-secret.js "test-user" "ADMIN"
   ```
4. **Update Token**: Copy the generated token to the `api-token` environment variable

## Collection Structure

### Queries (5 operations)
- **Get Exceptions**: Paginated list with filtering
- **Get Exception by ID**: Detailed exception information
- **Get Exception Summary**: Aggregated statistics
- **Search Exceptions**: Full-text search capabilities
- **Get System Health**: System status monitoring

### Mutations (6 operations)
- **Retry Exception**: Single exception retry
- **Bulk Retry Exceptions**: Multiple exception retry
- **Acknowledge Exception**: Exception acknowledgment
- **Bulk Acknowledge Exceptions**: Multiple exception acknowledgment
- **Resolve Exception**: Mark exception as resolved
- **Cancel Retry**: Cancel ongoing retry operation

### Subscriptions (3 operations)
- **Exception Updates**: Real-time exception events
- **Summary Updates**: Real-time statistics updates
- **Retry Status Updates**: Real-time retry status changes

### Advanced Queries (3 operations)
- **Complex Filtering**: Advanced filtering examples
- **Custom Time Range Summary**: Custom date range statistics
- **Test Data Check**: Verify test data availability

## Environment Variables

- `graphql-endpoint`: GraphQL API endpoint URL
- `api-token`: JWT authentication token

## Test Data

The collection includes realistic test data in variables:
- Transaction IDs: `test-transaction-123`, `test-transaction-456`, etc.
- Customer IDs: `CUST-MOUNT-SINAI-001`, `CUST-MAYO-CLINIC-002`
- Location Codes: `HOSP-NYC-001`, `HOSP-MN-001`

## Usage Tips

1. **Start with Queries**: Test basic data retrieval first
2. **Use Test Data Check**: Verify test data exists before running other operations
3. **Authentication**: Ensure your JWT token is valid and has appropriate permissions
4. **Subscriptions**: Require WebSocket support - ensure your GraphQL endpoint supports subscriptions
5. **Error Handling**: Check the `errors` field in mutation responses for detailed error information

## Troubleshooting

- **401 Unauthorized**: Check your JWT token validity
- **403 Forbidden**: Ensure your token has the required role (ADMIN recommended for testing)
- **Connection Issues**: Verify the GraphQL endpoint is running and accessible
- **Subscription Failures**: Ensure WebSocket support is enabled on the server

## Support

For issues or questions, refer to the main project documentation or contact the development team.
EOF

echo ""
echo "✅ Bruno Collection generation complete!"
echo ""
echo "📁 Collection created in: $COLLECTION_DIR"
echo ""
echo "📋 Collection Contents:"
echo "   - bruno.json (collection configuration)"
echo "   - environments/ (3 environment files)"
echo "   - Queries/ (5 query operations)"
echo "   - Mutations/ (6 mutation operations)"
echo "   - Subscriptions/ (3 subscription operations)"
echo "   - Advanced Queries/ (3 advanced operations)"
echo "   - README.md (setup and usage instructions)"
echo ""
echo "🔧 Next Steps:"
echo "1. Import the collection into Bruno:"
echo "   - Open Bruno"
echo "   - File → Import Collection"
echo "   - Select the '$COLLECTION_DIR' folder"
echo ""
echo "2. Configure authentication:"
echo "   - Generate JWT token: node generate-jwt-correct-secret.js \"test-user\" \"ADMIN\""
echo "   - Update the 'api-token' variable in the Local environment"
echo ""
echo "3. Start testing your GraphQL API!"
echo ""
echo "📦 To create a zip file for easy sharing:"
echo "   zip -r bruno-graphql-collection.zip $COLLECTION_DIR"

# Create a zip file for easy distribution
if command -v zip &> /dev/null; then
    echo ""
    echo "📦 Creating zip file for distribution..."
    zip -r "bruno-graphql-collection.zip" "$COLLECTION_DIR" > /dev/null 2>&1
    echo "✅ Created: bruno-graphql-collection.zip"
fi

echo ""
echo "🎉 Collection ready for import into Bruno!"