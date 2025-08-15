# Partner Order Service

A microservice for processing blood product orders from external partners, integrated with the BioPro Interface Exception Collector Service for comprehensive exception handling and retry capabilities.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Testing Examples](#testing-examples)
- [Integration](#integration)
- [Development](#development)
- [Troubleshooting](#troubleshooting)

## Overview

The Partner Order Service provides a REST API for external partners to submit blood product orders. It validates incoming orders, stores them for audit purposes, and publishes events to Kafka for downstream processing. The service integrates seamlessly with the Interface Exception Collector Service to provide retry capabilities and comprehensive exception management.

### Key Features

- **External Partner API**: No authentication required for easy partner integration
- **Order Validation**: JSON schema validation with detailed error reporting
- **Event Publishing**: Publishes OrderReceived, OrderRejected, and InvalidOrderEvent to Kafka
- **Retry Support**: Integrates with Interface Exception Collector for automatic retry capabilities
- **Audit Trail**: Stores all order payloads for compliance and debugging
- **Rate Limiting**: Prevents abuse and ensures system stability

### Architecture

```
External Partners → Partner Order Service → Kafka Events → Interface Exception Collector
                           ↓
                    PostgreSQL Storage
```

## Features

- ✅ **No Authentication Required** - Simplified integration for external partners
- ✅ **JSON Schema Validation** - Comprehensive validation with detailed error messages
- ✅ **Event-Driven Architecture** - Publishes events to Kafka for downstream processing
- ✅ **Retry Capabilities** - Integration with Interface Exception Collector for retry operations
- ✅ **Audit Logging** - Complete audit trail of all order submissions
- ✅ **Rate Limiting** - Protection against abuse and system overload
- ✅ **Health Monitoring** - Built-in health checks and metrics

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker and Docker Compose (for local development)
- Kafka cluster (provided by docker-compose)
- PostgreSQL database (provided by docker-compose)

### Local Development Setup

1. **Start the infrastructure services:**
   ```bash
   # From the root project directory
   docker-compose up -d postgres-partner kafka redis
   ```

2. **Run database migrations:**
   ```bash
   # From the root project directory
   ./scripts/run-migrations.sh --partner-service
   ```

3. **Start the Partner Order Service:**
   ```bash
   cd partner-order-service
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

4. **Verify the service is running:**
   ```bash
   curl http://localhost:8090/actuator/health
   ```

### Access Points

- **API Base URL**: http://localhost:8090
- **Health Check**: http://localhost:8090/actuator/health
- **Metrics**: http://localhost:8090/actuator/metrics
- **Debug Port**: 5006 (for remote debugging)

## API Documentation

### Submit Order

**Endpoint:** `POST /v1/partner-order-provider/orders`

**Description:** Submit a new blood product order for processing. No authentication required.

**Request Headers:**
- `Content-Type: application/json`
- `X-Retry-Attempt: <number>` (optional, used by Interface Exception Collector for retries)
- `X-Original-Transaction-ID: <uuid>` (optional, used by Interface Exception Collector for retries)

**Request Body Schema:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `externalId` | string | ✅ | Partner's unique order identifier (1-255 chars) |
| `orderStatus` | string | ✅ | Must be "OPEN" |
| `locationCode` | string | ✅ | Hospital/location identifier (1-255 chars) |
| `shipmentType` | string | ✅ | Must be "CUSTOMER" |
| `productCategory` | string | ✅ | Product category description |
| `orderItems` | array | ✅ | Array of order items (minimum 1) |
| `createDate` | string | ❌ | Order date in "YYYY-MM-DD HH:MM:SS" format |
| `deliveryType` | string | ❌ | "DATE_TIME", "SCHEDULED", "STAT", "ROUTINE", "ASAP" |

**Order Item Schema:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `productFamily` | string | ✅ | Product type (e.g., "RED_BLOOD_CELLS_LEUKOREDUCED") |
| `bloodType` | string | ✅ | Blood type (e.g., "O-", "A+") |
| `quantity` | integer | ✅ | Number of units (minimum 1) |
| `comments` | string | ❌ | Optional notes about the item |

**Response Codes:**
- `202 Accepted` - Order accepted for processing
- `400 Bad Request` - Validation errors
- `409 Conflict` - Duplicate external ID
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - System error

### Retrieve Order Payload

**Endpoint:** `GET /v1/partner-order-provider/orders/{transactionId}/payload`

**Description:** Retrieve the original order payload for a given transaction ID. This endpoint is primarily used by the Interface Exception Collector Service for retry operations, but can also be used for debugging, auditing, or manual order reprocessing.

**Path Parameters:**
- `transactionId` (UUID) - Transaction ID returned from order submission

**Request Headers:**
- No authentication required
- `Accept: application/json` (optional)

**Response Codes:**
- `200 OK` - Payload retrieved successfully
- `404 Not Found` - Transaction ID not found
- `500 Internal Server Error` - System error

**Success Response Example (200 OK):**
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "externalId": "ORDER-TEST-12345",
  "retrievedAt": "2025-08-13T15:30:00.000Z",
  "originalPayload": {
    "externalId": "ORDER-TEST-12345",
    "orderStatus": "OPEN",
    "locationCode": "HOSP-NYC-001",
    "shipmentType": "CUSTOMER",
    "deliveryType": "ROUTINE",
    "productCategory": "BLOOD_PRODUCTS",
    "createDate": "2025-08-13 14:30:00",
    "orderItems": [
      {
        "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
        "bloodType": "O-",
        "quantity": 2,
        "comments": "Urgent request for surgery"
      }
    ]
  },
  "submittedAt": "2025-08-13T14:30:00.000Z",
  "status": "STORED"
}
```

**Error Response Example (404 Not Found):**
```json
{
  "status": "NOT_FOUND",
  "message": "No order found for transaction ID: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": "2025-08-13T15:30:00.000Z"
}
```

## Testing Examples

### Basic Order Example

```json
{
  "externalId": "ORDER-TEST-12345",
  "orderStatus": "OPEN",
  "locationCode": "HOSP-NYC-001",
  "shipmentType": "CUSTOMER",
  "deliveryType": "ROUTINE",
  "productCategory": "BLOOD_PRODUCTS",
  "createDate": "2025-08-13 14:30:00",
  "orderItems": [
    {
      "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
      "bloodType": "O-",
      "quantity": 2,
      "comments": "Urgent request for surgery"
    },
    {
      "productFamily": "PLASMA_TRANSFUSABLE",
      "bloodType": "AB+",
      "quantity": 1,
      "comments": "Backup plasma unit"
    }
  ]
}
```

### Emergency Order Example

```json
{
  "externalId": "ORDER-EMERGENCY-67890",
  "orderStatus": "OPEN",
  "locationCode": "HOSP-MOUNT-SINAI-001",
  "shipmentType": "CUSTOMER",
  "deliveryType": "STAT",
  "productCategory": "EMERGENCY_BLOOD_PRODUCTS",
  "createDate": "2025-08-13 15:45:30",
  "orderItems": [
    {
      "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
      "bloodType": "O-",
      "quantity": 4,
      "comments": "Emergency trauma case - multiple units needed"
    },
    {
      "productFamily": "PLASMA_TRANSFUSABLE",
      "bloodType": "O-",
      "quantity": 2,
      "comments": "Fresh frozen plasma for coagulation support"
    }
  ]
}
```

### Minimal Required Fields Example

```json
{
  "externalId": "ORDER-MINIMAL-999",
  "orderStatus": "OPEN",
  "locationCode": "HOSP-TEST-001",
  "shipmentType": "CUSTOMER",
  "productCategory": "BLOOD_PRODUCTS",
  "orderItems": [
    {
      "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
      "bloodType": "A+",
      "quantity": 1
    }
  ]
}
```

### cURL Examples

**Submit a Basic Order:**
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "ORDER-TEST-12345",
    "orderStatus": "OPEN",
    "locationCode": "HOSP-NYC-001",
    "shipmentType": "CUSTOMER",
    "deliveryType": "ROUTINE",
    "productCategory": "BLOOD_PRODUCTS",
    "orderItems": [
      {
        "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
        "bloodType": "O-",
        "quantity": 2,
        "comments": "Urgent request for surgery"
      }
    ]
  }' \
  "http://localhost:8090/v1/partner-order-provider/orders"
```

**Submit an Emergency Order:**
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "ORDER-EMERGENCY-67890",
    "orderStatus": "OPEN",
    "locationCode": "HOSP-MOUNT-SINAI-001",
    "shipmentType": "CUSTOMER",
    "deliveryType": "STAT",
    "productCategory": "EMERGENCY_BLOOD_PRODUCTS",
    "orderItems": [
      {
        "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
        "bloodType": "O-",
        "quantity": 4,
        "comments": "Emergency trauma case"
      }
    ]
  }' \
  "http://localhost:8090/v1/partner-order-provider/orders"
```

**Retrieve Order Payload:**
```bash
# Replace {transactionId} with actual transaction ID from order submission response
curl "http://localhost:8090/v1/partner-order-provider/orders/550e8400-e29b-41d4-a716-446655440000/payload"
```

**Complete Workflow Example:**
```bash
# Step 1: Submit an order and capture the transaction ID
RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "ORDER-WORKFLOW-TEST",
    "orderStatus": "OPEN",
    "locationCode": "HOSP-NYC-001",
    "shipmentType": "CUSTOMER",
    "productCategory": "BLOOD_PRODUCTS",
    "orderItems": [
      {
        "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
        "bloodType": "A+",
        "quantity": 1
      }
    ]
  }' \
  "http://localhost:8090/v1/partner-order-provider/orders")

# Step 2: Extract transaction ID from response
TRANSACTION_ID=$(echo $RESPONSE | jq -r '.transactionId')
echo "Order submitted with transaction ID: $TRANSACTION_ID"

# Step 3: Retrieve the original payload
curl "http://localhost:8090/v1/partner-order-provider/orders/$TRANSACTION_ID/payload"
```

**Using jq for JSON Processing:**
```bash
# Submit order and extract transaction ID in one command
TRANSACTION_ID=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"externalId":"ORDER-JQ-TEST","orderStatus":"OPEN","locationCode":"HOSP-TEST","shipmentType":"CUSTOMER","productCategory":"BLOOD_PRODUCTS","orderItems":[{"productFamily":"RED_BLOOD_CELLS_LEUKOREDUCED","bloodType":"O+","quantity":1}]}' \
  "http://localhost:8090/v1/partner-order-provider/orders" | jq -r '.transactionId')

# Retrieve and format the payload
curl -s "http://localhost:8090/v1/partner-order-provider/orders/$TRANSACTION_ID/payload" | jq '.'
```

### Response Examples

**Successful Order Submission (202 Accepted):**
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "ACCEPTED",
  "message": "Order accepted for processing",
  "submittedAt": "2025-08-13T14:30:00.000Z"
}
```

**Validation Error (400 Bad Request):**
```json
{
  "status": "VALIDATION_FAILED",
  "message": "Order validation failed",
  "errors": [
    "externalId is required and cannot be empty",
    "orderItems must contain at least one item",
    "orderItems[0].quantity must be greater than 0"
  ],
  "timestamp": "2025-08-13T14:30:00.000Z"
}
```

**Duplicate Order (409 Conflict):**
```json
{
  "status": "DUPLICATE_ORDER",
  "message": "Order with external ID 'ORDER-TEST-12345' already exists",
  "existingTransactionId": "550e8400-e29b-41d4-a716-446655440001",
  "timestamp": "2025-08-13T14:30:00.000Z"
}
```

## Payload Retrieval Use Cases

### 1. Debugging Failed Orders

When an order fails processing, you can retrieve the original payload to understand what was submitted:

```bash
# Get the transaction ID from logs or exception records
TRANSACTION_ID="550e8400-e29b-41d4-a716-446655440000"

# Retrieve the original payload for analysis
curl -s "http://localhost:8090/v1/partner-order-provider/orders/$TRANSACTION_ID/payload" | jq '.originalPayload'
```

### 2. Manual Order Reprocessing

If you need to manually reprocess an order with modifications:

```bash
# Step 1: Retrieve the original payload
ORIGINAL_PAYLOAD=$(curl -s "http://localhost:8090/v1/partner-order-provider/orders/$TRANSACTION_ID/payload" | jq '.originalPayload')

# Step 2: Modify the payload (e.g., change external ID for resubmission)
MODIFIED_PAYLOAD=$(echo $ORIGINAL_PAYLOAD | jq '.externalId = "ORDER-RETRY-" + (.externalId | tostring)')

# Step 3: Resubmit the modified order
curl -X POST \
  -H "Content-Type: application/json" \
  -d "$MODIFIED_PAYLOAD" \
  "http://localhost:8090/v1/partner-order-provider/orders"
```

### 3. Audit and Compliance

Retrieve order payloads for audit purposes:

```bash
# Get multiple order payloads for audit
TRANSACTION_IDS=("550e8400-e29b-41d4-a716-446655440000" "550e8400-e29b-41d4-a716-446655440001")

for TRANSACTION_ID in "${TRANSACTION_IDS[@]}"; do
  echo "=== Order $TRANSACTION_ID ==="
  curl -s "http://localhost:8090/v1/partner-order-provider/orders/$TRANSACTION_ID/payload" | jq '{
    transactionId: .transactionId,
    externalId: .externalId,
    submittedAt: .submittedAt,
    locationCode: .originalPayload.locationCode,
    totalItems: (.originalPayload.orderItems | length)
  }'
  echo ""
done
```

### 4. Data Analysis and Reporting

Extract specific information from stored orders:

```bash
# Extract order statistics
curl -s "http://localhost:8090/v1/partner-order-provider/orders/$TRANSACTION_ID/payload" | jq '{
  orderSummary: {
    externalId: .externalId,
    location: .originalPayload.locationCode,
    deliveryType: .originalPayload.deliveryType,
    totalItems: (.originalPayload.orderItems | length),
    bloodTypes: [.originalPayload.orderItems[].bloodType] | unique,
    totalQuantity: [.originalPayload.orderItems[].quantity] | add
  }
}'
```

### 5. Integration Testing

Test the complete order lifecycle:

```bash
#!/bin/bash
# Complete integration test script

