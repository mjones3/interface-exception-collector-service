# Interface Exception Collector Service - API Documentation

## Overview

The Interface Exception Collector Service provides RESTful APIs for managing and retrieving interface exceptions from BioPro systems. The API follows REST principles and uses JSON for data exchange.

## Base URL

- **Local Development**: `http://localhost:8080`
- **Development**: `https://api-dev.biopro.com`
- **Staging**: `https://api-staging.biopro.com`
- **Production**: `https://api.biopro.com`

## Authentication

The API uses JWT Bearer token authentication. Include the token in the Authorization header:

```http
Authorization: Bearer <your-jwt-token>
```

## Rate Limiting

- **Authenticated users**: 1000 requests per minute
- **Unauthenticated requests**: 100 requests per minute

## Interactive Documentation

Swagger UI is available at: `{base-url}/swagger-ui.html`

OpenAPI specification: `{base-url}/v3/api-docs`

## API Endpoints

### Exception Management

#### List Exceptions

Retrieves a paginated list of exceptions with optional filtering.

```http
GET /api/v1/exceptions
```

**Query Parameters:**

| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| `interfaceType` | string | No | Filter by interface type | `ORDER`, `COLLECTION`, `DISTRIBUTION` |
| `status` | string | No | Filter by exception status | `NEW`, `ACKNOWLEDGED`, `RESOLVED` |
| `severity` | string | No | Filter by severity level | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `customerId` | string | No | Filter by customer ID | `CUST001` |
| `fromDate` | string | No | Start date (ISO 8601) | `2025-08-01T00:00:00Z` |
| `toDate` | string | No | End date (ISO 8601) | `2025-08-05T23:59:59Z` |
| `page` | integer | No | Page number (0-based) | `0` |
| `size` | integer | No | Page size (max 100) | `20` |
| `sort` | string | No | Sort criteria | `timestamp,desc` |

**Example Request:**

```bash
curl -X GET "http://localhost:8080/api/v1/exceptions?interfaceType=ORDER&status=NEW&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

**Example Response:**

```json
{
  "content": [
    {
      "id": 12345,
      "transactionId": "uuid-123",
      "interfaceType": "ORDER",
      "exceptionReason": "Order already exists",
      "operation": "CREATE_ORDER",
      "externalId": "ORDER-ABC123",
      "status": "NEW",
      "severity": "MEDIUM",
      "category": "BUSINESS_RULE",
      "customerId": "CUST001",
      "timestamp": "2025-08-05T10:30:00Z",
      "retryCount": 0
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "orders": [
        {
          "property": "timestamp",
          "direction": "DESC"
        }
      ]
    }
  },
  "totalElements": 150,
  "totalPages": 15,
  "numberOfElements": 10,
  "first": true,
  "last": false
}
```

#### Get Exception Details

Retrieves detailed information for a specific exception.

```http
GET /api/v1/exceptions/{transactionId}
```

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `transactionId` | string | Yes | Unique transaction identifier |

**Query Parameters:**

| Parameter | Type | Required | Description | Default |
|-----------|------|----------|-------------|---------|
| `includePayload` | boolean | No | Include original payload | `false` |

**Example Request:**

```bash
curl -X GET "http://localhost:8080/api/v1/exceptions/uuid-123?includePayload=true" \
  -H "Authorization: Bearer <token>"
