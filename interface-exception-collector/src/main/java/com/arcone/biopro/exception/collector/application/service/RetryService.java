package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.api.dto.RetryRequest;
import com.arcone.biopro.exception.collector.api.dto.RetryResponse;
import com.arcone.biopro.exception.collector.api.graphql.service.SubscriptionEventBridge;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.RetryStatus;
import com.arcone.biopro.exception.collector.infrastructure.kafka.publisher.RetryEventPublisher;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for orchestrating retry operations on interface exceptions.
 * Manages the complete retry lifecycle including payload retrieval, submission,
 * and history tracking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RetryService {

    private final InterfaceExceptionRepository exceptionRepository;
    private final RetryAttemptRepository retryAttemptRepository;
    private final PayloadRetrievalService payloadRetrievalService;
    private final RetryEventPublisher retryEventPublisher;
    private final MetricsService metricsService;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Initiates a retry operation for the specified exception.
     *
     * @param transactionId the transaction ID of the exception to retry
     * @param retryRequest  the retry request details
     * @return RetryResponse containing retry operation details
     * @throws IllegalArgumentException if exception not found or not retryable
     */
    @Transactional
    public RetryResponse initiateRetry(String transactionId, RetryRequest retryRequest) {
        log.info("Initiating retry for transaction: {}, reason: {}", transactionId, retryRequest.getReason());

        // Find and validate the exception
        InterfaceException exception = exceptionRepository.findByTransactionId(transactionId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Exception not found for transaction: " + transactionId));

        if (!exception.getRetryable()) {
            throw new IllegalArgumentException("Exception is not retryable for transaction: " + transactionId);
        }

        // Get next attempt number
        Integer nextAttemptNumber = retryAttemptRepository.getNextAttemptNumber(exception);

        // Create retry attempt record
        RetryAttempt retryAttempt = RetryAttempt.builder()
                .interfaceException(exception)
                .attemptNumber(nextAttemptNumber)
                .status(RetryStatus.PENDING)
                .initiatedBy(retryRequest.getInitiatedBy())
                .initiatedAt(OffsetDateTime.now())
                .build();

        retryAttempt = retryAttemptRepository.save(retryAttempt);

        // Update exception retry count and last retry timestamp
        exception.setRetryCount(exception.getRetryCount() + 1);
        exception.setLastRetryAt(OffsetDateTime.now());
        exceptionRepository.save(exception);

        // Publish application event for GraphQL subscriptions
        try {
            applicationEventPublisher.publishEvent(
                    new SubscriptionEventBridge.RetryInitiatedEvent(exception, retryAttempt,
                            retryRequest.getInitiatedBy()));
            log.debug("Published RetryInitiated event for GraphQL subscriptions: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to publish RetryInitiated event for GraphQL subscriptions: {}", e.getMessage(), e);
        }

        // Perform retry asynchronously
        performRetryAsync(exception, retryAttempt, retryRequest);

        // Calculate estimated completion time (5 minutes from now)
        OffsetDateTime estimatedCompletion = OffsetDateTime.now().plusMinutes(5);

        log.info("Retry initiated for transaction: {}, attempt: {}", transactionId, nextAttemptNumber);

        return RetryResponse.builder()
                .retryId(retryAttempt.getId())
                .status("PENDING")
                .message("Retry operation initiated successfully")
                .estimatedCompletionTime(estimatedCompletion)
                .attemptNumber(nextAttemptNumber)
                .build();
    }

    /**
     * Performs the actual retry operation asynchronously.
     */
    private void performRetryAsync(InterfaceException exception, RetryAttempt retryAttempt, RetryRequest retryRequest) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting async retry execution for transaction: {}, attempt: {}",
                        exception.getTransactionId(), retryAttempt.getAttemptNumber());

                // Step 1: Retrieve original payload
                PayloadResponse payloadResponse = payloadRetrievalService.getOriginalPayload(exception).get();

                if (!payloadResponse.isRetrieved()) {
                    // Payload retrieval failed
                    handleRetryFailure(exception, retryAttempt,
                            "Failed to retrieve original payload: " + payloadResponse.getErrorMessage(),
                            null, payloadResponse.getErrorMessage());
                    return;
                }

                // Step 2: Submit retry to source service
                ResponseEntity<Object> retryResponse = payloadRetrievalService
                        .submitRetry(exception, payloadResponse.getPayload()).get();

                // Step 3: Process retry result
                if (retryResponse.getStatusCode().is2xxSuccessful()) {
                    handleRetrySuccess(exception, retryAttempt,
                            "Retry completed successfully",
                            retryResponse.getStatusCode().value());
                } else {
                    handleRetryFailure(exception, retryAttempt,
                            "Retry failed with status: " + retryResponse.getStatusCode(),
                            retryResponse.getStatusCode().value(),
                            retryResponse.getBody() != null ? retryResponse.getBody().toString() : "No response body");
                }

            } catch (Exception e) {
                log.error("Retry execution failed for transaction: {}, attempt: {}, error: {}",
                        exception.getTransactionId(), retryAttempt.getAttemptNumber(), e.getMessage(), e);

                handleRetryFailure(exception, retryAttempt,
                        "Retry execution failed: " + e.getMessage(),
                        null, e.getMessage());
            }
        });
    }

    /**
     * Handles successful retry completion.
     */
    @Transactional
    public void handleRetrySuccess(InterfaceException exception, RetryAttempt retryAttempt,
            String message, Integer responseCode) {
        log.info("Retry succeeded for transaction: {}, attempt: {}",
                exception.getTransactionId(), retryAttempt.getAttemptNumber());

        // Calculate retry duration
        Duration retryDuration = Duration.between(retryAttempt.getInitiatedAt().toInstant(), Instant.now());

        // Update retry attempt
        retryAttempt.markAsSuccess(message, responseCode);
        retryAttemptRepository.save(retryAttempt);

        // Update exception status
        exception.setStatus(ExceptionStatus.RETRIED_SUCCESS);
        exception.setResolvedAt(OffsetDateTime.now());
        exceptionRepository.save(exception);

        // Record metrics
        metricsService.recordRetryOperation(exception.getInterfaceType(), true);
        metricsService.recordRetryOperationTime(retryDuration, exception.getInterfaceType(), true);

        // Publish retry completed event
        publishRetryCompletedEvent(exception, retryAttempt, RetryStatus.SUCCESS);

        // Publish application event for GraphQL subscriptions
        try {
            applicationEventPublisher.publishEvent(
                    new SubscriptionEventBridge.RetryCompletedEvent(exception, retryAttempt, true));
            log.debug("Published RetryCompleted event for GraphQL subscriptions: {}", exception.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to publish RetryCompleted event for GraphQL subscriptions: {}", e.getMessage(), e);
        }

        log.info("Retry success handling completed for transaction: {}", exception.getTransactionId());
    }

    /**
     * Handles failed retry completion.
     */
    @Transactional
    public void handleRetryFailure(InterfaceException exception, RetryAttempt retryAttempt,
            String message, Integer responseCode, String errorDetails) {
        log.warn("Retry failed for transaction: {}, attempt: {}, message: {}",
                exception.getTransactionId(), retryAttempt.getAttemptNumber(), message);

        // Calculate retry duration
        Duration retryDuration = Duration.between(retryAttempt.getInitiatedAt().toInstant(), Instant.now());

        // Update retry attempt
        retryAttempt.markAsFailed(message, responseCode, errorDetails);
        retryAttemptRepository.save(retryAttempt);

        // Update exception status
        exception.setStatus(ExceptionStatus.RETRIED_FAILED);
        exceptionRepository.save(exception);

        // Record metrics
        metricsService.recordRetryOperation(exception.getInterfaceType(), false);
        metricsService.recordRetryOperationTime(retryDuration, exception.getInterfaceType(), false);

        // Publish retry completed event
        publishRetryCompletedEvent(exception, retryAttempt, RetryStatus.FAILED);

        // Publish application event for GraphQL subscriptions
        try {
            applicationEventPublisher.publishEvent(
                    new SubscriptionEventBridge.RetryCompletedEvent(exception, retryAttempt, false));
            log.debug("Published RetryCompleted event for GraphQL subscriptions: {}", exception.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to publish RetryCompleted event for GraphQL subscriptions: {}", e.getMessage(), e);
        }

        log.info("Retry failure handling completed for transaction: {}", exception.getTransactionId());
    }

    /**
     * Retrieves retry history for an exception.
     *
     * @param transactionId the transaction ID
     * @return List of retry attempts ordered by attempt number
     */
    public List<RetryAttempt> getRetryHistory(String transactionId) {
        InterfaceException exception = exceptionRepository.findByTransactionId(transactionId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Exception not found for transaction: " + transactionId));

        return retryAttemptRepository.findByInterfaceExceptionOrderByAttemptNumberAsc(exception);
    }

    /**
     * Gets the latest retry attempt for an exception.
     *
     * @param transactionId the transaction ID
     * @return Optional containing the latest retry attempt
     */
    public Optional<RetryAttempt> getLatestRetryAttempt(String transactionId) {
        InterfaceException exception = exceptionRepository.findByTransactionId(transactionId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Exception not found for transaction: " + transactionId));

        return retryAttemptRepository.findTopByInterfaceExceptionOrderByAttemptNumberDesc(exception);
    }

    /**
     * Gets retry statistics for an exception.
     *
     * @param transactionId the transaction ID
     * @return Array containing [totalAttempts, successfulAttempts, failedAttempts,
     *         pendingAttempts]
     */
    public Object[] getRetryStatistics(String transactionId) {
        InterfaceException exception = exceptionRepository.findByTransactionId(transactionId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Exception not found for transaction: " + transactionId));

        return retryAttemptRepository.getRetryStatistics(exception);
    }

    /**
     * Publishes retry completed event to Kafka.
     */
    private void publishRetryCompletedEvent(InterfaceException exception, RetryAttempt retryAttempt,
            RetryStatus status) {
        try {
            retryEventPublisher.publishRetryCompleted(
                    exception.getId(),
                    exception.getTransactionId(),
                    retryAttempt.getAttemptNumber(),
                    status,
                    retryAttempt.getResultMessage(),
                    retryAttempt.getInitiatedBy(),
                    retryAttempt.getCompletedAt());
        } catch (Exception e) {
            log.error("Failed to publish retry completed event for transaction: {}, error: {}",
                    exception.getTransactionId(), e.getMessage(), e);
        }
    }

    /**
     * Validates if an exception can be retried.
     *
     * @param transactionId the transaction ID
     * @return true if retryable, false otherwise
     */
    public boolean canRetry(String transactionId) {
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);

        if (exceptionOpt.isEmpty()) {
            return false;
        }

        InterfaceException exception = exceptionOpt.get();

        // Check if exception is marked as retryable
        if (!exception.getRetryable()) {
            return false;
        }

        // Check if exception is already resolved
        if (exception.getStatus() == ExceptionStatus.RESOLVED ||
                exception.getStatus() == ExceptionStatus.CLOSED) {
            return false;
        }

        // Check if there's already a pending retry
        Optional<RetryAttempt> latestAttempt = retryAttemptRepository
                .findTopByInterfaceExceptionOrderByAttemptNumberDesc(exception);

        if (latestAttempt.isPresent() && latestAttempt.get().getStatus() == RetryStatus.PENDING) {
            return false;
        }

        return true;
    }

    /**
     * Cancels a pending retry attempt.
     *
     * @param transactionId the transaction ID
     * @param attemptNumber the attempt number to cancel
     * @return true if cancelled successfully, false otherwise
     */
    @Transactional
    public boolean cancelRetry(String transactionId, Integer attemptNumber) {
        InterfaceException exception = exceptionRepository.findByTransactionId(transactionId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Exception not found for transaction: " + transactionId));

        Optional<RetryAttempt> retryAttemptOpt = retryAttemptRepository
                .findByInterfaceExceptionAndAttemptNumber(exception, attemptNumber);

        if (retryAttemptOpt.isEmpty()) {
            return false;
        }

        RetryAttempt retryAttempt = retryAttemptOpt.get();

        if (retryAttempt.getStatus() != RetryStatus.PENDING) {
            return false;
        }

        // Mark as failed with cancellation message
        retryAttempt.markAsFailed("Retry cancelled by user", null, "User cancelled retry operation");
        retryAttemptRepository.save(retryAttempt);

        log.info("Retry cancelled for transaction: {}, attempt: {}", transactionId, attemptNumber);

        return true;
    }
}