echo "🧪 Testing Partner Order Service Integration"

# Submit test order
echo "📤 Submitting test order..."
RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "ORDER-INTEGRATION-TEST-'$(date +%s)'",
    "orderStatus": "OPEN",
    "locationCode": "HOSP-INTEGRATION-TEST",
    "shipmentType": "CUSTOMER",
    "deliveryType": "STAT",
    "productCategory": "EMERGENCY_BLOOD_PRODUCTS",
    "orderItems": [
      {
        "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
        "bloodType": "O-",
        "quantity": 2,
        "comments": "Integration test order"
      }
    ]
  }' \
  "http://localhost:8090/v1/partner-order-provider/orders")

# Check if submission was successful
if echo "$RESPONSE" | jq -e '.transactionId' > /dev/null; then
  TRANSACTION_ID=$(echo "$RESPONSE" | jq -r '.transactionId')
  echo "✅ Order submitted successfully: $TRANSACTION_ID"
  
  # Wait a moment for processing
  sleep 2
  
  # Retrieve the payload
  echo "📥 Retrieving order payload..."
  PAYLOAD_RESPONSE=$(curl -s "http://localhost:8090/v1/partner-order-provider/orders/$TRANSACTION_ID/payload")
  
  # Verify payload retrieval
  if echo "$PAYLOAD_RESPONSE" | jq -e '.originalPayload' > /dev/null; then
    echo "✅ Payload retrieved successfully"
    echo "📊 Order Details:"
    echo "$PAYLOAD_RESPONSE" | jq '{
      transactionId: .transactionId,
      externalId: .externalId,
      location: .originalPayload.locationCode,
      itemCount: (.originalPayload.orderItems | length)
    }'
  else
    echo "❌ Failed to retrieve payload"
    echo "$PAYLOAD_RESPONSE"
  fi