```

**Example Response:**

```json
{
  "id": 12345,
  "transactionId": "uuid-123",
  "interfaceType": "ORDER",
  "exceptionReason": "Order already exists",
  "operation": "CREATE_ORDER",
  "externalId": "ORDER-ABC123",
  "status": "NEW",
  "severity": "MEDIUM",
  "category": "BUSINESS_RULE",
  "retryable": true,
  "customerId": "CUST001",
  "locationCode": "LOC001",
  "timestamp": "2025-08-05T10:30:00Z",
  "processedAt": "2025-08-05T10:30:05Z",
  "retryCount": 0,
  "originalPayload": {
    "orderId": "ORDER-ABC123",
    "customerId": "CUST001",
    "items": [...]
  },
  "retryHistory": [],
  "relatedExceptions": [
    {
      "id": 12344,
      "transactionId": "uuid-122",
      "exceptionReason": "Invalid customer data",
      "timestamp": "2025-08-05T09:15:00Z"
    }
  ]
}
```

#### Search Exceptions

Performs full-text search across exception fields.

```http
GET /api/v1/exceptions/search
```

**Query Parameters:**

| Parameter | Type | Required | Description | Default |
|-----------|------|----------|-------------|---------|
| `query` | string | Yes | Search query string | - |
| `fields` | array | No | Fields to search | `["exceptionReason"]` |
| `page` | integer | No | Page number (0-based) | `0` |
| `size` | integer | No | Page size (max 100) | `20` |
| `sort` | string | No | Sort criteria | `timestamp,desc` |

**Valid Search Fields:**
- `exceptionReason`
- `externalId`
- `operation`

**Example Request:**

```bash
curl -X GET "http://localhost:8080/api/v1/exceptions/search?query=order%20exists&fields=exceptionReason,externalId" \
  -H "Authorization: Bearer <token>"
```

#### Get Exception Summary

Retrieves aggregated exception statistics.

```http
GET /api/v1/exceptions/summary
```

**Query Parameters:**

| Parameter | Type | Required | Description | Default |
|-----------|------|----------|-------------|---------|
| `timeRange` | string | No | Time range for statistics | `week` |
| `groupBy` | string | No | Group statistics by field | - |

**Valid Time Ranges:**
- `today`
- `week`
- `month`
- `quarter`

**Valid Group By Fields:**
- `interfaceType`
- `severity`
- `status`

**Example Request:**

```bash
curl -X GET "http://localhost:8080/api/v1/exceptions/summary?timeRange=week&groupBy=severity" \
  -H "Authorization: Bearer <token>"
```

**Example Response:**

```json
{
  "totalExceptions": 1250,
  "timeRange": "week",
  "byInterfaceType": {
    "ORDER": 650,
    "COLLECTION": 350,
    "DISTRIBUTION": 250
  },
  "bySeverity": {
    "LOW": 500,
    "MEDIUM": 450,
    "HIGH": 250,
    "CRITICAL": 50
  },
  "byStatus": {
    "NEW": 300,
    "ACKNOWLEDGED": 200,
    "RESOLVED": 700,
    "CLOSED": 50
  },
  "trends": {
    "daily": [
      {
        "date": "2025-08-01",
        "count": 180
      },
      {
        "date": "2025-08-02",
        "count": 165
      }
    ]
  }
}
```

### Retry Management

#### Initiate Retry

Initiates a retry operation for a failed exception.

```http
POST /api/v1/exceptions/{transactionId}/retry
```

**Path Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `transactionId` | string | Yes | Unique transaction identifier |

**Request Body:**

```json
{
  "reason": "Transient network error resolved",
  "priority": "HIGH",
  "notifyOnCompletion": true
}
```

**Example Request:**

```bash
curl -X POST "http://localhost:8080/api/v1/exceptions/uuid-123/retry" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "Transient network error resolved",
    "priority": "HIGH",
    "notifyOnCompletion": true
  }'
