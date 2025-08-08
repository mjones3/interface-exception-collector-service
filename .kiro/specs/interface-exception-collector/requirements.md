# Interface Exception Collector Service - Requirements

## User Stories

### Epic 1: Exception Event Collection

#### US-001: Capture Order Exception Events
**As a** BioPro operations team member  
**I want** the system to automatically capture order exception events from Kafka  
**So that** I can monitor order processing failures in real-time

**Acceptance Criteria:**
- WHEN an OrderRejected event is published to the OrderRejected Kafka topic THE SYSTEM SHALL consume the event within 100ms
- WHEN an OrderRejected event is consumed THE SYSTEM SHALL extract transactionId, externalId, operation, rejectedReason, customerId, locationCode, and orderItems from the payload
- WHEN an OrderRejected event is processed THE SYSTEM SHALL store the exception in the database with interfaceType "ORDER" and status "NEW"
- WHEN an OrderRejected event is processed THE SYSTEM SHALL classify the exception category based on rejectedReason (BUSINESS_RULE for "Order already exists", VALIDATION for data issues)
- WHEN an OrderCancelled event is published to the OrderCancelled Kafka topic THE SYSTEM SHALL consume the event within 100ms
- WHEN an OrderCancelled event is consumed THE SYSTEM SHALL extract transactionId, externalId, cancelReason, cancelledBy, and customerId from the payload
- WHEN an OrderCancelled event is processed THE SYSTEM SHALL store the exception in the database with interfaceType "ORDER" and appropriate category
- WHEN an exception is successfully stored THE SYSTEM SHALL publish an ExceptionCaptured event to the ExceptionCaptured Kafka topic

#### US-002: Capture Collection Exception Events
**As a** BioPro operations team member  
**I want** the system to automatically capture collection exception events from Kafka  
**So that** I can monitor collection processing failures in real-time

**Acceptance Criteria:**
- WHEN a CollectionRejected event is published to the CollectionRejected Kafka topic THE SYSTEM SHALL consume the event within 100ms
- WHEN a CollectionRejected event is consumed THE SYSTEM SHALL extract transactionId, collectionId, operation, rejectedReason, donorId, and locationCode from the payload
- WHEN a CollectionRejected event is processed THE SYSTEM SHALL store the exception in the database with interfaceType "COLLECTION"
- WHEN a collection exception is stored THE SYSTEM SHALL classify operation as CREATE_COLLECTION or MODIFY_COLLECTION based on the event payload
- WHEN a collection exception is stored THE SYSTEM SHALL publish an ExceptionCaptured event with interfaceType "COLLECTION"

#### US-003: Capture Distribution Exception Events
**As a** BioPro operations team member  
**I want** the system to automatically capture distribution exception events from Kafka  
**So that** I can monitor distribution processing failures in real-time

**Acceptance Criteria:**
- WHEN a DistributionFailed event is published to the DistributionFailed Kafka topic THE SYSTEM SHALL consume the event within 100ms
- WHEN a DistributionFailed event is consumed THE SYSTEM SHALL extract transactionId, distributionId, operation, failureReason, customerId, and destinationLocation from the payload
- WHEN a DistributionFailed event is processed THE SYSTEM SHALL store the exception in the database with interfaceType "DISTRIBUTION"
- WHEN a distribution exception is stored THE SYSTEM SHALL set operation as CREATE_DISTRIBUTION or MODIFY_DISTRIBUTION based on the event payload
- WHEN a distribution exception is stored THE SYSTEM SHALL publish an ExceptionCaptured event with interfaceType "DISTRIBUTION"

#### US-004: Handle Validation Errors
**As a** BioPro operations team member  
**I want** the system to capture schema validation errors from all interface services  
**So that** I can identify data quality issues across interfaces

**Acceptance Criteria:**
- WHEN a ValidationError event is published to the ValidationError Kafka topic THE SYSTEM SHALL consume the event within 100ms
- WHEN a ValidationError event is consumed THE SYSTEM SHALL extract transactionId, interfaceType, and validationErrors array from the payload
- WHEN a validation error is processed THE SYSTEM SHALL store the exception with category "VALIDATION" and severity "MEDIUM"
- WHEN validationErrors contains multiple field errors THE SYSTEM SHALL aggregate them into a single exception record with combined error message
- WHEN a validation exception is stored THE SYSTEM SHALL set interfaceType to ORDER, COLLECTION, or DISTRIBUTION based on the event payload

### Epic 2: Exception Data Management and Lifecycle