else
  echo "❌ Order submission failed"
  echo "$RESPONSE"
fi
```

## Integration

### With Interface Exception Collector Service

The Partner Order Service integrates seamlessly with the Interface Exception Collector Service to provide comprehensive exception handling and retry capabilities.

**Event Flow:**
1. Partner submits order → Partner Order Service validates and stores order
2. Partner Order Service publishes events to Kafka (OrderReceived, OrderRejected, InvalidOrderEvent)
3. Interface Exception Collector consumes events and creates exception records
4. For retries, Interface Exception Collector retrieves original payload and resubmits order

**Retry Process:**
1. Interface Exception Collector calls `GET /v1/partner-order-provider/orders/{transactionId}/payload`
2. Interface Exception Collector resubmits order with `X-Retry-Attempt` and `X-Original-Transaction-ID` headers
3. Partner Order Service processes retry through same validation logic
4. Interface Exception Collector updates retry history based on response

**Manual Retry Simulation:**

You can simulate the retry process manually to test the integration:

```bash
# Step 1: Submit an initial order that might fail
INITIAL_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "ORDER-RETRY-TEST-'$(date +%s)'",
    "orderStatus": "OPEN",
    "locationCode": "HOSP-RETRY-TEST",
    "shipmentType": "CUSTOMER",
    "productCategory": "BLOOD_PRODUCTS",
    "orderItems": [
      {
        "productFamily": "RED_BLOOD_CELLS_LEUKOREDUCED",
        "bloodType": "B-",
        "quantity": 3
      }
    ]
  }' \
  "http://localhost:8090/v1/partner-order-provider/orders")

