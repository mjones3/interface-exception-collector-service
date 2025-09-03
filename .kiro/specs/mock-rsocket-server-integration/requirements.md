# Mock RSocket Server Integration Requirements Document

## Introduction

The BioPro Interface Exception Collector service requires enhanced capabilities for order data retrieval during OrderRejected event processing. To support development and testing workflows, this enhancement introduces integration with a containerized mock RSocket server that simulates Partner Order Service responses.

This approach enables reliable testing and development without dependencies on external services while maintaining the same integration patterns that will be used in production environments.

## Requirements

### Requirement 1: Mock RSocket Server Container Integration

**User Story:** As a developer working on BioPro, I want the Interface Exception Collector to retrieve order data from a containerized mock RSocket server when processing OrderRejected events, so that I can develop and test retry operations without external service dependencies.

#### Acceptance Criteria

1. WHEN an OrderRejected event is received THEN the system SHALL immediately call the mock RSocket server via RSocket client to retrieve complete order data using the externalId from the OrderRejected event payload
2. WHEN making the RSocket call THEN the system SHALL connect to the configurable mock server endpoint (default: localhost:7000)
3. WHEN the order data is successfully retrieved THEN the system SHALL store the complete order JSON in the interface_exceptions.order_received JSONB field
4. WHEN storing the exception record THEN the system SHALL ensure the order_received field is populated before the database transaction commits
5. IF the mock RSocket call fails THEN the system SHALL log the error but still create the exception record with a null order_received field and mark it as non-retryable

### Requirement 2: Mock Server Mapping Configuration

**User Story:** As a developer, I want to configure mock RSocket server responses for different order scenarios, so that I can test various order data structures and error conditions.

#### Acceptance Criteria

1. WHEN the mock server starts THEN it SHALL load mapping files from the mappings directory using the provided mapping structure:
   ```json
   {
     "request": {
       "routePath": "orders/{externalId}"
     },
     "response": {
       "bodyFileName": "complete-order-response.json"
     }
   }
   ```
2. WHEN creating mapping files THEN the system SHALL support routePath, routePathTemplate, and routePathPattern matching as demonstrated in the mock server code
3. WHEN the mock server receives a request THEN it SHALL return order data with multiple order items as compatible with the Bruno collection examples
4. WHEN no mapping matches THEN the mock server SHALL return a default "endpoint not found" response using jsonBody format
5. WHEN mapping files are updated THEN they SHALL be reloaded without requiring server restart

### Requirement 3: Enhanced OrderRejected Event Processing

**User Story:** As a system operator, I want the Interface Exception Collector to process the updated OrderRejected event schema with proper field mapping, so that order retrieval works with the new event structure.

#### Acceptance Criteria

1. WHEN processing OrderRejected events THEN the system SHALL handle the updated event schema with required fields: eventId, occurredOn, payload, eventType, eventVersion
2. WHEN extracting order information THEN the system SHALL use payload.externalId (pattern: ^[A-Z0-9-]+$) to retrieve order data from the mock server
3. WHEN logging operations THEN the system SHALL include payload.transactionId, payload.operation, and payload.rejectedReason for traceability
4. WHEN validation fails THEN the system SHALL log schema validation errors and create non-retryable exception records
5. WHEN the eventType is not "OrderRejected" or eventVersion is not "1.0" THEN the system SHALL log warnings but continue processing

### Requirement 4: Container Integration with Tilt

**User Story:** As a developer, I want the mock RSocket server to be automatically deployed as part of the local development environment, so that I can run integration tests without manual setup.

#### Acceptance Criteria

1. WHEN Tilt starts THEN it SHALL pull and run the mock RSocket server container from artifactory.sha.ao.arc-one.com/docker/biopro/utils/rsocket_mock:21.0.1
2. WHEN the container starts THEN it SHALL expose port 7000 for RSocket connections
3. WHEN mapping files are modified THEN Tilt SHALL detect changes and reload the container with updated mappings
4. WHEN the container is healthy THEN the Interface Exception Collector SHALL be able to connect successfully
5. WHEN Tilt shuts down THEN all container resources SHALL be cleaned up properly

### Requirement 5: Order Data Mock Responses