#### US-005: Store Exception Metadata
**As a** BioPro operations team member  
**I want** exception data to be stored with consistent metadata  
**So that** I can effectively search and filter exceptions

**Acceptance Criteria:**
- WHEN an exception event is processed THE SYSTEM SHALL store id, transactionId, interfaceType, exceptionReason, operation, externalId, status, severity, category, retryable, customerId, and timestamp fields
- WHEN an exception is stored THE SYSTEM SHALL assign a unique auto-generated id as the primary key
- WHEN an exception is stored THE SYSTEM SHALL set processedAt to current timestamp
- WHEN an exception is stored THE SYSTEM SHALL initialize retryCount to 0 and status to "NEW"
- WHEN duplicate exception events are received for the same transactionId THE SYSTEM SHALL update the existing record rather than creating duplicates

#### US-006: Manage Exception Status Lifecycle
**As a** BioPro operations team member  
**I want** exceptions to have proper status tracking through the defined status values  
**So that** I can monitor resolution progress

**Acceptance Criteria:**
- WHEN an exception is first captured THE SYSTEM SHALL set status to "NEW"
- WHEN an exception is acknowledged via PUT /api/v1/exceptions/{transactionId}/acknowledge THE SYSTEM SHALL update status to "ACKNOWLEDGED"
- WHEN a retry operation completes successfully THE SYSTEM SHALL update status to "RETRIED_SUCCESS" and set resolvedAt timestamp
- WHEN a retry operation fails THE SYSTEM SHALL update status to "RETRIED_FAILED" and increment retryCount
- WHEN an exception requires escalation THE SYSTEM SHALL update status to "ESCALATED"
- WHEN an exception is marked as resolved THE SYSTEM SHALL update status to "RESOLVED"
- WHEN an exception investigation is complete THE SYSTEM SHALL update status to "CLOSED"
- WHEN status is updated THE SYSTEM SHALL publish appropriate events (ExceptionRetryCompleted, ExceptionResolved)

### Epic 3: Exception Retrieval and Search

#### US-007: List Exceptions with Filtering
**As a** BioPro operations team member  
**I want** to retrieve exceptions with filtering capabilities via GET /api/v1/exceptions  
**So that** I can efficiently find relevant exceptions

**Acceptance Criteria:**
- WHEN a GET request is made to /api/v1/exceptions THE SYSTEM SHALL return a paginated list of exceptions ordered by timestamp DESC
- WHEN interfaceType parameter is provided with values ORDER, COLLECTION, DISTRIBUTION, or RECRUITMENT THE SYSTEM SHALL filter results to show only exceptions from that interface
- WHEN status parameter is provided with values NEW, ACKNOWLEDGED, RETRIED_SUCCESS, RETRIED_FAILED, ESCALATED, RESOLVED, or CLOSED THE SYSTEM SHALL filter results accordingly
- WHEN severity parameter is provided with values LOW, MEDIUM, HIGH, or CRITICAL THE SYSTEM SHALL filter results to show only exceptions with that severity
- WHEN customerId parameter is provided THE SYSTEM SHALL filter results to show only exceptions for that customer
- WHEN fromDate parameter is provided THE SYSTEM SHALL filter results to show only exceptions after that date
- WHEN toDate parameter is provided THE SYSTEM SHALL filter results to show only exceptions before that date
- WHEN page and size parameters are provided THE SYSTEM SHALL return paginated results with specified page number (0-based) and page size (max 100)
- WHEN sort parameter is provided THE SYSTEM SHALL sort results by the specified field and direction (default "timestamp,desc")
- WHEN multiple filters are provided THE SYSTEM SHALL apply all filters using AND logic
- WHEN no results match the filters THE SYSTEM SHALL return an empty content array with appropriate pagination metadata

#### US-008: Get Exception Details with Original Payload
**As a** BioPro operations team member  
**I want** to retrieve detailed exception information including original payload via GET /api/v1/exceptions/{transactionId}  
**So that** I can understand the root cause and retry if needed

**Acceptance Criteria:**
- WHEN a GET request is made to /api/v1/exceptions/{transactionId} THE SYSTEM SHALL return detailed exception information including all InterfaceException fields
- WHEN retrieving exception details with includePayload=true THE SYSTEM SHALL fetch the original payload from the source interface service
- WHEN the original payload is successfully retrieved THE SYSTEM SHALL include it in the originalPayload field of the response
- WHEN the source service is unavailable THE SYSTEM SHALL return exception details with originalPayload as null and log the retrieval failure
- WHEN retrieving exception details THE SYSTEM SHALL include retryHistory array with all retry attempts if any have been made
- WHEN retrieving exception details THE SYSTEM SHALL include relatedExceptions array with other exceptions for the same customer/order
- WHEN the requested transactionId does not exist THE SYSTEM SHALL return HTTP 404 with ErrorResponse schema
- WHEN includePayload=false THE SYSTEM SHALL return exception details without attempting to fetch the original payload

