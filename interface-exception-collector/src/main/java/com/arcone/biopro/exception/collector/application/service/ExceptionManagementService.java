package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.api.dto.AcknowledgeRequest;
import com.arcone.biopro.exception.collector.api.dto.AcknowledgeResponse;
import com.arcone.biopro.exception.collector.api.dto.ResolveRequest;
import com.arcone.biopro.exception.collector.api.dto.ResolveResponse;
import com.arcone.biopro.exception.collector.api.graphql.service.SubscriptionEventBridge;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.infrastructure.kafka.publisher.ExceptionEventPublisher;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Service for managing exception acknowledgment and resolution operations.
 * Implements requirements US-013 and US-014 for exception lifecycle management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExceptionManagementService {

    private final InterfaceExceptionRepository exceptionRepository;
    private final ExceptionEventPublisher eventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Acknowledges an exception by updating its status and audit information.
     * Implements requirement US-013 for exception acknowledgment.
     *
     * @param transactionId the transaction ID of the exception to acknowledge
     * @param request       the acknowledgment request containing user and notes
     * @return AcknowledgeResponse with acknowledgment details
     * @throws IllegalArgumentException if the exception is not found
     */
    @Transactional
    public AcknowledgeResponse acknowledgeException(String transactionId, AcknowledgeRequest request) {
        log.info("Acknowledging exception with transaction ID: {} by user: {}",
                transactionId, request.getAcknowledgedBy());

        // Find the exception
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
        if (exceptionOpt.isEmpty()) {
            log.warn("Exception not found for transaction ID: {}", transactionId);
            throw new IllegalArgumentException("Exception not found with transaction ID: " + transactionId);
        }

        InterfaceException exception = exceptionOpt.get();

        // Update acknowledgment information
        exception.setStatus(ExceptionStatus.ACKNOWLEDGED);
        exception.setAcknowledgedAt(OffsetDateTime.now());
        exception.setAcknowledgedBy(request.getAcknowledgedBy());
        exception.setAcknowledgmentNotes(request.getNotes());

        // Save the updated exception
        InterfaceException savedException = exceptionRepository.save(exception);

        log.info("Exception acknowledged successfully - transaction ID: {}, acknowledged by: {}, at: {}",
                transactionId, savedException.getAcknowledgedBy(), savedException.getAcknowledgedAt());

        // Publish application event for GraphQL subscriptions
        try {
            applicationEventPublisher.publishEvent(
                    new SubscriptionEventBridge.ExceptionAcknowledgedEvent(savedException,
                            request.getAcknowledgedBy()));
            log.debug("Published ExceptionAcknowledged event for GraphQL subscriptions: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to publish ExceptionAcknowledged event for GraphQL subscriptions: {}", e.getMessage(), e);
        }

        // Build and return response
        return AcknowledgeResponse.builder()
                .status(savedException.getStatus().name())
                .acknowledgedAt(savedException.getAcknowledgedAt())
                .acknowledgedBy(savedException.getAcknowledgedBy())
                .notes(savedException.getAcknowledgmentNotes())
                .transactionId(transactionId)
                .build();
    }

    /**
     * Resolves an exception by updating its status and resolution information.
     * Publishes an ExceptionResolved event upon successful resolution.
     * Implements requirement US-014 for exception resolution management.
     *
     * @param transactionId the transaction ID of the exception to resolve
     * @param request       the resolution request containing resolution details
     * @return ResolveResponse with resolution details
     * @throws IllegalArgumentException if the exception is not found
     */
    @Transactional
    public ResolveResponse resolveException(String transactionId, ResolveRequest request) {
        log.info("Resolving exception with transaction ID: {} by user: {} using method: {}",
                transactionId, request.getResolvedBy(), request.getResolutionMethod());

        // Find the exception
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
        if (exceptionOpt.isEmpty()) {
            log.warn("Exception not found for transaction ID: {}", transactionId);
            throw new IllegalArgumentException("Exception not found with transaction ID: " + transactionId);
        }

        InterfaceException exception = exceptionOpt.get();

        // Update resolution information
        exception.setStatus(ExceptionStatus.RESOLVED);
        exception.setResolvedAt(OffsetDateTime.now());
        exception.setResolvedBy(request.getResolvedBy());
        exception.setResolutionMethod(request.getResolutionMethod());
        exception.setResolutionNotes(request.getResolutionNotes());

        // Save the updated exception
        InterfaceException savedException = exceptionRepository.save(exception);

        log.info("Exception resolved successfully - transaction ID: {}, resolved by: {}, method: {}, at: {}",
                transactionId, savedException.getResolvedBy(), savedException.getResolutionMethod(),
                savedException.getResolvedAt());

        // Publish ExceptionResolved event
        try {
            eventPublisher.publishExceptionResolved(savedException);
            log.debug("Published ExceptionResolved event for transaction ID: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to publish ExceptionResolved event for transaction ID: {}, error: {}",
                    transactionId, e.getMessage(), e);
            // Continue processing - event publishing failure should not fail the resolution
        }

        // Publish application event for GraphQL subscriptions
        try {
            applicationEventPublisher.publishEvent(
                    new SubscriptionEventBridge.ExceptionResolvedEvent(savedException, request.getResolvedBy()));
            log.debug("Published ExceptionResolved event for GraphQL subscriptions: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to publish ExceptionResolved event for GraphQL subscriptions: {}", e.getMessage(), e);
        }

        // Build and return response
        return ResolveResponse.builder()
                .status(savedException.getStatus().name())
                .resolvedAt(savedException.getResolvedAt())
                .resolvedBy(savedException.getResolvedBy())
                .resolutionMethod(savedException.getResolutionMethod())
                .resolutionNotes(savedException.getResolutionNotes())
                .transactionId(transactionId)
                .totalRetryAttempts(savedException.getRetryCount())
                .build();
    }

    /**
     * Checks if an exception can be acknowledged.
     * An exception can be acknowledged if it exists and is not already resolved or
     * closed.
     *
     * @param transactionId the transaction ID to check
     * @return true if the exception can be acknowledged, false otherwise
     */
    public boolean canAcknowledge(String transactionId) {
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
        if (exceptionOpt.isEmpty()) {
            return false;
        }

        ExceptionStatus status = exceptionOpt.get().getStatus();
        return status != ExceptionStatus.RESOLVED && status != ExceptionStatus.CLOSED;
    }

    /**
     * Checks if an exception can be resolved.
     * An exception can be resolved if it exists and is not already resolved or
     * closed.
     *
     * @param transactionId the transaction ID to check
     * @return true if the exception can be resolved, false otherwise
     */
    public boolean canResolve(String transactionId) {
        Optional<InterfaceException> exceptionOpt = exceptionRepository.findByTransactionId(transactionId);
        if (exceptionOpt.isEmpty()) {
            return false;
        }

        ExceptionStatus status = exceptionOpt.get().getStatus();
        return status != ExceptionStatus.RESOLVED && status != ExceptionStatus.CLOSED;
    }

    /**
     * Gets the current status of an exception.
     *
     * @param transactionId the transaction ID to check
     * @return Optional containing the exception status, or empty if not found
     */
    public Optional<ExceptionStatus> getExceptionStatus(String transactionId) {
        return exceptionRepository.findByTransactionId(transactionId)
                .map(InterfaceException::getStatus);
    }
}