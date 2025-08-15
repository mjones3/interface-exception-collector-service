# Partner Order Service - Implementation Tasks

## Task List

- [x] 1. Set up project structure and core configuration
  - Create Spring Boot application with proper package structure
  - Configure application.yml with database, Kafka, and service settings
  - Set up JSON schema validation configuration
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1_

- [x] 2. Implement domain entities and data models
  - [x] 2.1 Create PartnerOrder entity with JPA annotations
    - Map to partner_orders table with all required fields
    - Include transaction ID, external ID, status, and payload fields
    - Add proper indexes and constraints
    - _Requirements: 4.1, 4.2, 5.1, 5.2_
  
  - [x] 2.2 Create PartnerOrderItem entity for order line items
    - Map to partner_order_items table with relationship to PartnerOrder
    - Include product family, blood type, quantity, and comments
    - Add validation constraints for quantity > 0
    - _Requirements: 1.1, 4.4, 5.1_
  
  - [x] 2.3 Create PartnerOrderEvent entity for event tracking
    - Map to partner_order_events table for audit trail
    - Include event ID, type, version, correlation ID, and payload
    - Track all published events for debugging and monitoring
    - _Requirements: 3.4, 4.3_

- [x] 3. Create DTOs and request/response models
  - [x] 3.1 Create PartnerOrderRequest DTO with validation annotations
    - Map to JSON schema requirements with JSR-303 validation
    - Include external ID, order status, location code, and order items
    - Add custom validation for business rules
    - _Requirements: 1.1, 1.2, 2.1_
  
  - [x] 3.2 Create PartnerOrderResponse DTO for API responses
    - Include transaction ID, status, message, and timestamp
    - Support both success and error response formats
    - Add correlation ID for traceability
    - _Requirements: 1.2, 4.2_
  
  - [x] 3.3 Create PayloadResponse DTO for payload retrieval
    - Include transaction ID, original payload, submitted timestamp
    - Support Interface Exception Collector integration format
    - Add payload size and metadata information
    - _Requirements: 5.3, 5.4_

- [ ] 4. Implement JSON schema validation service
  - [ ] 4.1 Create JsonSchemaValidationService
    - Load partner order input schema from classpath
    - Validate incoming requests against JSON schema
    - Return detailed validation error messages
    - _Requirements: 1.1, 1.3, 2.3_
  
  - [ ] 4.2 Create ValidationErrorHandler for error formatting
    - Convert schema validation errors to user-friendly messages
    - Include field-level error details and suggestions
    - Support internationalization for error messages
    - _Requirements: 1.3, 2.3_

- [x] 5. Create repository layer for data access
  - [x] 5.1 Create PartnerOrderRepository with JPA methods
    - Add findByTransactionId and findByExternalId methods
    - Include existsByExternalId for duplicate detection
    - Add custom queries for filtering and searching
    - _Requirements: 1.5, 4.1, 5.1, 5.3_
  
  - [x] 5.2 Create PartnerOrderEventRepository for event tracking
    - Add methods to store and retrieve published events
    - Include queries for event history and debugging
    - Support pagination for large event logs
    - _Requirements: 3.4_

- [x] 6. Implement Kafka event publishing
  - [x] 6.1 Create event DTOs for Kafka messages
    - Create OrderReceivedEvent, OrderRejectedEvent, and InvalidOrderEvent DTOs
    - Include all required fields: eventId, eventType, correlationId, transactionId
    - Add proper JSON serialization annotations
    - _Requirements: 3.1, 3.2, 3.3, 3.4_
  
  - [x] 6.2 Create EventPublishingService for Kafka integration
    - Implement methods to publish OrderReceived events to OrderRecieved topic
    - Implement methods to publish OrderRejected events for testing functionality
    - Implement methods to publish InvalidOrderEvent for validation failures
    - Add error handling and retry logic for failed publications
    - _Requirements: 3.1, 3.2, 3.3, 3.4_
  
  - [x] 6.3 Create EventMapper to convert entities to event DTOs
    - Map PartnerOrder entities to OrderReceived event format
    - Map validation errors to InvalidOrderEvent format
    - Generate proper correlation IDs and event metadata
    - _Requirements: 3.1, 3.3, 3.4_