#### US-009: Search Exceptions by Text
**As a** BioPro operations team member  
**I want** to search exceptions by text content via GET /api/v1/exceptions/search  
**So that** I can find exceptions related to specific error messages or external IDs

**Acceptance Criteria:**
- WHEN a GET request is made to /api/v1/exceptions/search with a query parameter THE SYSTEM SHALL search across the specified fields
- WHEN fields parameter includes "exceptionReason" THE SYSTEM SHALL search within the exceptionReason field
- WHEN fields parameter includes "externalId" THE SYSTEM SHALL search within the externalId field  
- WHEN fields parameter includes "operation" THE SYSTEM SHALL search within the operation field
- WHEN fields parameter is not specified THE SYSTEM SHALL default to searching only exceptionReason field
- WHEN performing text search THE SYSTEM SHALL use case-insensitive partial matching
- WHEN search results are returned THE SYSTEM SHALL maintain pagination with page and size parameters
- WHEN search parameter is empty or missing THE SYSTEM SHALL return HTTP 400 with appropriate error message

#### US-010: Get Exception Summary Statistics
**As a** BioPro operations team member  
**I want** to access aggregated exception statistics via GET /api/v1/exceptions/summary  
**So that** I can monitor trends and create dashboard widgets

**Acceptance Criteria:**
- WHEN a GET request is made to /api/v1/exceptions/summary THE SYSTEM SHALL return aggregated statistics for the specified time range
- WHEN timeRange parameter is "today" THE SYSTEM SHALL return statistics for exceptions from the current day
- WHEN timeRange parameter is "week" THE SYSTEM SHALL return statistics for exceptions from the last 7 days
- WHEN timeRange parameter is "month" THE SYSTEM SHALL return statistics for exceptions from the last 30 days
- WHEN timeRange parameter is "quarter" THE SYSTEM SHALL return statistics for exceptions from the last 90 days
- WHEN groupBy parameter is "interfaceType" THE SYSTEM SHALL group statistics by ORDER, COLLECTION, DISTRIBUTION, RECRUITMENT
- WHEN groupBy parameter is "severity" THE SYSTEM SHALL group statistics by LOW, MEDIUM, HIGH, CRITICAL
- WHEN groupBy parameter is "status" THE SYSTEM SHALL group statistics by all defined status values
- WHEN returning summary THE SYSTEM SHALL include totalExceptions, byInterfaceType, bySeverity, byStatus, and trends with daily counts

### Epic 4: Exception Retry Functionality

#### US-011: Retry Failed Requests
**As a** BioPro operations team member  
**I want** to retry failed interface requests via POST /api/v1/exceptions/{transactionId}/retry  
**So that** I can recover from transient failures without customer intervention

**Acceptance Criteria:**
- WHEN a POST request is made to /api/v1/exceptions/{transactionId}/retry THE SYSTEM SHALL validate that the exception exists and has retryable=true
- WHEN initiating a retry with reason and priority in the RetryRequest THE SYSTEM SHALL retrieve the original payload from the source interface service
- WHEN the original payload is retrieved THE SYSTEM SHALL resubmit it to the appropriate interface service endpoint based on interfaceType and operation
- WHEN the retry request is successful THE SYSTEM SHALL update the exception status to "RETRIED_SUCCESS" and set resolvedAt timestamp
- WHEN the retry request fails THE SYSTEM SHALL update the exception status to "RETRIED_FAILED" and increment retryCount
- WHEN a retry is initiated THE SYSTEM SHALL return HTTP 202 with RetryResponse containing retryId, status, message, and estimatedCompletionTime
- WHEN a retry is completed THE SYSTEM SHALL create a RetryAttempt record with attemptNumber, status, initiatedBy, initiatedAt, completedAt, and result details
- WHEN a retry is completed THE SYSTEM SHALL publish an ExceptionRetryCompleted event with attemptNumber, retryStatus, retryResult, initiatedBy, and completedAt
- WHEN an exception has retryable=false THE SYSTEM SHALL return HTTP 409 with appropriate error message
- WHEN the requested transactionId does not exist THE SYSTEM SHALL return HTTP 404 with ErrorResponse
- WHEN notifyOnCompletion=true in the retry request THE SYSTEM SHALL send notification when retry completes