ORIGINAL_TRANSACTION_ID=$(echo "$INITIAL_RESPONSE" | jq -r '.transactionId')
echo "Original order transaction ID: $ORIGINAL_TRANSACTION_ID"

# Step 2: Retrieve the original payload (simulating Exception Collector)
ORIGINAL_PAYLOAD=$(curl -s "http://localhost:8090/v1/partner-order-provider/orders/$ORIGINAL_TRANSACTION_ID/payload" | jq '.originalPayload')

# Step 3: Modify the external ID for retry (to avoid duplicate error)
RETRY_PAYLOAD=$(echo "$ORIGINAL_PAYLOAD" | jq '.externalId = .externalId + "-RETRY"')

# Step 4: Submit retry with special headers (simulating Exception Collector retry)
RETRY_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -H "X-Retry-Attempt: 1" \
  -H "X-Original-Transaction-ID: $ORIGINAL_TRANSACTION_ID" \
  -d "$RETRY_PAYLOAD" \
  "http://localhost:8090/v1/partner-order-provider/orders")

RETRY_TRANSACTION_ID=$(echo "$RETRY_RESPONSE" | jq -r '.transactionId')
echo "Retry order transaction ID: $RETRY_TRANSACTION_ID"

# Step 5: Verify both payloads are stored
echo "=== Original Payload ==="
curl -s "http://localhost:8090/v1/partner-order-provider/orders/$ORIGINAL_TRANSACTION_ID/payload" | jq '.originalPayload.externalId'

