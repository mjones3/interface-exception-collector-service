# Partner Order Service - Design

## Overview

The Partner Order Service follows a containerized microservices architecture deployed in Kubernetes, adhering to event-driven patterns used throughout BioPro. It integrates seamlessly with the existing Interface Exception Collector Service through Kafka events and direct API endpoints.

## Architecture

### Component Architecture

#### 1. REST API Controller
- **Endpoint Handler**: Exposes `/v1/partner-order-provider/orders` endpoint for both new orders and retry requests
- **Request Processing**: Handles HTTP requests from external partners and Interface Exception Collector retry operations
- **Rate Limiting**: Prevents abuse and ensures system stability
- **Request Logging**: Captures all incoming requests for audit

#### 2. Internal Validation Component
- **Schema Validator**: Validates against the predefined JSON schema
- **Business Rules**: Applies order validation rules
- **Duplicate Detection**: Checks for existing external IDs in database
- **Data Sanitization**: Cleanses and normalizes incoming data
- **Validation Error Handling**: Creates InvalidOrderEvent for schema failures

#### 3. Event Publishing Component
- **Kafka Producer**: Publishes events to existing Kafka cluster topics
- **Event Serialization**: Converts order data to standard event format
- **Correlation Tracking**: Maintains correlation and causation IDs for traceability
- **Retry Mechanism**: Handles failed event publications
- **Multi-Topic Publishing**: Publishes to OrderRecieved, OrderRejected, and InvalidOrderEvent topics

#### 4. Order Storage Component
- **Payload Persistence**: Stores original request payloads in PostgreSQL database
- **Transaction Management**: Maintains transaction lifecycle records
- **Metadata Management**: Adds system metadata to stored records
- **Payload Retrieval API**: Provides endpoint for Interface Exception Collector to retrieve original payloads

## Data Models

### Database Schema

#### Partner Orders Table
- `id` (BIGSERIAL PRIMARY KEY)
- `transaction_id` (UUID NOT NULL UNIQUE)
- `external_id` (VARCHAR(255) NOT NULL UNIQUE)
- `status` (VARCHAR(50) NOT NULL DEFAULT 'RECEIVED')
- `original_payload` (JSONB NOT NULL)
- `location_code` (VARCHAR(255))
- `product_category` (VARCHAR(255))
- `submitted_at` (TIMESTAMP WITH TIME ZONE)
- `processed_at` (TIMESTAMP WITH TIME ZONE)
- `created_at` (TIMESTAMP WITH TIME ZONE)
- `updated_at` (TIMESTAMP WITH TIME ZONE)

#### Partner Order Items Table
- `id` (BIGSERIAL PRIMARY KEY)
- `partner_order_id` (BIGINT REFERENCES partner_orders(id))
- `product_family` (VARCHAR(255) NOT NULL)
- `blood_type` (VARCHAR(50) NOT NULL)
- `quantity` (INTEGER NOT NULL CHECK (quantity > 0))
- `comments` (TEXT)
- `created_at` (TIMESTAMP WITH TIME ZONE)

#### Partner Order Events Table
- `id` (BIGSERIAL PRIMARY KEY)
- `event_id` (UUID NOT NULL UNIQUE)
- `event_type` (VARCHAR(100) NOT NULL)
- `event_version` (VARCHAR(10) NOT NULL DEFAULT '1.0')
- `transaction_id` (UUID NOT NULL)
- `correlation_id` (UUID NOT NULL)
- `source` (VARCHAR(100) NOT NULL DEFAULT 'partner-order-service')
- `topic` (VARCHAR(100) NOT NULL)
- `payload` (JSONB NOT NULL)
- `published_at` (TIMESTAMP WITH TIME ZONE)
- `created_at` (TIMESTAMP WITH TIME ZONE)

## API Endpoints

### POST /v1/partner-order-provider/orders
Creates a new order from partner submission or processes a retry request initiated by the Interface Exception Collector Service.

**Request Headers:**
- `Content-Type: application/json`
- `X-Retry-Attempt: <number>` (optional, set by Interface Exception Collector)
- `X-Original-Transaction-ID: <uuid>` (optional, set by Interface Exception Collector)

**Response Codes:**
- `202 Accepted`: Order accepted for processing
- `400 Bad Request`: Validation errors
- `409 Conflict`: Duplicate external ID
- `429 Too Many Requests`: Rate limit exceeded
- `500 Internal Server Error`: System error

### GET /v1/partner-order-provider/orders/{transactionId}/payload
Retrieves the original order payload for a given transaction ID. Used by the Interface Exception Collector Service to retrieve payloads for retry operations.

**Response Codes:**
- `200 OK`: Payload retrieved successfully
- `404 Not Found`: Transaction ID not found
- `500 Internal Server Error`: System error

## Event Schemas

### OrderReceived Event
```json
{
  "eventId": "uuid",
  "eventType": "OrderReceived",
  "eventVersion": "1.0",
  "occurredOn": "2025-08-14T10:30:00Z",
  "source": "partner-order-service",
  "correlationId": "uuid",
  "transactionId": "uuid",
  "payload": {
    "externalId": "string",
    "locationCode": "string",
    "orderData": "object"
  }
}
```

### OrderRejected Event (for testing)
```json
{
  "eventId": "uuid",
  "eventType": "OrderRejected",
  "eventVersion": "1.0",
  "occurredOn": "2025-08-14T10:30:01Z",
  "source": "partner-order-service",
  "correlationId": "uuid",
  "transactionId": "uuid",
  "payload": {
    "externalId": "string",
    "rejectedReason": "Test event for Interface Exception Collector functionality",
    "operation": "CREATE_ORDER",
    "customerId": "string",
    "locationCode": "string",
    "originalPayload": "object"
  }
}
```

### InvalidOrderEvent
```json
{
  "eventId": "uuid",
  "eventType": "InvalidOrderEvent",
  "eventVersion": "1.0",
  "occurredOn": "2025-08-14T10:30:00Z",
  "source": "partner-order-service",
  "correlationId": "uuid",
  "transactionId": "uuid",
  "payload": {
    "validationErrors": ["array of error messages"],
    "originalRequest": "complete original request payload",
    "failedAt": "2025-08-14T10:30:00Z"
  }
}
```

## Technology Stack

- **Runtime**: Java 17 with Spring Boot 3.x
- **Container**: Docker with distroless base image
- **Orchestration**: Kubernetes with Helm charts
- **Database**: PostgreSQL 15 (dedicated instance)
- **Messaging**: Existing Apache Kafka cluster for event streaming
- **Monitoring**: Prometheus metrics with Micrometer
- **Logging**: Structured JSON logging with correlation IDs

## Integration with Interface Exception Collector

### Event Publication Flow
1. **Successful Orders**: Partner Order Service publishes both `OrderReceived` and `OrderRejected` events (for testing)
2. **Validation Failures**: Partner Order Service publishes `InvalidOrderEvent`
3. **Processing Failures**: Order Service publishes `OrderRejected` events

### Exception Collection Flow
1. Interface Exception Collector consumes `OrderRejected` and `InvalidOrderEvent` events
2. Interface Exception Collector stores exceptions with `interfaceType: "ORDER"`
3. Interface Exception Collector creates retry-capable exception records

### Retry Orchestration Flow
1. Interface Exception Collector calls `GET /v1/partner-order-provider/orders/{transactionId}/payload` to retrieve original order data
2. Interface Exception Collector submits retry using `POST /v1/partner-order-provider/orders` with the retrieved payload
3. Partner Order Service processes retry through same validation and publishing logic
4. Interface Exception Collector updates retry history based on response