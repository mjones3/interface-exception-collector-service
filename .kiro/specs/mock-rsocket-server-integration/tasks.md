# Implementation Plan

- [ ] 1. Add RSocket dependencies and database schema changes
  - Add Spring Boot RSocket starter dependency to interface-exception-collector pom.xml
  - Create V16__Add_order_data_fields.sql migration to add order_received, order_retrieval_attempted, order_retrieval_error, and order_retrieved_at columns
  - Update InterfaceException entity with new fields for order data storage
  - _Requirements: 1.3, 1.4, 2.1_

- [ ] 2. Create mock RSocket server client implementation
  - Implement MockRSocketOrderServiceClient extending BaseSourceServiceClient
  - Add RSocket configuration with connection management and timeout settings
  - Implement getOriginalPayload method using RSocket requester to call mock server
  - Add circuit breaker, retry, and timeout annotations for resilience
  - _Requirements: 1.1, 1.2, 6.2, 6.3_

- [ ] 3. Enhance order event processing with order data retrieval
  - Modify existing OrderRejectedEventProcessor to attempt order data retrieval
  - Add retrieveAndStoreOrderData method to fetch complete order data from mock server
  - Update exception creation to populate order_received field and retrieval metadata
  - Implement fallback handling when order retrieval fails
  - _Requirements: 3.1, 3.2, 3.3, 1.5_

- [ ] 4. Configure mock server container integration with Tilt
  - Create k8s/mock-rsocket-server.yaml deployment configuration
  - Add mock server resource to Tiltfile with port forwarding and dependencies
  - Configure volume mounts for mappings and response files
  - Add health checks and container lifecycle management
  - _Requirements: 4.1, 4.2, 4.4, 4.5_

- [ ] 5. Create mock server mapping and response files
  - Create mappings directory structure with order-success-mapping.json, order-not-found-mapping.json, and default-fallback-mapping.json
  - Create mock-responses directory with complete-order-with-items.json and error response files
  - Implement mapping files supporting routePathPattern matching for different externalId patterns
  - Add test scenarios for TEST-ORDER-1, TEST-ORD-2025-018, and error conditions
  - _Requirements: 2.1, 2.2, 2.3, 5.1, 5.2, 5.3_

- [ ] 6. Implement configuration management and feature flags
  - Add application properties for mock server configuration (host, port, timeout, circuit breaker settings)
  - Create SourceServiceClientConfiguration with conditional bean registration
  - Implement environment-specific configuration files (dev, test, prod)
  - Add feature flag support to enable/disable mock server vs production service
  - _Requirements: 6.1, 6.4, 6.5_

- [ ] 7. Add monitoring and health checks for mock server integration
  - Create MockRSocketServerHealthIndicator for health check endpoint
  - Add custom metrics for RSocket call tracking (success rate, duration, errors)
  - Implement structured logging with correlation IDs for mock server interactions
  - Add RSocketMetrics component for Prometheus metrics collection
  - _Requirements: 8.1, 8.2, 8.5, 7.4_

- [ ] 8. Create unit tests for RSocket client and order processing
  - Write MockRSocketOrderServiceClientTest with success and failure scenarios
  - Create tests for order data retrieval with timeout and circuit breaker behavior
  - Test enhanced OrderRejectedEventProcessor with mock server integration
  - Add tests for fallback handling when mock server is unavailable
  - _Requirements: 1.5, 6.2, 7.1, 7.2_

- [ ] 9. Create integration tests with TestContainers
  - Implement MockRSocketServerIntegrationTest using TestContainers for mock server
  - Create end-to-end test for complete OrderRejected event processing flow
  - Test order data retrieval and storage in database with real mock server container
  - Verify circuit breaker and retry behavior under failure conditions
  - _Requirements: 7.1, 7.2, 7.3, 8.4_

- [ ] 10. Add configuration validation and error handling
  - Implement configuration validation for RSocket connection parameters
  - Add comprehensive error handling for RSocket connection failures
  - Create fallback mechanisms for when mock server is unavailable during startup
  - Add configuration documentation and example files
  - _Requirements: 6.2, 6.3, 8.3_

- [ ] 11. Update API responses to include order data
  - Modify ExceptionResponse DTOs to include orderReceived field when available
  - Update GET /api/v1/exceptions/{transactionId} endpoint to return complete order data
  - Add includeOrderData query parameter for controlling order data inclusion in responses
  - Update API documentation and OpenAPI specifications
  - _Requirements: 5.5, 7.1_

- [ ] 12. Create comprehensive documentation and examples
  - Document mock server setup and configuration in README
  - Create example mapping files and response templates for different test scenarios
  - Add troubleshooting guide for common mock server integration issues
  - Document environment-specific configuration and deployment procedures
  - _Requirements: 4.3, 5.4, 6.1, 8.5_