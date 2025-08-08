# Interface Exception Collector Service - API Authentication Guide

## 🔑 JWT Token Generation

This service uses JWT (JSON Web Token) authentication for protected endpoints. Here's how to generate tokens for testing.

### Prerequisites

- Node.js installed on your system
- The service running on `http://localhost:8080`

### Quick Token Generation

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local -Dmaven.test.skip=true &
```

Use the provided script to generate JWT tokens:

```bash
# Generate token for default admin user
node generate-jwt.js

# Generate token for specific user
node generate-jwt.js "john.doe"

# Generate token with specific roles
node generate-jwt.js "jane.smith" "OPERATOR,VIEWER"

# Generate token for viewer only
node generate-jwt.js "viewer-user" "VIEWER"
```

### Available Roles

| Role | Permissions |
|------|-------------|
| `ADMIN` | Full access to all endpoints including actuator endpoints |
| `OPERATOR` | Can read exceptions, retry, acknowledge, and resolve |
| `VIEWER` | Read-only access to exception data |

### Token Expiration

- Default expiration: **1 hour**
- Tokens include expiration time in the output
- Generate a new token when the current one expires

---

## 📡 API Endpoints

### Public Endpoints (No Authentication Required)

#### Health Check
```http
GET /actuator/health
```
**Description:** Check service health status  
**Headers:** None required  
**Response:** Service health information including database, Redis, and circuit breaker status

#### Service Info
```http
GET /actuator/info
```
**Description:** Get service information  
**Headers:** None required  
**Response:** Application metadata and build information

#### API Documentation
```http
GET /swagger-ui/index.html
```
**Description:** Interactive API documentation  
**Headers:** None required  
**Response:** Swagger UI interface

---

### Protected Endpoints (JWT Authentication Required)

All protected endpoints require the `Authorization` header:
```
Authorization: Bearer <your-jwt-token>
```

#### Exception Management

##### List Exceptions
```http
GET /api/v1/exceptions
```
**Required Roles:** `VIEWER`, `OPERATOR`, `ADMIN`  
**Query Parameters:**
- `interfaceType` (optional): Filter by interface type
- `status` (optional): Filter by exception status
- `severity` (optional): Filter by severity level
- `sourceSystem` (optional): Filter by source system
- `fromDate` (optional): Filter from date (ISO 8601)
- `toDate` (optional): Filter to date (ISO 8601)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort criteria

**Example:**
```bash
curl -H "Authorization: Bearer <token>" \
     "http://localhost:8080/api/v1/exceptions?status=OPEN&page=0&size=10"
