package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.dto.AcknowledgeRequest;
import com.arcone.biopro.exception.collector.api.dto.AcknowledgeResponse;
import com.arcone.biopro.exception.collector.api.graphql.dto.*;
import com.arcone.biopro.exception.collector.application.service.ExceptionManagementService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.exception.ExceptionNotFoundException;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * GraphQL-specific acknowledgment service that handles acknowledgment operations with circuit
 * breaker patterns, proper error handling, and audit logging for GraphQL responses.
 * Implements requirements 3.4, 3.5, 6.4, and 7.1.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GraphQLAcknowledgmentService {

    private final ExceptionManagementService exceptionManagementService;
    private final InterfaceExceptionRepository exceptionRepository;
    private final AcknowledgmentValidationService validationService;

    /**
     * Acknowledges a single exception with circuit breaker and timeout protection.
     * Implements optimistic locking to handle concurrent updates.
     *
     * @param input the acknowledgment input
     * @param authentication the user authentication context
     * @return CompletableFuture containing the acknowledgment result
     */
    @CircuitBreaker(name = "acknowledgment-service", fallbackMethod = "acknowledgeExceptionFallback")
    @TimeLimiter(name = "acknowledgment-service")
    @Retry(name = "acknowledgment-service")
    public CompletableFuture<AcknowledgeExceptionResult> acknowledgeException(
            AcknowledgeExceptionInput input, Authentication authentication) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Processing GraphQL acknowledgment for transaction: {} by user: {}",
                        input.getTransactionId(), authentication.getName());

                // Validate acknowledgment request and user permissions
                validationService.validateAcknowledgmentRequest(input, authentication);

                // Check if exception can be acknowledged
                if (!exceptionManagementService.canAcknowledge(input.getTransactionId())) {
                    log.warn("Exception cannot be acknowledged - transaction: {}, user: {}",
                            input.getTransactionId(), authentication.getName());
                    
                    return AcknowledgeExceptionResult.failure(GraphQLError.builder()
                            .message("Exception cannot be acknowledged in its current state")
                            .code("ACKNOWLEDGMENT_NOT_ALLOWED")
                            .build());
                }

                // Convert GraphQL input to service request
                AcknowledgeRequest acknowledgeRequest = AcknowledgeRequest.builder()
                        .acknowledgedBy(authentication.getName())
                        .notes(buildAcknowledgmentNotes(input))
                        .build();

                // Execute acknowledgment through existing service with optimistic locking
                AcknowledgeResponse acknowledgeResponse = exceptionManagementService
                        .acknowledgeException(input.getTransactionId(), acknowledgeRequest);

                // Fetch updated exception
                InterfaceException exception = exceptionRepository
                        .findByTransactionId(input.getTransactionId())
                        .orElseThrow(() -> new ExceptionNotFoundException(
                                "Exception not found after acknowledgment"));

                // Log successful acknowledgment for audit trail
                log.info("Exception acknowledged successfully - transaction: {}, user: {}, timestamp: {}",
                        input.getTransactionId(), authentication.getName(), 
                        acknowledgeResponse.getAcknowledgedAt());

                return AcknowledgeExceptionResult.success(exception);

            } catch (IllegalArgumentException e) {
                log.warn("Acknowledgment validation failed for transaction: {}, error: {}",
                        input.getTransactionId(), e.getMessage());

                return AcknowledgeExceptionResult.failure(GraphQLError.builder()
                        .message(e.getMessage())
                        .code("VALIDATION_ERROR")
                        .build());

            } catch (IllegalStateException e) {
                log.warn("Acknowledgment state error for transaction: {}, error: {}",
                        input.getTransactionId(), e.getMessage());

                return AcknowledgeExceptionResult.failure(GraphQLError.builder()
                        .message(e.getMessage())
                        .code("ACKNOWLEDGMENT_NOT_ALLOWED")
                        .build());

            } catch (org.springframework.dao.OptimisticLockingFailureException e) {
                log.warn("Optimistic locking failure during acknowledgment for transaction: {}, user: {}",
                        input.getTransactionId(), authentication.getName());

                return AcknowledgeExceptionResult.failure(GraphQLError.builder()
                        .message("Exception was modified by another user. Please refresh and try again.")
                        .code("CONCURRENT_MODIFICATION")
                        .build());

            } catch (Exception e) {
                log.error("Acknowledgment operation failed for transaction: {}, error: {}",
                        input.getTransactionId(), e.getMessage(), e);

                return AcknowledgeExceptionResult.failure(GraphQLError.builder()
                        .message("Internal error occurred during acknowledgment operation")
                        .code("INTERNAL_ERROR")
                        .build());
            }
        });
    }

    /**
     * Fallback method for acknowledge exception when circuit breaker is open.
     */
    public CompletableFuture<AcknowledgeExceptionResult> acknowledgeExceptionFallback(
            AcknowledgeExceptionInput input, Authentication authentication, Exception ex) {

        log.error("Acknowledgment service circuit breaker activated for transaction: {}, error: {}",
                input.getTransactionId(), ex.getMessage());

        return CompletableFuture.completedFuture(
                AcknowledgeExceptionResult.failure(GraphQLError.builder()
                        .message("Acknowledgment service is temporarily unavailable. Please try again later.")
                        .code("SERVICE_UNAVAILABLE")
                        .build()));
    }

    /**
     * Performs bulk acknowledgment operations with individual error handling.
     * Implements optimistic locking for each individual acknowledgment.
     *
     * @param input the bulk acknowledgment input
     * @param authentication the user authentication context
     * @return CompletableFuture containing the bulk acknowledgment result
     */
    @CircuitBreaker(name = "bulk-acknowledgment-service", fallbackMethod = "bulkAcknowledgeExceptionsFallback")
    @TimeLimiter(name = "bulk-acknowledgment-service")
    public CompletableFuture<BulkAcknowledgeResult> bulkAcknowledgeExceptions(
            BulkAcknowledgeInput input, Authentication authentication) {
        
        return CompletableFuture.supplyAsync(() -> {
            log.info("Processing bulk acknowledgment for {} transactions by user: {}",
                    input.getTransactionIds().size(), authentication.getName());

            // Validate bulk acknowledgment request and user permissions
            validationService.validateBulkAcknowledgmentRequest(input.getTransactionIds(), authentication);
            validationService.validateBulkOperationSize(input.getTransactionIds().size(), authentication);

            List<AcknowledgeExceptionResult> results = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;

            for (String transactionId : input.getTransactionIds()) {
                try {
                    AcknowledgeExceptionInput singleInput = AcknowledgeExceptionInput.builder()
                            .transactionId(transactionId)
                            .reason(input.getReason())
                            .notes(input.getNotes())
                            .assignedTo(input.getAssignedTo())
                            .build();

                    // Process each acknowledgment synchronously to avoid overwhelming the system
                    AcknowledgeExceptionResult result = acknowledgeException(singleInput, authentication)
                            .join();
                    results.add(result);

                    if (result.isSuccess()) {
                        successCount++;
                    } else {
                        failureCount++;
                    }

                } catch (CompletionException e) {
                    log.error("Bulk acknowledgment failed for transaction: {}, error: {}",
                            transactionId, e.getMessage());

                    AcknowledgeExceptionResult failedResult = AcknowledgeExceptionResult.failure(
                            GraphQLError.builder()
                                    .message("Failed to process acknowledgment for transaction: " + transactionId)
                                    .code("BULK_ACKNOWLEDGMENT_ITEM_FAILED")
                                    .build());

                    results.add(failedResult);
                    failureCount++;
                }
            }

            log.info("Bulk acknowledgment completed: {} successful, {} failed", successCount, failureCount);

            return BulkAcknowledgeResult.builder()
                    .successCount(successCount)
                    .failureCount(failureCount)
                    .results(results)
                    .errors(List.of())
                    .build();
        });
    }

    /**
     * Fallback method for bulk acknowledgment when circuit breaker is open.
     */
    public CompletableFuture<BulkAcknowledgeResult> bulkAcknowledgeExceptionsFallback(
            BulkAcknowledgeInput input, Authentication authentication, Exception ex) {

        log.error("Bulk acknowledgment service circuit breaker activated, error: {}", ex.getMessage());

        return CompletableFuture.completedFuture(
                BulkAcknowledgeResult.failure(
                        GraphQLError.builder()
                                .message("Bulk acknowledgment service is temporarily unavailable. Please try again later.")
                                .code("SERVICE_UNAVAILABLE")
                                .build(),
                        input.getTransactionIds().size()));
    }

    /**
     * Builds comprehensive acknowledgment notes from the input.
     * Combines reason, notes, and assignment information.
     *
     * @param input the acknowledgment input
     * @return formatted acknowledgment notes
     */
    private String buildAcknowledgmentNotes(AcknowledgeExceptionInput input) {
        StringBuilder notes = new StringBuilder();
        notes.append("Reason: ").append(input.getReason());
        
        if (input.getNotes() != null && !input.getNotes().trim().isEmpty()) {
            notes.append("\nNotes: ").append(input.getNotes());
        }
        
        if (input.getAssignedTo() != null && !input.getAssignedTo().trim().isEmpty()) {
            notes.append("\nAssigned to: ").append(input.getAssignedTo());
        }
        
        if (input.getEstimatedResolutionTime() != null) {
            notes.append("\nEstimated resolution time: ").append(input.getEstimatedResolutionTime());
        }
        
        return notes.toString();
    }
}