echo "=== Retry Payload ==="
curl -s "http://localhost:8090/v1/partner-order-provider/orders/$RETRY_TRANSACTION_ID/payload" | jq '.originalPayload.externalId'
```

**Automated Retry Testing Script:**

```bash
#!/bin/bash
# Test retry functionality end-to-end

test_retry_functionality() {
  local test_external_id="ORDER-RETRY-FUNC-TEST-$(date +%s)"
  
  echo "🔄 Testing retry functionality with external ID: $test_external_id"
  
  # Submit original order
  echo "📤 Submitting original order..."
  local original_response=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -d "{
      \"externalId\": \"$test_external_id\",
      \"orderStatus\": \"OPEN\",
      \"locationCode\": \"HOSP-RETRY-TEST\",
      \"shipmentType\": \"CUSTOMER\",
      \"productCategory\": \"BLOOD_PRODUCTS\",
      \"orderItems\": [
        {
          \"productFamily\": \"RED_BLOOD_CELLS_LEUKOREDUCED\",
          \"bloodType\": \"AB-\",
          \"quantity\": 1
        }
      ]
    }" \
    "http://localhost:8090/v1/partner-order-provider/orders")
  
  local original_transaction_id=$(echo "$original_response" | jq -r '.transactionId')
  
  if [[ "$original_transaction_id" == "null" ]]; then
    echo "❌ Failed to submit original order"
    return 1
  fi
  
  echo "✅ Original order submitted: $original_transaction_id"
  
  # Retrieve original payload
  echo "📥 Retrieving original payload..."
  local payload_response=$(curl -s "http://localhost:8090/v1/partner-order-provider/orders/$original_transaction_id/payload")
  local original_payload=$(echo "$payload_response" | jq '.originalPayload')
  
  if [[ "$original_payload" == "null" ]]; then
    echo "❌ Failed to retrieve original payload"
    return 1
  fi
  
  echo "✅ Original payload retrieved"
  
  # Create retry payload with modified external ID
  local retry_payload=$(echo "$original_payload" | jq ".externalId = \"$test_external_id-RETRY\"")
  
  # Submit retry
  echo "🔄 Submitting retry order..."
  local retry_response=$(curl -s -X POST \
    -H "Content-Type: application/json" \
    -H "X-Retry-Attempt: 1" \
    -H "X-Original-Transaction-ID: $original_transaction_id" \
    -d "$retry_payload" \
    "http://localhost:8090/v1/partner-order-provider/orders")
  
  local retry_transaction_id=$(echo "$retry_response" | jq -r '.transactionId')
  
  if [[ "$retry_transaction_id" == "null" ]]; then
    echo "❌ Failed to submit retry order"
    return 1
  fi
  
  echo "✅ Retry order submitted: $retry_transaction_id"
  
  # Verify retry payload is stored
  echo "📥 Verifying retry payload storage..."
  local retry_payload_response=$(curl -s "http://localhost:8090/v1/partner-order-provider/orders/$retry_transaction_id/payload")
  local stored_retry_payload=$(echo "$retry_payload_response" | jq '.originalPayload')
  
  if [[ "$stored_retry_payload" == "null" ]]; then
    echo "❌ Failed to retrieve retry payload"
    return 1
  fi
  
  echo "✅ Retry payload stored and retrieved successfully"
  
  # Compare external IDs
  local original_external_id=$(echo "$original_payload" | jq -r '.externalId')
  local retry_external_id=$(echo "$stored_retry_payload" | jq -r '.externalId')
  
  echo "📊 Test Results:"
  echo "   Original External ID: $original_external_id"
  echo "   Retry External ID: $retry_external_id"
  echo "   Original Transaction ID: $original_transaction_id"
  echo "   Retry Transaction ID: $retry_transaction_id"
  
  if [[ "$retry_external_id" == "$test_external_id-RETRY" ]]; then
    echo "✅ Retry functionality test PASSED"
    return 0
  else
    echo "❌ Retry functionality test FAILED"
    return 1
  fi
}