- [ ] 7. Implement core business services
  - [x] 7.1 Create PartnerOrderService for business logic
    - Implement order processing workflow with validation and storage
    - Generate unique transaction IDs for each order submission
    - Handle duplicate detection and return appropriate HTTP status codes
    - Coordinate between validation, storage, and event publishing
    - _Requirements: 1.1, 1.2, 1.3, 1.5, 4.1, 4.2, 4.4, 5.1, 5.2_
  
  - [x] 7.2 Create PayloadRetrievalService for Interface Exception Collector integration
    - Implement payload retrieval by transaction ID
    - Return original payload exactly as received for retry operations
    - Handle not found cases with proper HTTP 404 responses
    - Add security and access control for payload access
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 8. Create REST API controllers
  - [x] 8.1 Create PartnerOrderController for order submission endpoint
    - Implement POST /v1/partner-order-provider/orders endpoint
    - Handle both new orders and retry requests from Interface Exception Collector
    - Add proper HTTP status codes: 202 Accepted, 400 Bad Request, 409 Conflict
    - Include request logging and correlation ID handling
    - _Requirements: 1.1, 1.2, 1.3, 1.5, 2.1, 2.2, 2.3_
  
  - [x] 8.2 Create PayloadController for payload retrieval endpoint
    - Implement GET /v1/partner-order-provider/orders/{transactionId}/payload endpoint
    - Return original payloads for Interface Exception Collector retry operations
    - Handle transaction ID validation and not found cases
    - Add proper security headers and access control
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 9. Add error handling and exception management
  - [x] 9.1 Create GlobalExceptionHandler for centralized error handling
    - Handle validation errors with detailed field-level messages
    - Handle duplicate external ID errors with HTTP 409 Conflict
    - Handle not found errors with HTTP 404 responses
    - Add correlation ID to all error responses
    - _Requirements: 1.3, 1.5, 5.4_
  
  - [x] 9.2 Create custom exceptions for business logic
    - Create DuplicateExternalIdException for duplicate detection
    - Create ValidationException for schema validation failures
    - Create PayloadNotFoundException for missing transaction IDs
    - Add proper error codes and messages for each exception type
    - _Requirements: 1.3, 1.5, 5.4_

- [ ] 10. Implement monitoring and observability
  - [ ] 10.1 Add Micrometer metrics for monitoring
    - Track order submission rates and success/failure counts
    - Monitor validation error rates and types
    - Track Kafka event publishing success rates
    - Add custom metrics for business KPIs
    - _Requirements: 3.4_
  
  - [ ] 10.2 Configure structured logging with correlation IDs
    - Add correlation ID to all log messages for traceability
    - Log order processing steps with appropriate log levels
    - Include transaction ID and external ID in relevant log messages
    - Configure JSON logging format for log aggregation
    - _Requirements: 3.4, 4.3_

- [ ] 11. Create comprehensive test suite
  - [ ] 11.1 Write unit tests for all service classes
    - Test PartnerOrderService with various validation scenarios
    - Test JsonSchemaValidationService with valid and invalid payloads
    - Test EventPublishingService with Kafka integration mocks
    - Test PayloadRetrievalService with database interactions
    - _Requirements: All requirements_
  
  - [ ] 11.2 Write integration tests for API endpoints
    - Test POST /v1/partner-order-provider/orders with end-to-end flow
    - Test GET /v1/partner-order-provider/orders/{transactionId}/payload endpoint
    - Test error scenarios: validation failures, duplicates, not found
    - Test retry scenarios with Interface Exception Collector headers
    - _Requirements: All requirements_
  
  - [ ] 11.3 Write contract tests for Kafka event schemas
    - Verify OrderReceived event schema compatibility
    - Verify OrderRejected event schema compatibility
    - Verify InvalidOrderEvent schema compatibility
    - Test event serialization and deserialization
    - _Requirements: 3.1, 3.2, 3.3_

- [ ] 12. Configure deployment and infrastructure
  - [ ] 12.1 Update Kubernetes configurations
    - Verify partner-order-service.yaml deployment configuration
    - Verify partner-order-postgres.yaml database configuration
    - Verify partner-order-migration-job.yaml database setup
    - Test health check endpoints and readiness probes
    - _Requirements: All requirements_
  
  - [ ] 12.2 Update Tiltfile for development workflow
    - Verify multi-service Tilt configuration works correctly
    - Test hot reload functionality for Partner Order Service
    - Verify port forwarding and service dependencies
    - Test selective service startup (individual vs both services)
    - _Requirements: All requirements_

- [ ] 13. Integration with Interface Exception Collector Service
  - [ ] 13.1 Update Interface Exception Collector configuration
    - Add Partner Order Service endpoint configuration
    - Configure service-specific retry parameters and timeouts
    - Add Partner Order Service health check integration
    - Update source service client factory for Partner Order Service
    - _Requirements: 2.1, 2.2, 2.3, 5.3_
  
  - [ ] 13.2 Create PartnerOrderServiceClient in Interface Exception Collector
    - Implement getOriginalPayload method for payload retrieval
    - Implement submitRetry method for retry operations
    - Add proper error handling and circuit breaker patterns
    - Include retry headers (X-Retry-Attempt, X-Original-Transaction-ID)
    - _Requirements: 2.1, 2.2, 2.3, 5.3_
  
  - [ ] 13.3 Test end-to-end integration between services
    - Test order submission → validation failure → InvalidOrderEvent → exception storage
    - Test order submission → OrderRejected event → exception storage → retry flow
    - Test payload retrieval → retry submission → success/failure handling
    - Verify correlation IDs and transaction IDs flow correctly through both services
    - _Requirements: All requirements_