#### US-012: Track Retry History
**As a** BioPro operations team member  
**I want** to see the complete retry history for each exception  
**So that** I can understand what recovery attempts have been made

**Acceptance Criteria:**
- WHEN a retry attempt is initiated THE SYSTEM SHALL create a RetryAttempt record with attemptNumber (sequential), status "PENDING", and initiatedBy
- WHEN a retry completes THE SYSTEM SHALL update the RetryAttempt record with status "SUCCESS" or "FAILED", completedAt timestamp, and result object
- WHEN storing retry results THE SYSTEM SHALL include success boolean, message, responseCode, and errorDetails in the result object
- WHEN retrieving exception details THE SYSTEM SHALL include all retryHistory records ordered by attemptNumber
- WHEN displaying retry history THE SYSTEM SHALL show attemptNumber, status, initiatedBy, initiatedAt, completedAt, and result details

### Epic 5: Exception Acknowledgment and Resolution

#### US-013: Acknowledge Exceptions
**As a** BioPro operations team member  
**I want** to acknowledge exceptions via PUT /api/v1/exceptions/{transactionId}/acknowledge  
**So that** I can track which exceptions have been reviewed by the operations team

**Acceptance Criteria:**
- WHEN a PUT request is made to /api/v1/exceptions/{transactionId}/acknowledge with AcknowledgeRequest THE SYSTEM SHALL update the exception status to "ACKNOWLEDGED"
- WHEN acknowledging an exception THE SYSTEM SHALL extract acknowledgedBy and notes from the request body
- WHEN the acknowledgment is processed THE SYSTEM SHALL set acknowledgedAt to current timestamp
- WHEN acknowledgment is successful THE SYSTEM SHALL return HTTP 200 with AcknowledgeResponse containing status, acknowledgedAt, and acknowledgedBy
- WHEN the requested transactionId does not exist THE SYSTEM SHALL return HTTP 404 with ErrorResponse
- WHEN an exception is acknowledged THE SYSTEM SHALL maintain the acknowledgment information for audit purposes

#### US-014: Manage Exception Resolution
**As a** BioPro operations team member  
**I want** exceptions to be marked as resolved when appropriate  
**So that** I can track which issues have been addressed

**Acceptance Criteria:**
- WHEN a retry operation succeeds THE SYSTEM SHALL automatically update exception status to "RESOLVED" and set resolvedAt timestamp
- WHEN an exception is manually resolved THE SYSTEM SHALL allow updating status to "RESOLVED" with resolvedBy information
- WHEN an exception reaches "RESOLVED" status THE SYSTEM SHALL publish an ExceptionResolved event to the ExceptionResolved Kafka topic
- WHEN publishing ExceptionResolved event THE SYSTEM SHALL include exceptionId, transactionId, resolutionMethod, resolvedBy, resolvedAt, totalRetryAttempts, and resolutionNotes
- WHEN resolutionMethod is "RETRY_SUCCESS" THE SYSTEM SHALL set it for automatic resolution via successful retry
- WHEN resolutionMethod is "MANUAL_RESOLUTION" THE SYSTEM SHALL set it for manual operations team resolution
- WHEN resolutionMethod is "CUSTOMER_RESOLVED" THE SYSTEM SHALL set it when customer fixes the issue on their end
- WHEN resolutionMethod is "AUTOMATED" THE SYSTEM SHALL set it for system-automated resolution

### Epic 6: Critical Exception Alerting

#### US-015: Generate Critical Exception Alerts
**As a** BioPro operations team member  
**I want** the system to automatically generate alerts for critical exceptions  
**So that** urgent issues receive immediate attention

**Acceptance Criteria:**
- WHEN an exception is captured with severity "CRITICAL" THE SYSTEM SHALL automatically publish a CriticalExceptionAlert event to the CriticalExceptionAlert Kafka topic
- WHEN publishing critical alerts THE SYSTEM SHALL include exceptionId, transactionId, alertLevel, alertReason, interfaceType, exceptionReason, customerId, and escalationTeam
- WHEN alertLevel is "CRITICAL" THE SYSTEM SHALL set requiresImmediateAction to true
- WHEN alertLevel is "EMERGENCY" THE SYSTEM SHALL set escalationTeam to "MANAGEMENT"
- WHEN alertReason is "CRITICAL_SEVERITY" THE SYSTEM SHALL trigger for any exception with severity CRITICAL
- WHEN alertReason is "MULTIPLE_RETRIES_FAILED" THE SYSTEM SHALL trigger when retryCount exceeds 3 for any exception
- WHEN alertReason is "SYSTEM_ERROR" THE SYSTEM SHALL trigger for exceptions with category "SYSTEM_ERROR"
- WHEN alertReason is "CUSTOMER_IMPACT" THE SYSTEM SHALL trigger when customersAffected count is high
- WHEN generating alerts THE SYSTEM SHALL include estimatedImpact assessment (LOW, MEDIUM, HIGH, SEVERE)

