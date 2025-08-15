# Partner Order Service - Requirements

## Introduction

The Partner Order Service is a microservice that provides external API endpoints for partners/customers to submit blood product orders to the BioPro order management system. The service validates incoming orders, generates unique transaction IDs, and publishes appropriate events to Kafka topics for downstream processing. This service serves as the retry target endpoint for the existing Interface Exception Collector Service, enabling automated recovery from failed order submissions.

## Requirements

### Requirement 1: Order Reception and Validation

**User Story:** As a partner organization, I want to submit blood product orders via REST API, so that I can integrate my systems with BioPro for seamless order processing.

#### Acceptance Criteria

1. WHEN a partner submits a POST request to `/v1/partner-order-provider/orders` THEN the system SHALL validate the request against the predefined JSON schema
2. WHEN the validation succeeds THEN the system SHALL return HTTP 202 Accepted with a unique transaction ID
3. WHEN the validation fails THEN the system SHALL return HTTP 400 Bad Request with detailed error messages
4. WHEN schema validation fails THEN the system SHALL publish an InvalidOrderEvent to the InvalidOrderEvent topic containing the entire request payload
5. WHEN a duplicate external ID is detected THEN the system SHALL return HTTP 409 Conflict

### Requirement 2: Retry Endpoint for Interface Exception Collector

**User Story:** As an Interface Exception Collector, I want to send retry requests to the Partner Order Service using original payloads, so that failed orders can be reprocessed through the same validation and publishing logic.

#### Acceptance Criteria

1. WHEN the Interface Exception Collector sends a retry request with original payload THEN the system SHALL process it using the same validation and publishing logic as new orders
2. WHEN a retry is successful THEN the system SHALL return the same response format as original submissions
3. WHEN a retry fails validation THEN the system SHALL generate the same InvalidOrderEvent as original failures
4. WHEN processing retries THEN the system SHALL accept correlation with the original transaction ID via headers

### Requirement 3: Event Publishing

**User Story:** As a BioPro system, I want the Partner Order Service to publish structured events, so that downstream services can process orders and handle exceptions appropriately.

#### Acceptance Criteria

1. WHEN an order is successfully validated THEN the system SHALL publish an `OrderReceived` event to the `OrderRecieved` topic
2. WHEN an order is successfully validated and returns HTTP 202 THEN the system SHALL also publish an `OrderRejected` event to the `OrderRejected` topic for Interface Exception Collector testing functionality
3. WHEN an order fails schema validation THEN the system SHALL publish an `InvalidOrderEvent` to the `InvalidOrderEvent` topic containing the complete request payload
4. WHEN publishing any event THEN the system SHALL include correlation ID and transaction ID for traceability

### Requirement 4: Transaction Management and Payload Storage

**User Story:** As a BioPro administrator, I want each order submission to have a unique transaction ID and stored payload, so that the Interface Exception Collector Service can retrieve original payloads for retry operations.

#### Acceptance Criteria

1. WHEN any order is received THEN the system SHALL generate a unique UUID transaction ID
2. WHEN responding to the submitter THEN the system SHALL include the transaction ID in the response
3. WHEN publishing events THEN the system SHALL include the transaction ID in all event payloads
4. WHEN storing order data THEN the system SHALL persist the complete original payload for retrieval by Interface Exception Collector
5. WHEN the Interface Exception Collector requests a payload THEN the system SHALL provide the original payload via GET endpoint

### Requirement 5: Order Storage and Payload Retrieval

**User Story:** As an Interface Exception Collector Service, I want to retrieve original order payloads from the Partner Order Service, so that I can resubmit failed orders with the exact original data.

#### Acceptance Criteria

1. WHEN any order is received THEN the system SHALL store the complete original payload exactly as received
2. WHEN storing order data THEN the system SHALL include timestamp and transaction metadata
3. WHEN the Interface Exception Collector requests a payload by transaction ID THEN the system SHALL return the original payload
4. WHEN a requested transaction ID does not exist THEN the system SHALL return HTTP 404 Not Found