# Run the test
test_retry_functionality
```

### Kafka Events Published

**OrderReceived Event:**
```json
{
  "eventId": "uuid",
  "eventType": "OrderReceived",
  "eventVersion": "1.0",
  "occurredOn": "2025-08-13T14:30:00Z",
  "source": "partner-order-service",
  "correlationId": "uuid",
  "transactionId": "uuid",
  "payload": {
    "externalId": "ORDER-TEST-12345",
    "locationCode": "HOSP-NYC-001",
    "orderData": { /* complete order payload */ }
  }
}
```

**OrderRejected Event (for testing):**
```json
{
  "eventId": "uuid",
  "eventType": "OrderRejected",
  "eventVersion": "1.0",
  "occurredOn": "2025-08-13T14:30:01Z",
  "source": "partner-order-service",
  "correlationId": "uuid",
  "transactionId": "uuid",
  "payload": {
    "externalId": "ORDER-TEST-12345",
    "rejectedReason": "Test event for Interface Exception Collector functionality",
    "operation": "CREATE_ORDER",
    "customerId": "string",
    "locationCode": "HOSP-NYC-001",
    "originalPayload": { /* complete order payload */ }
  }
}
```

## Development

### Project Structure

```
partner-order-service/
├── src/main/java/
│   └── com/arcone/biopro/partner/order/
│       ├── api/                    # REST controllers and DTOs
│       ├── application/            # Business services and use cases
│       ├── domain/                 # Core business entities and events
│       └── infrastructure/         # External integrations (Kafka, DB)
├── src/main/resources/
│   ├── application.yml             # Application configuration
│   ├── application-local.yml       # Local development configuration
│   └── schemas/                    # JSON schemas for validation
├── src/test/                       # Unit and integration tests
├── Dockerfile                      # Production Docker image
├── Dockerfile.dev                  # Development Docker image
└── pom.xml                         # Maven dependencies
```

### Configuration

**Environment Variables:**

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profiles | `local` |
| `PARTNER_ORDER_DATABASE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5433/partner_orders` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | `localhost:9092` |
| `SERVER_PORT` | HTTP server port | `8090` |

**Application Profiles:**
- `local` - Local development with Docker Compose
- `dev` - Development environment
- `staging` - Staging environment
- `prod` - Production environment

### Building and Running

**Build the application:**
```bash
./mvnw clean package
```

**Run tests:**
```bash
./mvnw test
```

**Run with specific profile:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Build Docker image:**
```bash
docker build -t partner-order-service .
```

### Database Schema

The service uses PostgreSQL with the following main tables:

**partner_orders:**
- Stores order metadata and original payloads
- Indexed on external_id and transaction_id for fast lookups

**partner_order_items:**
- Stores individual order items with product details
- Linked to partner_orders via foreign key

**partner_order_events:**
- Audit trail of all events published to Kafka
- Used for debugging and compliance

## Troubleshooting

### Common Issues

**1. Port 8090 already in use:**
```bash
# Check what's using the port
lsof -i :8090

# Kill the process or change the port
export SERVER_PORT=8091
./mvnw spring-boot:run
```

**2. Database connection failed:**
```bash
# Check if PostgreSQL is running
docker-compose ps postgres-partner