### Epic 7: Event Publishing and Integration

#### US-016: Publish Exception Lifecycle Events
**As a** downstream system consuming exception events  
**I want** to receive notifications about exception lifecycle changes  
**So that** I can trigger appropriate workflows and notifications

**Acceptance Criteria:**
- WHEN an exception is successfully captured and stored THE SYSTEM SHALL publish an ExceptionCaptured event to the ExceptionCaptured Kafka topic
- WHEN publishing ExceptionCaptured event THE SYSTEM SHALL include eventId, eventType "ExceptionCaptured", eventVersion, occurredOn, source "exception-collector-service", and correlationId
- WHEN publishing ExceptionCaptured event THE SYSTEM SHALL include payload with exceptionId, transactionId, interfaceType, severity, category, exceptionReason, customerId, and retryable fields
- WHEN an exception retry is completed THE SYSTEM SHALL publish an ExceptionRetryCompleted event to the ExceptionRetryCompleted Kafka topic
- WHEN publishing ExceptionRetryCompleted event THE SYSTEM SHALL include exceptionId, transactionId, attemptNumber, retryStatus (SUCCESS or FAILED), retryResult, initiatedBy, and completedAt
- WHEN an exception reaches resolved status THE SYSTEM SHALL publish an ExceptionResolved event to the ExceptionResolved Kafka topic
- WHEN event publishing fails THE SYSTEM SHALL log the failure but continue with core exception processing

#### US-017: Maintain Event Correlation
**As a** BioPro operations team member  
**I want** all events to maintain proper correlation tracking  
**So that** I can trace the complete lifecycle of exceptions

**Acceptance Criteria:**
- WHEN processing any inbound exception event THE SYSTEM SHALL preserve the original correlationId from the source event headers
- WHEN publishing outbound events THE SYSTEM SHALL include the original correlationId in the event headers for traceability
- WHEN publishing outbound events THE SYSTEM SHALL generate a new unique eventId while maintaining correlationId consistency
- WHEN multiple events relate to the same transaction THE SYSTEM SHALL use consistent correlationId across all related events
- WHEN event headers include causationId THE SYSTEM SHALL propagate it to maintain the event causation chain

### Epic 8: Error Handling and Resilience

#### US-018: Handle Kafka Processing Failures
**As a** BioPro operations team member  
**I want** the system to gracefully handle Kafka processing failures  
**So that** no exception events are lost

**Acceptance Criteria:**
- WHEN a Kafka message cannot be processed due to deserialization errors THE SYSTEM SHALL log the error details and continue processing other messages
- WHEN database connection is temporarily unavailable THE SYSTEM SHALL retry processing with exponential backoff up to 5 attempts
- WHEN maximum retry attempts are reached for database operations THE SYSTEM SHALL log the failure and alert operations team
- WHEN Kafka consumer group rebalancing occurs THE SYSTEM SHALL continue processing from the last committed offset without data loss
- WHEN processing a malformed event payload THE SYSTEM SHALL log validation errors with event details and skip to the next message
- WHEN Kafka broker connectivity is lost THE SYSTEM SHALL attempt to reconnect automatically and resume processing

#### US-019: Handle External Service Dependencies
**As a** BioPro operations team member  
**I want** the system to handle external service failures gracefully  
**So that** core exception collection continues even when dependencies are unavailable

**Acceptance Criteria:**
- WHEN a source interface service is unavailable for payload retrieval THE SYSTEM SHALL return exception details with originalPayload as null
- WHEN retry requests fail due to target service unavailability THE SYSTEM SHALL update retry history with appropriate error details
- WHEN external service calls timeout after 5 seconds THE SYSTEM SHALL return appropriate error responses
- WHEN external services return HTTP 5xx errors THE SYSTEM SHALL log the errors and continue processing
- WHEN circuit breaker thresholds are exceeded THE SYSTEM SHALL temporarily stop calling failing services and return cached responses where possible