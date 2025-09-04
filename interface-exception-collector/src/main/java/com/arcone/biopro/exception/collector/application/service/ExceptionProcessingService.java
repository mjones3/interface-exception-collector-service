package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.event.inbound.CollectionRejectedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.DistributionFailedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderCancelledEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent;
import com.arcone.biopro.exception.collector.domain.event.inbound.ValidationErrorEvent;
import com.arcone.biopro.exception.collector.infrastructure.client.SourceServiceClient;
import com.arcone.biopro.exception.collector.infrastructure.client.SourceServiceClientRegistry;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.config.LoggingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service responsible for processing exception events and managing exception
 * lifecycle.
 * Handles business logic for exception categorization, severity assignment,
 * duplicate detection,
 * and status management according to requirements US-001 through US-006.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExceptionProcessingService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExceptionProcessingService.class);

    private final InterfaceExceptionRepository exceptionRepository;
    private final CacheEvictionService cacheEvictionService;
    private final MetricsService metricsService;
    private final SourceServiceClientRegistry clientRegistry;

    /**
     * Process an OrderRejected event and create or update an interface exception.
     * Implements requirements US-001 for order exception capture and 3.1, 3.2, 3.3, 1.5 for order data retrieval.
     *
     * @param event the OrderRejected event to process
     * @return the processed InterfaceException entity
     */
    @Transactional
    public InterfaceException processOrderRejectedEvent(OrderRejectedEvent event) {
        Instant start = Instant.now();
        String transactionId = event.getPayload().getTransactionId();

        // Set logging context for structured logging
        LoggingConfig.LoggingContext.setTransactionId(transactionId);
        LoggingConfig.LoggingContext.setInterfaceType(InterfaceType.ORDER.name());

        log.info("Processing OrderRejected event for transaction: {} with externalId: {}", 
                transactionId, event.getPayload().getExternalId());

        try {
            // Check for duplicate using transaction ID (US-005)
            Optional<InterfaceException> existingException = exceptionRepository
                    .findByTransactionId(transactionId);

            if (existingException.isPresent()) {
                log.info("Updating existing exception for transaction: {}", transactionId);
                return updateExistingException(existingException.get(), event);
            }

            // Create new exception
            ExceptionSeverity severity = assignSeverity(InterfaceType.ORDER, event.getPayload().getRejectedReason());
            InterfaceException exception = InterfaceException.builder()
                    .transactionId(transactionId)
                    .interfaceType(InterfaceType.ORDER)
                    .exceptionReason(event.getPayload().getRejectedReason())
                    .operation(event.getPayload().getOperation().name())
                    .externalId(event.getPayload().getExternalId())
                    .status(ExceptionStatus.NEW)
                    .category(categorizeOrderException(event.getPayload().getRejectedReason()))
                    .severity(severity)
                    .retryable(determineRetryability(event.getPayload().getRejectedReason()))
                    .customerId(event.getPayload().getCustomerId())
                    .locationCode(event.getPayload().getLocationCode())
                    .timestamp(event.getOccurredOn())
                    .processedAt(OffsetDateTime.now())
                    .retryCount(0)
                    .orderRetrievalAttempted(false)
                    .build();

            // Attempt to retrieve order data from mock server
            retrieveAndStoreOrderData(exception);

            InterfaceException savedException = exceptionRepository.save(exception);
            log.info("Created new exception with ID: {} for transaction: {}, order data retrieved: {}",
                    savedException.getId(), savedException.getTransactionId(), 
                    savedException.getOrderReceived() != null);

            // Record metrics for monitoring
            Duration processingTime = Duration.between(start, Instant.now());
            metricsService.recordExceptionProcessed(InterfaceType.ORDER, severity);
            metricsService.recordExceptionProcessingTime(processingTime, InterfaceType.ORDER);

            // Evict caches to ensure new exception is reflected in queries
            cacheEvictionService.evictCachesOnExceptionCreation(savedException);

            return savedException;
        } finally {
            // Clear logging context
            LoggingConfig.LoggingContext.clearKeys("transactionId", "interfaceType");
        }
    }

    /**
     * Process an OrderCancelled event and create or update an interface exception.
     * Implements requirements US-001 for order exception capture.
     *
     * @param event the OrderCancelled event to process
     * @return the processed InterfaceException entity
     */
    @Transactional
    public InterfaceException processOrderCancelledEvent(OrderCancelledEvent event) {
        Instant start = Instant.now();
        String transactionId = event.getPayload().getTransactionId();

        // Set logging context for structured logging
        LoggingConfig.LoggingContext.setTransactionId(transactionId);
        LoggingConfig.LoggingContext.setInterfaceType(InterfaceType.ORDER.name());

        log.info("Processing OrderCancelled event for transaction: {}", transactionId);

        try {
            // Check for duplicate using transaction ID (US-005)
            Optional<InterfaceException> existingException = exceptionRepository
                    .findByTransactionId(transactionId);

            if (existingException.isPresent()) {
                log.info("Updating existing exception for transaction: {}", transactionId);
                return updateExistingException(existingException.get(), event);
            }

            // Create new exception
            ExceptionSeverity severity = assignSeverity(InterfaceType.ORDER, event.getPayload().getCancelReason());
            InterfaceException exception = InterfaceException.builder()
                    .transactionId(transactionId)
                    .interfaceType(InterfaceType.ORDER)
                    .exceptionReason(event.getPayload().getCancelReason())
                    .operation("CANCEL_ORDER")
                    .externalId(event.getPayload().getExternalId())
                    .status(ExceptionStatus.NEW)
                    .category(categorizeOrderException(event.getPayload().getCancelReason()))
                    .severity(severity)
                    .retryable(determineRetryability(event.getPayload().getCancelReason()))
                    .customerId(event.getPayload().getCustomerId())
                    .timestamp(event.getOccurredOn())
                    .processedAt(OffsetDateTime.now())
                    .retryCount(0)
                    .build();

            InterfaceException savedException = exceptionRepository.save(exception);
            log.info("Created new exception with ID: {} for transaction: {}",
                    savedException.getId(), savedException.getTransactionId());

            // Record metrics for monitoring
            Duration processingTime = Duration.between(start, Instant.now());
            metricsService.recordExceptionProcessed(InterfaceType.ORDER, severity);
            metricsService.recordExceptionProcessingTime(processingTime, InterfaceType.ORDER);

            return savedException;
        } finally {
            // Clear logging context
            LoggingConfig.LoggingContext.clearKeys("transactionId", "interfaceType");
        }
    }

    /**
     * Process a CollectionRejected event and create or update an interface
     * exception.
     * Implements requirements US-002 for collection exception capture.
     *
     * @param event the CollectionRejected event to process
     * @return the processed InterfaceException entity
     */
    @Transactional
    public InterfaceException processCollectionRejectedEvent(CollectionRejectedEvent event) {
        Instant start = Instant.now();
        String transactionId = event.getPayload().getTransactionId();

        // Set logging context for structured logging
        LoggingConfig.LoggingContext.setTransactionId(transactionId);
        LoggingConfig.LoggingContext.setInterfaceType(InterfaceType.COLLECTION.name());

        log.info("Processing CollectionRejected event for transaction: {}", transactionId);

        try {
            // Check for duplicate using transaction ID (US-005)
            Optional<InterfaceException> existingException = exceptionRepository
                    .findByTransactionId(transactionId);

            if (existingException.isPresent()) {
                log.info("Updating existing exception for transaction: {}", transactionId);
                return updateExistingException(existingException.get(), event);
            }

            // Create new exception
            ExceptionSeverity severity = assignSeverity(InterfaceType.COLLECTION,
                    event.getPayload().getRejectedReason());
            InterfaceException exception = InterfaceException.builder()
                    .transactionId(transactionId)
                    .interfaceType(InterfaceType.COLLECTION)
                    .exceptionReason(event.getPayload().getRejectedReason())
                    .operation(event.getPayload().getOperation())
                    .externalId(event.getPayload().getCollectionId())
                    .status(ExceptionStatus.NEW)
                    .category(categorizeCollectionException(event.getPayload().getRejectedReason()))
                    .severity(severity)
                    .retryable(determineRetryability(event.getPayload().getRejectedReason()))
                    .customerId(event.getPayload().getDonorId()) // Using donorId as customerId for collections
                    .locationCode(event.getPayload().getLocationCode())
                    .timestamp(event.getOccurredOn())
                    .processedAt(OffsetDateTime.now())
                    .retryCount(0)
                    .build();

            InterfaceException savedException = exceptionRepository.save(exception);
            log.info("Created new exception with ID: {} for transaction: {}",
                    savedException.getId(), savedException.getTransactionId());

            // Record metrics for monitoring
            Duration processingTime = Duration.between(start, Instant.now());
            metricsService.recordExceptionProcessed(InterfaceType.COLLECTION, severity);
            metricsService.recordExceptionProcessingTime(processingTime, InterfaceType.COLLECTION);

            return savedException;
        } finally {
            // Clear logging context
            LoggingConfig.LoggingContext.clearKeys("transactionId", "interfaceType");
        }
    }

    /**
     * Process a DistributionFailed event and create or update an interface
     * exception.
     * Implements requirements US-003 for distribution exception capture.
     *
     * @param event the DistributionFailed event to process
     * @return the processed InterfaceException entity
     */
    @Transactional
    public InterfaceException processDistributionFailedEvent(DistributionFailedEvent event) {
        Instant start = Instant.now();
        String transactionId = event.getPayload().getTransactionId();

        // Set logging context for structured logging
        LoggingConfig.LoggingContext.setTransactionId(transactionId);
        LoggingConfig.LoggingContext.setInterfaceType(InterfaceType.DISTRIBUTION.name());

        log.info("Processing DistributionFailed event for transaction: {}", transactionId);

        try {
            // Check for duplicate using transaction ID (US-005)
            Optional<InterfaceException> existingException = exceptionRepository
                    .findByTransactionId(transactionId);

            if (existingException.isPresent()) {
                log.info("Updating existing exception for transaction: {}", transactionId);
                return updateExistingException(existingException.get(), event);
            }

            // Create new exception
            ExceptionSeverity severity = assignSeverity(InterfaceType.DISTRIBUTION,
                    event.getPayload().getFailureReason());
            InterfaceException exception = InterfaceException.builder()
                    .transactionId(transactionId)
                    .interfaceType(InterfaceType.DISTRIBUTION)
                    .exceptionReason(event.getPayload().getFailureReason())
                    .operation(event.getPayload().getOperation())
                    .externalId(event.getPayload().getDistributionId())
                    .status(ExceptionStatus.NEW)
                    .category(categorizeDistributionException(event.getPayload().getFailureReason()))
                    .severity(severity)
                    .retryable(determineRetryability(event.getPayload().getFailureReason()))
                    .customerId(event.getPayload().getCustomerId())
                    .locationCode(event.getPayload().getDestinationLocation())
                    .timestamp(event.getOccurredOn())
                    .processedAt(OffsetDateTime.now())
                    .retryCount(0)
                    .build();

            InterfaceException savedException = exceptionRepository.save(exception);
            log.info("Created new exception with ID: {} for transaction: {}",
                    savedException.getId(), savedException.getTransactionId());

            // Record metrics for monitoring
            Duration processingTime = Duration.between(start, Instant.now());
            metricsService.recordExceptionProcessed(InterfaceType.DISTRIBUTION, severity);
            metricsService.recordExceptionProcessingTime(processingTime, InterfaceType.DISTRIBUTION);

            return savedException;
        } finally {
            // Clear logging context
            LoggingConfig.LoggingContext.clearKeys("transactionId", "interfaceType");
        }
    }

    /**
     * Process a ValidationError event and create or update an interface exception.
     * Implements requirements US-004 for validation error capture.
     *
     * @param event the ValidationError event to process
     * @return the processed InterfaceException entity
     */
    @Transactional
    public InterfaceException processValidationErrorEvent(ValidationErrorEvent event) {
        Instant start = Instant.now();
        String transactionId = event.getPayload().getTransactionId();
        InterfaceType interfaceType = InterfaceType.valueOf(event.getPayload().getInterfaceType());

        // Set logging context for structured logging
        LoggingConfig.LoggingContext.setTransactionId(transactionId);
        LoggingConfig.LoggingContext.setInterfaceType(interfaceType.name());

        log.info("Processing ValidationError event for transaction: {}", transactionId);

        try {
            // Check for duplicate using transaction ID (US-005)
            Optional<InterfaceException> existingException = exceptionRepository
                    .findByTransactionId(transactionId);

            if (existingException.isPresent()) {
                log.info("Updating existing exception for transaction: {}", transactionId);
                return updateExistingException(existingException.get(), event);
            }

            // Aggregate validation errors into a single exception message
            String aggregatedErrorMessage = event.getPayload().getValidationErrors().stream()
                    .map(error -> String.format("Field '%s': %s", error.getField(), error.getMessage()))
                    .collect(Collectors.joining("; "));

            // Create new exception
            InterfaceException exception = InterfaceException.builder()
                    .transactionId(transactionId)
                    .interfaceType(interfaceType)
                    .exceptionReason(aggregatedErrorMessage)
                    .operation("VALIDATION")
                    .status(ExceptionStatus.NEW)
                    .category(ExceptionCategory.VALIDATION)
                    .severity(ExceptionSeverity.MEDIUM)
                    .retryable(true) // Validation errors are typically retryable after data correction
                    .timestamp(event.getOccurredOn())
                    .processedAt(OffsetDateTime.now())
                    .retryCount(0)
                    .build();

            InterfaceException savedException = exceptionRepository.save(exception);
            log.info("Created new validation exception with ID: {} for transaction: {}",
                    savedException.getId(), savedException.getTransactionId());

            // Record metrics for monitoring
            Duration processingTime = Duration.between(start, Instant.now());
            metricsService.recordExceptionProcessed(interfaceType, ExceptionSeverity.MEDIUM);
            metricsService.recordExceptionProcessingTime(processingTime, interfaceType);

            return savedException;
        } finally {
            // Clear logging context
            LoggingConfig.LoggingContext.clearKeys("transactionId", "interfaceType");
        }
    }

    /**
     * Update exception status according to lifecycle management requirements
     * (US-006).
     *
     * @param transactionId the transaction ID of the exception to update
     * @param newStatus     the new status to set
     * @param updatedBy     the user or system updating the status
     * @return the updated InterfaceException entity
     * @throws IllegalArgumentException if exception not found or invalid status
     *                                  transition
     */
    @Transactional
    public InterfaceException updateExceptionStatus(String transactionId, ExceptionStatus newStatus, String updatedBy) {
        InterfaceException exception = exceptionRepository.findByTransactionId(transactionId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Exception not found for transaction: " + transactionId));

        validateStatusTransition(exception.getStatus(), newStatus);

        exception.setStatus(newStatus);

        // Set appropriate timestamps based on status
        switch (newStatus) {
            case ACKNOWLEDGED:
                exception.setAcknowledgedAt(OffsetDateTime.now());
                exception.setAcknowledgedBy(updatedBy);
                break;
            case RETRIED_SUCCESS:
            case RESOLVED:
                exception.setResolvedAt(OffsetDateTime.now());
                exception.setResolvedBy(updatedBy);
                break;
            case RETRIED_FAILED:
                exception.setRetryCount(exception.getRetryCount() + 1);
                exception.setLastRetryAt(OffsetDateTime.now());
                break;
        }

        InterfaceException updatedException = exceptionRepository.save(exception);
        log.info("Updated exception status to {} for transaction: {}", newStatus, transactionId);

        return updatedException;
    }

    /**
     * Increment retry count for an exception.
     *
     * @param transactionId the transaction ID of the exception
     * @return the updated InterfaceException entity
     */
    @Transactional
    public InterfaceException incrementRetryCount(String transactionId) {
        InterfaceException exception = exceptionRepository.findByTransactionId(transactionId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Exception not found for transaction: " + transactionId));

        exception.setRetryCount(exception.getRetryCount() + 1);
        exception.setLastRetryAt(OffsetDateTime.now());

        InterfaceException updatedException = exceptionRepository.save(exception);
        log.info("Incremented retry count to {} for transaction: {}",
                updatedException.getRetryCount(), transactionId);

        return updatedException;
    }

    /**
     * Retrieve and store order data from mock RSocket server.
     * Implements requirements 3.1, 3.2, 3.3, 1.5 for order data retrieval.
     *
     * @param exception the interface exception to retrieve order data for
     */
    private void retrieveAndStoreOrderData(InterfaceException exception) {
        try {
            exception.setOrderRetrievalAttempted(true);
            
            log.info("Attempting to retrieve order data for externalId: {} from mock server", 
                    exception.getExternalId());

            // Get the appropriate source service client for ORDER interface type
            if (clientRegistry.hasClient(InterfaceType.ORDER)) {
                SourceServiceClient client = clientRegistry.getClient(InterfaceType.ORDER);
                
                // Retrieve order data with timeout
                PayloadResponse response = client.getOriginalPayload(exception)
                    .get(10, TimeUnit.SECONDS);
                
                if (response.isRetrieved() && response.getPayload() != null) {
                    // Successfully retrieved order data
                    exception.setOrderReceived(response.getPayload());
                    exception.setOrderRetrievedAt(OffsetDateTime.now());
                    exception.setRetryable(true);
                    
                    log.info("Successfully retrieved order data for externalId: {} from service: {}", 
                            exception.getExternalId(), response.getSourceService());
                } else {
                    // Failed to retrieve order data
                    String errorMessage = response.getErrorMessage() != null ? 
                        response.getErrorMessage() : "Order data not found";
                    exception.setOrderRetrievalError(errorMessage);
                    exception.setRetryable(false);
                    
                    log.warn("Failed to retrieve order data for externalId: {}, error: {}", 
                            exception.getExternalId(), errorMessage);
                }
            } else {
                log.warn("No source service client available for ORDER interface type");
                exception.setOrderRetrievalError("No source service client available");
                exception.setRetryable(false);
            }
        } catch (Exception e) {
            // Handle any errors during order data retrieval
            String errorMessage = "Error retrieving order data: " + e.getMessage();
            exception.setOrderRetrievalError(errorMessage);
            exception.setRetryable(false);
            
            log.error("Error retrieving order data for externalId: {}", 
                    exception.getExternalId(), e);
        }
    }

    /**
     * Update an existing exception with new event data (duplicate detection logic).
     */
    private InterfaceException updateExistingException(InterfaceException existing, Object event) {
        // Update processed timestamp to indicate recent activity
        existing.setProcessedAt(OffsetDateTime.now());

        // Update exception reason if it's different (could indicate new information)
        String newReason = extractExceptionReason(event);
        if (!existing.getExceptionReason().equals(newReason)) {
            existing.setExceptionReason(newReason);
            log.info("Updated exception reason for transaction: {}", existing.getTransactionId());
        }

        // If this is an OrderRejected event and order data hasn't been retrieved yet, attempt retrieval
        if (event instanceof OrderRejectedEvent && !existing.getOrderRetrievalAttempted()) {
            log.info("Attempting order data retrieval for existing exception: {}", existing.getTransactionId());
            retrieveAndStoreOrderData(existing);
        }

        return exceptionRepository.save(existing);
    }

    /**
     * Extract exception reason from various event types.
     */
    private String extractExceptionReason(Object event) {
        if (event instanceof OrderRejectedEvent) {
            return ((OrderRejectedEvent) event).getPayload().getRejectedReason();
        } else if (event instanceof OrderCancelledEvent) {
            return ((OrderCancelledEvent) event).getPayload().getCancelReason();
        } else if (event instanceof CollectionRejectedEvent) {
            return ((CollectionRejectedEvent) event).getPayload().getRejectedReason();
        } else if (event instanceof DistributionFailedEvent) {
            return ((DistributionFailedEvent) event).getPayload().getFailureReason();
        } else if (event instanceof ValidationErrorEvent) {
            return ((ValidationErrorEvent) event).getPayload().getValidationErrors().stream()
                    .map(error -> String.format("Field '%s': %s", error.getField(), error.getMessage()))
                    .collect(Collectors.joining("; "));
        }
        return "Unknown exception reason";
    }

    /**
     * Categorize order exceptions based on rejection reason.
     */
    private ExceptionCategory categorizeOrderException(String rejectedReason) {
        String reason = rejectedReason.toLowerCase();

        if (reason.contains("already exists") || reason.contains("duplicate")) {
            return ExceptionCategory.BUSINESS_RULE;
        } else if (reason.contains("validation") || reason.contains("invalid") || reason.contains("required")) {
            return ExceptionCategory.VALIDATION;
        } else if (reason.contains("timeout") || reason.contains("connection")) {
            return ExceptionCategory.NETWORK_ERROR;
        } else if (reason.contains("unauthorized") || reason.contains("forbidden")) {
            return ExceptionCategory.AUTHORIZATION;
        } else if (reason.contains("authentication") || reason.contains("credentials")) {
            return ExceptionCategory.AUTHENTICATION;
        } else if (reason.contains("system") || reason.contains("internal")) {
            return ExceptionCategory.SYSTEM_ERROR;
        } else {
            return ExceptionCategory.BUSINESS_RULE; // Default for order exceptions
        }
    }

    /**
     * Categorize collection exceptions based on rejection reason.
     */
    private ExceptionCategory categorizeCollectionException(String rejectedReason) {
        String reason = rejectedReason.toLowerCase();

        if (reason.contains("validation") || reason.contains("invalid") || reason.contains("required")) {
            return ExceptionCategory.VALIDATION;
        } else if (reason.contains("donor") || reason.contains("collection") || reason.contains("sample")) {
            return ExceptionCategory.BUSINESS_RULE;
        } else if (reason.contains("timeout") || reason.contains("connection")) {
            return ExceptionCategory.NETWORK_ERROR;
        } else if (reason.contains("system") || reason.contains("internal")) {
            return ExceptionCategory.SYSTEM_ERROR;
        } else {
            return ExceptionCategory.BUSINESS_RULE; // Default for collection exceptions
        }
    }

    /**
     * Categorize distribution exceptions based on failure reason.
     */
    private ExceptionCategory categorizeDistributionException(String failureReason) {
        String reason = failureReason.toLowerCase();

        if (reason.contains("validation") || reason.contains("invalid") || reason.contains("required")) {
            return ExceptionCategory.VALIDATION;
        } else if (reason.contains("destination") || reason.contains("location") || reason.contains("inventory")) {
            return ExceptionCategory.BUSINESS_RULE;
        } else if (reason.contains("timeout") || reason.contains("connection")) {
            return ExceptionCategory.NETWORK_ERROR;
        } else if (reason.contains("external") || reason.contains("service")) {
            return ExceptionCategory.EXTERNAL_SERVICE;
        } else if (reason.contains("system") || reason.contains("internal")) {
            return ExceptionCategory.SYSTEM_ERROR;
        } else {
            return ExceptionCategory.BUSINESS_RULE; // Default for distribution exceptions
        }
    }

    /**
     * Assign severity based on interface type and exception reason.
     */
    private ExceptionSeverity assignSeverity(InterfaceType interfaceType, String exceptionReason) {
        String reason = exceptionReason.toLowerCase();

        // Critical severity conditions
        if (reason.contains("system error") || reason.contains("internal error") ||
                reason.contains("database") || reason.contains("critical")) {
            return ExceptionSeverity.CRITICAL;
        }

        // High severity conditions
        if (reason.contains("timeout") || reason.contains("connection failed") ||
                reason.contains("service unavailable") || reason.contains("authentication failed")) {
            return ExceptionSeverity.HIGH;
        }

        // Interface-specific severity rules (check before generic rules)
        switch (interfaceType) {
            case ORDER:
                if (reason.contains("customer")) {
                    return ExceptionSeverity.HIGH;
                }
                break;
            case COLLECTION:
                if (reason.contains("donor") || reason.contains("sample")) {
                    return ExceptionSeverity.HIGH;
                }
                break;
            case DISTRIBUTION:
                if (reason.contains("destination") || reason.contains("delivery")) {
                    return ExceptionSeverity.HIGH;
                }
                break;
        }

        // Medium severity conditions (default for most business rule violations)
        if (reason.contains("validation") || reason.contains("invalid") ||
                reason.contains("already exists") || reason.contains("not found")) {
            return ExceptionSeverity.MEDIUM;
        }

        // Low severity conditions
        if (reason.contains("warning") || reason.contains("info")) {
            return ExceptionSeverity.LOW;
        }

        // Default severity
        return ExceptionSeverity.MEDIUM;
    }

    /**
     * Determine if an exception is retryable based on the exception reason.
     */
    private Boolean determineRetryability(String exceptionReason) {
        String reason = exceptionReason.toLowerCase();

        // Non-retryable conditions
        if (reason.contains("already exists") || reason.contains("duplicate") ||
                reason.contains("invalid format") || reason.contains("malformed") ||
                reason.contains("authentication failed") || reason.contains("unauthorized")) {
            return false;
        }

        // Retryable conditions
        if (reason.contains("timeout") || reason.contains("connection") ||
                reason.contains("service unavailable") || reason.contains("temporary")) {
            return true;
        }

        // Default to retryable for most exceptions
        return true;
    }

    /**
     * Validate status transitions according to exception lifecycle rules.
     */
    private void validateStatusTransition(ExceptionStatus currentStatus, ExceptionStatus newStatus) {
        // Define valid transitions
        switch (currentStatus) {
            case NEW:
                if (newStatus != ExceptionStatus.ACKNOWLEDGED && newStatus != ExceptionStatus.ESCALATED) {
                    throw new IllegalArgumentException(
                            String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
                }
                break;
            case ACKNOWLEDGED:
                if (newStatus != ExceptionStatus.RETRIED_SUCCESS && newStatus != ExceptionStatus.RETRIED_FAILED &&
                        newStatus != ExceptionStatus.RESOLVED && newStatus != ExceptionStatus.ESCALATED) {
                    throw new IllegalArgumentException(
                            String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
                }
                break;
            case RETRIED_FAILED:
                if (newStatus != ExceptionStatus.RETRIED_SUCCESS && newStatus != ExceptionStatus.RETRIED_FAILED &&
                        newStatus != ExceptionStatus.ESCALATED && newStatus != ExceptionStatus.RESOLVED) {
                    throw new IllegalArgumentException(
                            String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
                }
                break;
            case RETRIED_SUCCESS:
                if (newStatus != ExceptionStatus.RESOLVED && newStatus != ExceptionStatus.CLOSED) {
                    throw new IllegalArgumentException(
                            String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
                }
                break;
            case ESCALATED:
                if (newStatus != ExceptionStatus.RESOLVED && newStatus != ExceptionStatus.CLOSED) {
                    throw new IllegalArgumentException(
                            String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
                }
                break;
            case RESOLVED:
                if (newStatus != ExceptionStatus.CLOSED) {
                    throw new IllegalArgumentException(
                            String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
                }
                break;
            case CLOSED:
                throw new IllegalArgumentException("Cannot transition from CLOSED status");
        }
    }
}