# Restart the database
docker-compose restart postgres-partner

# Check database logs
docker-compose logs postgres-partner
```

**3. Kafka connection issues:**
```bash
# Check if Kafka is running
docker-compose ps kafka

# Restart Kafka
docker-compose restart kafka

# Check Kafka logs
docker-compose logs kafka
```

**4. Order validation failures:**
- Check the JSON schema in `src/main/resources/schemas/partner-order-input-schema.json`
- Ensure all required fields are present
- Verify data types match the schema requirements
- Check field length constraints

**5. Duplicate external ID errors:**
- Each `externalId` must be unique across all orders
- Use a different `externalId` for testing
- Check existing orders in the database

**6. Payload retrieval returns 404:**
```bash
# Check if the transaction ID exists in the database
curl -s "http://localhost:8090/v1/partner-order-provider/orders/TRANSACTION_ID/payload" | jq '.'

# Common causes:
# - Transaction ID is incorrect or malformed
# - Order was not successfully stored
# - Database connection issues
# - Order was submitted to a different environment

# Verify transaction ID format (should be a valid UUID)
echo "550e8400-e29b-41d4-a716-446655440000" | grep -E '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'
```

**7. Payload retrieval is slow:**
```bash
# Check database performance
curl -s "http://localhost:8090/actuator/metrics/jdbc.connections.active" | jq '.'

# Check if database indexes are working
# Look for slow query logs in PostgreSQL
docker-compose logs postgres-partner | grep "slow"
```

**8. "Name for argument of type [java.util.UUID] not specified" error:**

This error occurs when the Java compiler doesn't preserve parameter names. This is a common issue with Spring Boot path parameters.

```json
{
  "error": "INVALID_ARGUMENT",
  "message": "Name for argument of type [java.util.UUID] not specified, and parameter name information not available via reflection. Ensure that the compiler uses the '-parameters' flag.",
  "timestamp": "2025-08-14T23:52:05.424Z"
}
```

**Solution:**

1. **Rebuild the application with proper compiler flags:**
   ```bash
   # Clean and rebuild with parameters flag
   cd partner-order-service
   ./mvnw clean compile -Dmaven.compiler.parameters=true
   ./mvnw spring-boot:run
   ```

2. **If the issue persists, check Maven compiler configuration:**
   ```bash
   # Verify the parent pom.xml has the correct compiler configuration
   grep -A 10 "maven-compiler-plugin" ../pom.xml
   ```

3. **Force rebuild with clean slate:**
   ```bash
   # Stop the application
   # Clean everything
   ./mvnw clean
   rm -rf target/
   
   # Rebuild with explicit parameters flag
   ./mvnw compile -Dmaven.compiler.parameters=true
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

4. **Verify the fix:**
   ```bash
   # Test the endpoint that was failing
   curl "http://localhost:8090/v1/partner-order-provider/orders/419e13c3-db10-466d-a175-2afd48a5aef0/payload"
   
   # Should return either valid JSON or a proper 404 error, not the parameters error
   ```

**Root Cause:**
- The Maven compiler plugin wasn't configured with the `-parameters` flag
- This flag is required for Spring to properly bind path parameters like `{transactionId}`
- Without it, Spring can't determine which method parameter corresponds to which path variable

**Prevention:**
- The parent `pom.xml` has been updated with the correct compiler configuration
- Future builds will automatically include the `-parameters` flag
- This ensures proper parameter name preservation for Spring Boot applications

### Health Checks

**Application Health:**
```bash
curl http://localhost:8090/actuator/health
```

**Database Connectivity:**
```bash
curl http://localhost:8090/actuator/health/db
```

**Kafka Connectivity:**
```bash
curl http://localhost:8090/actuator/health/kafka
```

### Logging

**Enable debug logging:**
```bash
# Add to application-local.yml
logging:
  level:
    com.arcone.biopro.partner.order: DEBUG
    org.springframework.kafka: DEBUG
```

**View application logs:**
```bash
# If running with Maven
tail -f target/logs/partner-order-service.log

# If running with Docker
docker-compose logs -f partner-order-service
```

### Testing Integration

**Test with Interface Exception Collector:**