**User Story:** As a developer testing retry operations, I want realistic order data returned from the mock server, so that I can validate end-to-end functionality with representative data.

#### Acceptance Criteria

1. WHEN the mock server returns order data THEN it SHALL include a complete order structure with multiple order items compatible with BioPro's domain model
2. WHEN different externalId patterns are requested THEN the mock server SHALL return appropriate test data (e.g., TEST-ORDER-1, TEST-ORD-2025-018)
3. WHEN creating mock responses THEN they SHALL include all necessary fields for retry operations including customer information, product details, and transaction metadata
4. WHEN testing error scenarios THEN specific externalId patterns SHALL trigger error responses to test failure handling
5. WHEN order data is stored THEN it SHALL be valid JSON that can be deserialized for retry operations

### Requirement 6: Configuration and Environment Support

**User Story:** As a DevOps engineer, I want to configure the mock RSocket server integration for different environments, so that development, testing, and staging environments can use appropriate mock data.

#### Acceptance Criteria

1. WHEN deploying the application THEN it SHALL support configuration of the mock RSocket server endpoint via environment variables or application properties
2. WHEN the mock server is unavailable THEN the system SHALL implement circuit breaker patterns to prevent cascading failures
3. WHEN configuring timeouts THEN the system SHALL use environment-specific timeout values for RSocket calls
4. WHEN enabling debug mode THEN the system SHALL provide detailed logging of mock server interactions
5. WHEN feature flags are used THEN the system SHALL support enabling/disabling mock server integration vs. real service calls

### Requirement 7: Development and Testing Enhancement

**User Story:** As a developer, I want comprehensive mock server integration that supports various testing scenarios, so that I can validate both happy path and error conditions.

#### Acceptance Criteria

1. WHEN running integration tests THEN the mock server SHALL support pre-configured test scenarios with known order data
2. WHEN testing retry operations THEN previously retrieved order data from mock responses SHALL be used for retry attempts
3. WHEN simulating service failures THEN the mock server SHALL support configurable failure rates and timeout scenarios
4. WHEN validating correlation tracking THEN all mock server interactions SHALL be logged with correlation IDs
5. WHEN performance testing THEN the mock server SHALL handle concurrent requests without degrading response times

### Requirement 8: Mock Server Health and Monitoring

**User Story:** As a system administrator, I want to monitor the health and performance of the mock RSocket server integration, so that I can identify and resolve issues quickly.

#### Acceptance Criteria

1. WHEN the mock server is running THEN it SHALL provide health check endpoints for container orchestration
2. WHEN monitoring performance THEN the system SHALL record metrics for mock server response times and success rates
3. WHEN errors occur THEN the system SHALL increment failure counters with categorization (timeout, connection failure, invalid response)
4. WHEN load testing THEN the mock server SHALL maintain consistent performance under expected concurrent load
5. WHEN troubleshooting THEN all mock server interactions SHALL be traceable through correlation IDs and structured logging

## Technical Implementation Notes

### RSocket Client Configuration
- Connection endpoint: Configurable (default: rsocket://localhost:7000)
- Timeout settings: Environment-specific (development: 5s, testing: 10s)
- Retry policy: 3 attempts with exponential backoff
- Circuit breaker: Open after 5 consecutive failures

### Mock Server Setup
- Container image: artifactory.sha.ao.arc-one.com/docker/biopro/utils/rsocket_mock:21.0.1
- Port mapping: 7000:7000
- Volume mounts: ./mappings:/app/mappings, ./mock-responses:/app/__files
- Health check: TCP connection on port 7000

### Mapping File Structure
```
mappings/
├── order-success.json          # Successful order retrieval
├── order-not-found.json        # Order not found scenario
├── order-validation-error.json # Invalid order data
└── default-fallback.json       # Catch-all for unmapped requests

__files/
├── complete-order-with-items.json
├── minimal-order.json
└── error-responses/
    ├── not-found.json
    └── validation-error.json
```

This requirements document provides a comprehensive framework for integrating the mock RSocket server with BioPro's Interface Exception Collector while maintaining the same architectural patterns that will be used with the actual Partner Order Service in production environments.