```

**Example Response:**

```json
{
  "retryId": "retry-uuid-456",
  "status": "PENDING",
  "message": "Retry operation initiated successfully",
  "estimatedCompletionTime": "2025-08-05T10:35:00Z"
}
```

#### Get Retry History

Retrieves the retry history for an exception.

```http
GET /api/v1/exceptions/{transactionId}/retry-history
```

**Example Response:**

```json
{
  "transactionId": "uuid-123",
  "retryAttempts": [
    {
      "attemptNumber": 1,
      "status": "SUCCESS",
      "initiatedBy": "user@biopro.com",
      "initiatedAt": "2025-08-05T10:30:00Z",
      "completedAt": "2025-08-05T10:30:15Z",
      "result": {
        "success": true,
        "message": "Retry completed successfully",
        "responseCode": 200
      }
    }
  ]
}
```

### Management Operations

#### Acknowledge Exception

Marks an exception as acknowledged by the operations team.

```http
PUT /api/v1/exceptions/{transactionId}/acknowledge
```

**Request Body:**

```json
{
  "acknowledgedBy": "user@biopro.com",
  "notes": "Investigating root cause"
}
```

**Example Response:**

```json
{
  "status": "ACKNOWLEDGED",
  "acknowledgedAt": "2025-08-05T10:30:00Z",
  "acknowledgedBy": "user@biopro.com"
}
```

#### Resolve Exception

Marks an exception as resolved.

```http
PUT /api/v1/exceptions/{transactionId}/resolve
```

**Request Body:**

```json
{
  "resolvedBy": "user@biopro.com",
  "resolutionMethod": "MANUAL_RESOLUTION",
  "notes": "Issue resolved by updating customer data"
}
```

## Health and Monitoring

### Health Check

```http
GET /actuator/health
```

### Application Metrics

```http
GET /actuator/metrics
```

### Prometheus Metrics

```http
GET /actuator/prometheus
```

## Error Responses

All error responses follow a consistent format:

```json
{
  "timestamp": "2025-08-05T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid request parameters",
  "path": "/api/v1/exceptions"
}
```

### Common HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 202 | Accepted - Request accepted for processing |
| 400 | Bad Request - Invalid request parameters |
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource conflict |
| 429 | Too Many Requests - Rate limit exceeded |
| 500 | Internal Server Error - Server error |

## Data Models

### InterfaceException

```json
{
  "id": "integer",
  "transactionId": "string",
  "interfaceType": "enum [ORDER, COLLECTION, DISTRIBUTION]",
  "exceptionReason": "string",
  "operation": "string",
  "externalId": "string",
  "status": "enum [NEW, ACKNOWLEDGED, RETRIED_SUCCESS, RETRIED_FAILED, ESCALATED, RESOLVED, CLOSED]",
  "severity": "enum [LOW, MEDIUM, HIGH, CRITICAL]",
  "category": "enum [BUSINESS_RULE, VALIDATION, SYSTEM_ERROR, TIMEOUT, NETWORK]",
  "retryable": "boolean",
  "customerId": "string",
  "locationCode": "string",
  "timestamp": "string (ISO 8601)",
  "processedAt": "string (ISO 8601)",
  "acknowledgedAt": "string (ISO 8601)",
  "acknowledgedBy": "string",
  "resolvedAt": "string (ISO 8601)",
  "resolvedBy": "string",
  "retryCount": "integer",
  "lastRetryAt": "string (ISO 8601)"
}
```

### RetryAttempt

```json
{
  "id": "integer",
  "attemptNumber": "integer",
  "status": "enum [PENDING, SUCCESS, FAILED]",
  "initiatedBy": "string",
  "initiatedAt": "string (ISO 8601)",
  "completedAt": "string (ISO 8601)",
  "result": {
    "success": "boolean",
    "message": "string",
    "responseCode": "integer",
    "errorDetails": "object"
  }
}
```

## SDK and Client Libraries

### Java Client

```java
// Example usage with Java client
ExceptionCollectorClient client = ExceptionCollectorClient.builder()
    .baseUrl("https://api.biopro.com")
    .bearerToken("your-jwt-token")
    .build();

PagedResponse<ExceptionListResponse> exceptions = client.listExceptions(
    ExceptionFilter.builder()
        .interfaceType(InterfaceType.ORDER)
        .status(ExceptionStatus.NEW)
        .build()
);
```

### cURL Examples

See the individual endpoint documentation above for cURL examples.

## Changelog

### Version 1.0.0
- Initial API release
- Exception listing and search functionality
- Retry management capabilities
- Management operations for acknowledgment and resolution

## Support

For API support and questions:
- Email: api-support@biopro.com
- Documentation: https://docs.biopro.com/exception-collector
- Status Page: https://status.biopro.com