1. Submit an order that will trigger an exception:
```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "ORDER-EXCEPTION-TEST",
    "orderStatus": "OPEN",
    "locationCode": "HOSP-TEST-001",
    "shipmentType": "CUSTOMER",
    "productCategory": "BLOOD_PRODUCTS",
    "orderItems": [
      {
        "productFamily": "INVALID_PRODUCT",
        "bloodType": "INVALID_TYPE",
        "quantity": 0
      }
    ]
  }' \
  "http://localhost:8090/v1/partner-order-provider/orders"
```

2. Check if the exception was captured by the Interface Exception Collector:
```bash
# Generate admin token first
node scripts/generate-admin-token.js | grep "Authorization:" | head -1 > auth_header.txt

# Query exceptions
curl -H @auth_header.txt "http://localhost:8080/api/v1/exceptions"
```

### Performance Monitoring

**View metrics:**
```bash
curl http://localhost:8090/actuator/metrics
```

**Key metrics to monitor:**
- `http.server.requests` - HTTP request metrics
- `kafka.producer` - Kafka producer metrics
- `jdbc.connections` - Database connection pool metrics
- `jvm.memory` - JVM memory usage

**Payload Retrieval Specific Metrics:**
```bash
# Monitor payload retrieval endpoint performance
curl -s "http://localhost:8090/actuator/metrics/http.server.requests" | \
  jq '.measurements[] | select(.statistic == "TOTAL_TIME") | .value' | \
  head -5

# Check payload retrieval success rate
curl -s "http://localhost:8090/actuator/metrics/http.server.requests" | \
  jq '.availableTags[] | select(.tag == "uri") | .values[] | select(. | contains("payload"))'

# Monitor database query performance for payload retrieval
curl -s "http://localhost:8090/actuator/metrics/jdbc.connections.active"
```

**Custom Monitoring Script:**
```bash
#!/bin/bash
# Monitor Partner Order Service payload retrieval health

monitor_payload_retrieval() {
  echo "🔍 Partner Order Service - Payload Retrieval Health Check"
  echo "=================================================="
  
  # Test with a known transaction ID (replace with actual ID from your system)
  local test_transaction_id="550e8400-e29b-41d4-a716-446655440000"
  
  # Check service health
  echo "🏥 Service Health:"
  local health_status=$(curl -s "http://localhost:8090/actuator/health" | jq -r '.status')
  echo "   Status: $health_status"
  
  # Check database connectivity
  echo "🗄️  Database Health:"
  local db_status=$(curl -s "http://localhost:8090/actuator/health/db" | jq -r '.status')
  echo "   Status: $db_status"
  
  # Test payload retrieval endpoint response time
  echo "⏱️  Payload Retrieval Performance:"
  local start_time=$(date +%s%N)
  local response=$(curl -s "http://localhost:8090/v1/partner-order-provider/orders/$test_transaction_id/payload")
  local end_time=$(date +%s%N)
  local response_time=$(( (end_time - start_time) / 1000000 ))
  
  echo "   Response Time: ${response_time}ms"
  
  # Check if response is valid JSON
  if echo "$response" | jq empty 2>/dev/null; then
    echo "   Response Format: ✅ Valid JSON"
    
    # Check response structure
    if echo "$response" | jq -e '.transactionId' > /dev/null 2>&1; then
      echo "   Response Structure: ✅ Contains transaction ID"
    elif echo "$response" | jq -e '.status' > /dev/null 2>&1; then
      local status=$(echo "$response" | jq -r '.status')
      echo "   Response Structure: ⚠️  Error response ($status)"
    else
      echo "   Response Structure: ❌ Unexpected format"
    fi
  else
    echo "   Response Format: ❌ Invalid JSON"
  fi
  
  # Check HTTP metrics
  echo "📊 HTTP Metrics:"
  local total_requests=$(curl -s "http://localhost:8090/actuator/metrics/http.server.requests" | \
    jq '.measurements[] | select(.statistic == "COUNT") | .value' | head -1)
  echo "   Total HTTP Requests: $total_requests"
  
  # Check active database connections
  echo "🔗 Database Connections:"
  local active_connections=$(curl -s "http://localhost:8090/actuator/metrics/jdbc.connections.active" | \
    jq '.measurements[] | select(.statistic == "VALUE") | .value' | head -1)
  echo "   Active Connections: $active_connections"
  
  echo "=================================================="
}

# Run monitoring
monitor_payload_retrieval
```

---

## Support

For issues and questions:
1. Check the [Troubleshooting](#troubleshooting) section
2. Review application logs for error details
3. Verify infrastructure services are running
4. Check the Interface Exception Collector Service integration

For development questions, refer to the main project documentation in the root README.md file.