```

##### Get Exception Details
```http
GET /api/v1/exceptions/{id}
```
**Required Roles:** `VIEWER`, `OPERATOR`, `ADMIN`  
**Path Parameters:**
- `id`: Exception ID (UUID)

##### Retry Exception
```http
POST /api/v1/exceptions/{id}/retry
```
**Required Roles:** `OPERATOR`, `ADMIN`  
**Path Parameters:**
- `id`: Exception ID (UUID)
**Body:** Optional retry configuration

##### Acknowledge Exception
```http
PUT /api/v1/exceptions/{id}/acknowledge
```
**Required Roles:** `OPERATOR`, `ADMIN`  
**Path Parameters:**
- `id`: Exception ID (UUID)
**Body:**
```json
{
  "acknowledgedBy": "user-id",
  "notes": "Acknowledgment notes"
}
```

##### Resolve Exception
```http
PUT /api/v1/exceptions/{id}/resolve
```
**Required Roles:** `OPERATOR`, `ADMIN`  
**Path Parameters:**
- `id`: Exception ID (UUID)
**Body:**
```json
{
  "resolvedBy": "user-id",
  "resolution": "Resolution description",
  "notes": "Additional notes"
}
```

#### Retry Management

##### List Retry Attempts
```http
GET /api/v1/retries
```
**Required Roles:** `VIEWER`, `OPERATOR`, `ADMIN`  
**Query Parameters:**
- `exceptionId` (optional): Filter by exception ID
- `status` (optional): Filter by retry status
- `page` (optional): Page number
- `size` (optional): Page size

##### Get Retry Details
```http
GET /api/v1/retries/{id}
```
**Required Roles:** `VIEWER`, `OPERATOR`, `ADMIN`  
**Path Parameters:**
- `id`: Retry attempt ID (UUID)

#### Data Management

##### Export Exceptions
```http
GET /api/v1/data/export
```
**Required Roles:** `OPERATOR`, `ADMIN`  
**Query Parameters:**
- `format`: Export format (CSV, JSON)
- `fromDate`: Start date for export
- `toDate`: End date for export

##### Archive Old Data
```http
POST /api/v1/data/archive
```
**Required Roles:** `ADMIN`  
**Body:**
```json
{
  "olderThanDays": 90,
  "dryRun": false
}
```

---

### Admin-Only Endpoints (ADMIN Role Required)

#### Actuator Endpoints

##### Metrics
```http
GET /actuator/metrics
```
**Description:** Application metrics  
**Required Roles:** `ADMIN`

##### Specific Metric
```http
GET /actuator/metrics/{metricName}
```
**Description:** Specific metric details  
**Required Roles:** `ADMIN`

##### Prometheus Metrics
```http
GET /actuator/prometheus
```
**Description:** Metrics in Prometheus format  
**Required Roles:** `ADMIN`

##### Environment
```http
GET /actuator/env
```
**Description:** Environment properties  
**Required Roles:** `ADMIN`

##### Configuration Properties
```http
GET /actuator/configprops
```
**Description:** Configuration properties  
**Required Roles:** `ADMIN`

#### Management Endpoints

##### Trigger Data Cleanup
```http
POST /api/v1/management/cleanup
```
**Required Roles:** `ADMIN`  
**Body:**
```json
{
  "retentionDays": 90,
  "batchSize": 1000
}
```

##### Cache Management
```http
DELETE /api/v1/management/cache/{cacheName}
```
**Required Roles:** `ADMIN`  
**Path Parameters:**
- `cacheName`: Name of cache to clear

---

## 🛠️ Bruno/Postman Setup

### Environment Variables
Create these variables in your API client:

```
base_url = http://localhost:8080
jwt_token = <your-generated-token>
```

### Common Headers
For protected endpoints, always include:

```
Authorization: Bearer {{jwt_token}}
Content-Type: application/json
```

### Example Requests

#### Get Health Status
```
GET {{base_url}}/actuator/health
```

#### List Exceptions with Authentication
```
GET {{base_url}}/api/v1/exceptions?page=0&size=10
Authorization: Bearer {{jwt_token}}
```

#### Retry an Exception
```
POST {{base_url}}/api/v1/exceptions/{{exception_id}}/retry
Authorization: Bearer {{jwt_token}}
Content-Type: application/json

{
  "reason": "Manual retry requested"
}
```

---

## 🔍 Response Formats

### Success Response
```json
{
  "data": { ... },
  "status": "success",
  "timestamp": "2025-08-07T22:30:00Z"
}
```

### Error Response
```json
{
  "status": 400,
  "error": "INVALID_ARGUMENT",
  "message": "Detailed error message",
  "path": "/api/v1/exceptions",
  "timestamp": "2025-08-07T22:30:00Z"
}
```

### Paginated Response
```json
{
  "content": [...],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

---

## 🚨 Common HTTP Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request parameters |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server error |

---

## 🔧 Troubleshooting

### Token Issues

**Problem:** Getting 401 Unauthorized  
**Solution:** 
1. Check if token is expired
2. Ensure `Bearer ` prefix in Authorization header
3. Generate a new token

**Problem:** Getting 403 Forbidden  
**Solution:** 
1. Check if your role has permission for the endpoint
2. Generate token with appropriate role (ADMIN for actuator endpoints)

### Rate Limiting

**Problem:** Getting 429 Too Many Requests  
**Solution:** 
1. Wait before making more requests
2. Check rate limit headers in response
3. Default limit: 60 requests per minute

### Connection Issues

**Problem:** Connection refused  
**Solution:** 
1. Ensure service is running: `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`
2. Check if port 8080 is available
3. Verify Docker services are running: `docker-compose ps`

---

## 📝 Notes

- All timestamps are in ISO 8601 format (UTC)
- UUIDs are used for resource identifiers
- Pagination uses zero-based page numbers
- Default page size is 20 items
- Maximum page size is 100 items
- All request/response bodies use JSON format
- CORS is enabled for development