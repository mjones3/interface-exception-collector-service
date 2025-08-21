package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.dto.AcknowledgeRequest;
import com.arcone.biopro.exception.collector.api.dto.AcknowledgeResponse;
import com.arcone.biopro.exception.collector.api.dto.ResolveRequest;
import com.arcone.biopro.exception.collector.api.dto.ResolveResponse;
import com.arcone.biopro.exception.collector.api.dto.RetryRequest;
import com.arcone.biopro.exception.collector.api.dto.RetryResponse;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkRetryInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkRetryResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.CancelRetryResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkAcknowledgeInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkAcknowledgeResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.ResolveExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError;
import com.arcone.biopro.exception.collector.application.service.RetryService;
import com.arcone.biopro.exception.collector.application.service.ExceptionManagementService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * GraphQL resolver for exception management mutations.
 * Handles retry, acknowledgment, and resolution operations using existing
 * business services.
 * Ensures consistency with REST API endpoints by delegating to the same service
 * layer.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class RetryMutationResolver {

        private final RetryService retryService;
        private final ExceptionManagementService exceptionManagementService;
        private final InterfaceExceptionRepository exceptionRepository;
        private final RetryAttemptRepository retryAttemptRepository;

        /**
         * Initiates a retry operation for a single exception.
         * Uses the same business logic as the REST API endpoint.
         *
         * @param input          the retry input containing transaction ID and retry
         *                       details
         * @param authentication the current user authentication
         * @return CompletableFuture containing the retry result
         */
        @MutationMapping
        @PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
        public CompletableFuture<RetryExceptionResult> retryException(
                        @Argument RetryExceptionInput input,
                        Authentication authentication) {

                log.info("GraphQL retry exception requested for transaction: {} by user: {}",
                                input.getTransactionId(), authentication.getName());

                return CompletableFuture.supplyAsync(() -> {
                        try {
                                // Validate if retry is possible (same logic as REST API)
                                if (!retryService.canRetry(input.getTransactionId())) {
                                        log.warn("Retry not allowed for transaction: {}", input.getTransactionId());
                                        return RetryExceptionResult.builder()
                                                        .success(false)
                                                        .errors(List.of(GraphQLError.builder()
                                                                        .message("Exception is not retryable or retry already in progress")
                                                                        .code("RETRY_NOT_ALLOWED")
                                                                        .build()))
                                                        .build();
                                }

                                // Convert GraphQL input to service request (same as REST API)
                                RetryRequest retryRequest = RetryRequest.builder()
                                                .reason(input.getReason())
                                                .initiatedBy(authentication.getName())
                                                .build();

                                // Execute retry through existing service (same as REST API)
                                RetryResponse retryResponse = retryService.initiateRetry(input.getTransactionId(),
                                                retryRequest);

                                // Fetch updated exception and retry attempt
                                InterfaceException exception = exceptionRepository
                                                .findByTransactionId(input.getTransactionId())
                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                "Exception not found after retry initiation"));

                                RetryAttempt retryAttempt = retryAttemptRepository.findById(retryResponse.getRetryId())
                                                .orElse(null);

                                log.info("GraphQL retry exception completed for transaction: {}, retry ID: {}",
                                                input.getTransactionId(), retryResponse.getRetryId());

                                return RetryExceptionResult.builder()
                                                .success(true)
                                                .exception(exception)
                                                .retryAttempt(retryAttempt)
                                                .errors(List.of())
                                                .build();

                        } catch (IllegalArgumentException e) {
                                log.warn("Retry validation failed for transaction: {}, error: {}",
                                                input.getTransactionId(), e.getMessage());

                                return RetryExceptionResult.builder()
                                                .success(false)
                                                .errors(List.of(GraphQLError.builder()
                                                                .message(e.getMessage())
                                                                .code("EXCEPTION_NOT_FOUND")
                                                                .build()))
                                                .build();

                        } catch (Exception e) {
                                log.error("GraphQL retry exception failed for transaction: {}, error: {}",
                                                input.getTransactionId(), e.getMessage(), e);

                                return RetryExceptionResult.builder()
                                                .success(false)
                                                .errors(List.of(GraphQLError.builder()
                                                                .message("Retry operation failed: " + e.getMessage())
                                                                .code("RETRY_OPERATION_FAILED")
                                                                .build()))
                                                .build();
                        }
                });
        }

        /**
         * Initiates retry operations for multiple exceptions in bulk.
         * Processes each retry individually using the same business logic as REST API.
         *
         * @param input          the bulk retry input containing transaction IDs and
         *                       retry details
         * @param authentication the current user authentication
         * @return CompletableFuture containing the bulk retry result
         */
        @MutationMapping
        @PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
        public CompletableFuture<BulkRetryResult> bulkRetryExceptions(
                        @Argument BulkRetryInput input,
                        Authentication authentication) {

                log.info("GraphQL bulk retry requested for {} exceptions by user: {}",
                                input.getTransactionIds().size(), authentication.getName());

                return CompletableFuture.supplyAsync(() -> {
                        List<RetryExceptionResult> results = new ArrayList<>();
                        int successCount = 0;
                        int failureCount = 0;

                        for (String transactionId : input.getTransactionIds()) {
                                try {
                                        RetryExceptionInput singleInput = RetryExceptionInput.builder()
                                                        .transactionId(transactionId)
                                                        .reason(input.getReason())
                                                        .priority(input.getPriority())
                                                        .build();

                                        // Process each retry using the same logic as single retry
                                        RetryExceptionResult result = retryException(singleInput, authentication)
                                                        .join();
                                        results.add(result);

                                        if (result.isSuccess()) {
                                                successCount++;
                                        } else {
                                                failureCount++;
                                        }

                                } catch (Exception e) {
                                        log.error("Bulk retry failed for transaction: {}, error: {}",
                                                        transactionId, e.getMessage());

                                        RetryExceptionResult failedResult = RetryExceptionResult.builder()
                                                        .success(false)
                                                        .errors(List.of(GraphQLError.builder()
                                                                        .message("Failed to process retry for transaction: "
                                                                                        + transactionId)
                                                                        .code("BULK_RETRY_ITEM_FAILED")
                                                                        .build()))
                                                        .build();

                                        results.add(failedResult);
                                        failureCount++;
                                }
                        }

                        log.info("GraphQL bulk retry completed: {} successful, {} failed", successCount, failureCount);

                        return BulkRetryResult.builder()
                                        .successCount(successCount)
                                        .failureCount(failureCount)
                                        .results(results)
                                        .errors(List.of())
                                        .build();
                });
        }

        /**
         * Cancels a pending retry operation.
         * Uses the same business logic as the REST API endpoint.
         *
         * @param transactionId  the transaction ID of the exception
         * @param reason         the reason for cancellation
         * @param authentication the current user authentication
         * @return CompletableFuture containing the cancel result
         */
        @MutationMapping
        @PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
        public CompletableFuture<CancelRetryResult> cancelRetry(
                        @Argument String transactionId,
                        @Argument String reason,
                        Authentication authentication) {

                log.info("GraphQL cancel retry requested for transaction: {} by user: {}",
                                transactionId, authentication.getName());

                return CompletableFuture.supplyAsync(() -> {
                        try {
                                // Find the latest pending retry attempt (same logic as REST API)
                                InterfaceException exception = exceptionRepository.findByTransactionId(transactionId)
                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                "Exception not found: " + transactionId));

                                Optional<RetryAttempt> latestAttempt = retryAttemptRepository
                                                .findTopByInterfaceExceptionOrderByAttemptNumberDesc(exception);

                                if (latestAttempt.isEmpty()) {
                                        return CancelRetryResult.builder()
                                                        .success(false)
                                                        .errors(List.of(GraphQLError.builder()
                                                                        .message("No retry attempts found for this exception")
                                                                        .code("NO_RETRY_FOUND")
                                                                        .build()))
                                                        .build();
                                }

                                // Cancel the retry through existing service (same as REST API)
                                boolean cancelled = retryService.cancelRetry(transactionId,
                                                latestAttempt.get().getAttemptNumber());

                                if (cancelled) {
                                        // Fetch updated exception
                                        exception = exceptionRepository.findByTransactionId(transactionId)
                                                        .orElseThrow(() -> new IllegalArgumentException(
                                                                        "Exception not found after cancellation"));

                                        log.info("GraphQL cancel retry completed for transaction: {}, success: true",
                                                        transactionId);

                                        return CancelRetryResult.builder()
                                                        .success(true)
                                                        .exception(exception)
                                                        .errors(List.of())
                                                        .build();
                                } else {
                                        return CancelRetryResult.builder()
                                                        .success(false)
                                                        .errors(List.of(GraphQLError.builder()
                                                                        .message("Unable to cancel retry - it may have already completed or been cancelled")
                                                                        .code("CANCEL_NOT_ALLOWED")
                                                                        .build()))
                                                        .build();
                                }

                        } catch (IllegalArgumentException e) {
                                log.warn("Cancel retry failed - exception not found: {}", transactionId);

                                return CancelRetryResult.builder()
                                                .success(false)
                                                .errors(List.of(GraphQLError.builder()
                                                                .message(e.getMessage())
                                                                .code("EXCEPTION_NOT_FOUND")
                                                                .build()))
                                                .build();

                        } catch (Exception e) {
                                log.error("GraphQL cancel retry failed for transaction: {}, error: {}",
                                                transactionId, e.getMessage(), e);

                                return CancelRetryResult.builder()
                                                .success(false)
                                                .errors(List.of(GraphQLError.builder()
                                                                .message("Cancel retry operation failed: "
                                                                                + e.getMessage())
                                                                .code("CANCEL_RETRY_OPERATION_FAILED")
                                                                .build()))
                                                .build();
                        }
                });
        }

        /**
         * Acknowledges a single exception with proper validation and audit logging.
         * Uses the same business logic as the REST API endpoint.
         *
         * @param input          the acknowledgment input containing transaction ID and
         *                       acknowledgment details
         * @param authentication the current user authentication
         * @return CompletableFuture containing the acknowledgment result
         */
        @MutationMapping
        @PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
        public CompletableFuture<AcknowledgeExceptionResult> acknowledgeException(
                        @Argument AcknowledgeExceptionInput input,
                        Authentication authentication) {

                log.info("GraphQL acknowledge exception requested for transaction: {} by user: {}",
                                input.getTransactionId(), authentication.getName());

                return CompletableFuture.supplyAsync(() -> {
                        try {
                                // Check if exception can be acknowledged (same logic as REST API)
                                if (!exceptionManagementService.canAcknowledge(input.getTransactionId())) {
                                        log.warn("Exception cannot be acknowledged - transaction: {}",
                                                        input.getTransactionId());
                                        return AcknowledgeExceptionResult.builder()
                                                        .success(false)
                                                        .errors(List.of(GraphQLError.builder()
                                                                        .message("Exception cannot be acknowledged (not found, already resolved, or closed)")
                                                                        .code("ACKNOWLEDGMENT_NOT_ALLOWED")
                                                                        .build()))
                                                        .build();
                                }

                                // Convert GraphQL input to service request (same as REST API)
                                AcknowledgeRequest acknowledgeRequest = AcknowledgeRequest.builder()
                                                .acknowledgedBy(authentication.getName())
                                                .notes(buildAcknowledgmentNotes(input))
                                                .build();

                                // Execute acknowledgment through existing service (same as REST API)
                                AcknowledgeResponse acknowledgeResponse = exceptionManagementService
                                                .acknowledgeException(input.getTransactionId(), acknowledgeRequest);

                                // Fetch updated exception
                                InterfaceException exception = exceptionRepository
                                                .findByTransactionId(input.getTransactionId())
                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                "Exception not found after acknowledgment"));

                                log.info("GraphQL acknowledge exception completed for transaction: {}, acknowledged by: {}, at: {}",
                                                input.getTransactionId(), acknowledgeResponse.getAcknowledgedBy(),
                                                acknowledgeResponse.getAcknowledgedAt());

                                return AcknowledgeExceptionResult.builder()
                                                .success(true)
                                                .exception(exception)
                                                .errors(List.of())
                                                .build();

                        } catch (IllegalArgumentException e) {
                                log.warn("Acknowledgment validation failed for transaction: {}, error: {}",
                                                input.getTransactionId(), e.getMessage());

                                return AcknowledgeExceptionResult.builder()
                                                .success(false)
                                                .errors(List.of(GraphQLError.builder()
                                                                .message(e.getMessage())
                                                                .code("EXCEPTION_NOT_FOUND")
                                                                .build()))
                                                .build();

                        } catch (Exception e) {
                                log.error("GraphQL acknowledge exception failed for transaction: {}, error: {}",
                                                input.getTransactionId(), e.getMessage(), e);

                                return AcknowledgeExceptionResult.builder()
                                                .success(false)
                                                .errors(List.of(GraphQLError.builder()
                                                                .message("Acknowledgment operation failed: "
                                                                                + e.getMessage())
                                                                .code("ACKNOWLEDGMENT_OPERATION_FAILED")
                                                                .build()))
                                                .build();
                        }
                });
        }

        /**
         * Acknowledges multiple exceptions in bulk with individual error handling.
         * Processes each acknowledgment individually using the same business logic as
         * REST API.
         *
         * @param input          the bulk acknowledgment input containing transaction
         *                       IDs and acknowledgment details
         * @param authentication the current user authentication
         * @return CompletableFuture containing the bulk acknowledgment result
         */
        @MutationMapping
        @PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
        public CompletableFuture<BulkAcknowledgeResult> bulkAcknowledgeExceptions(
                        @Argument BulkAcknowledgeInput input,
                        Authentication authentication) {

                log.info("GraphQL bulk acknowledge requested for {} exceptions by user: {}",
                                input.getTransactionIds().size(), authentication.getName());

                return CompletableFuture.supplyAsync(() -> {
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

                                        // Process each acknowledgment using the same logic as single acknowledgment
                                        AcknowledgeExceptionResult result = acknowledgeException(singleInput,
                                                        authentication).join();
                                        results.add(result);

                                        if (result.isSuccess()) {
                                                successCount++;
                                        } else {
                                                failureCount++;
                                        }

                                } catch (Exception e) {
                                        log.error("Bulk acknowledgment failed for transaction: {}, error: {}",
                                                        transactionId, e.getMessage());

                                        AcknowledgeExceptionResult failedResult = AcknowledgeExceptionResult.builder()
                                                        .success(false)
                                                        .errors(List.of(GraphQLError.builder()
                                                                        .message("Failed to process acknowledgment for transaction: "
                                                                                        + transactionId)
                                                                        .code("BULK_ACKNOWLEDGMENT_ITEM_FAILED")
                                                                        .build()))
                                                        .build();

                                        results.add(failedResult);
                                        failureCount++;
                                }
                        }

                        log.info("GraphQL bulk acknowledge completed: {} successful, {} failed", successCount,
                                        failureCount);

                        return BulkAcknowledgeResult.builder()
                                        .successCount(successCount)
                                        .failureCount(failureCount)
                                        .results(results)
                                        .errors(List.of())
                                        .build();
                });
        }

        /**
         * Resolves a single exception with proper validation and audit logging.
         * Uses the same business logic as the REST API endpoint.
         *
         * @param input          the resolution input containing transaction ID and
         *                       resolution details
         * @param authentication the current user authentication
         * @return CompletableFuture containing the resolution result
         */
        @MutationMapping
        @PreAuthorize("hasRole('OPERATIONS') or hasRole('ADMIN')")
        public CompletableFuture<ResolveExceptionResult> resolveException(
                        @Argument ResolveExceptionInput input,
                        Authentication authentication) {

                log.info("GraphQL resolve exception requested for transaction: {} by user: {}",
                                input.getTransactionId(), authentication.getName());

                return CompletableFuture.supplyAsync(() -> {
                        try {
                                // Check if exception can be resolved (same logic as REST API)
                                if (!exceptionManagementService.canResolve(input.getTransactionId())) {
                                        log.warn("Exception cannot be resolved - transaction: {}",
                                                        input.getTransactionId());
                                        return ResolveExceptionResult.builder()
                                                        .success(false)
                                                        .errors(List.of(GraphQLError.builder()
                                                                        .message("Exception cannot be resolved (not found, already resolved, or closed)")
                                                                        .code("RESOLUTION_NOT_ALLOWED")
                                                                        .build()))
                                                        .build();
                                }

                                // Convert GraphQL input to service request (same as REST API)
                                ResolveRequest resolveRequest = ResolveRequest.builder()
                                                .resolvedBy(authentication.getName())
                                                .resolutionMethod(input.getResolutionMethod())
                                                .resolutionNotes(input.getResolutionNotes())
                                                .build();

                                // Execute resolution through existing service (same as REST API)
                                ResolveResponse resolveResponse = exceptionManagementService
                                                .resolveException(input.getTransactionId(), resolveRequest);

                                // Fetch updated exception
                                InterfaceException exception = exceptionRepository
                                                .findByTransactionId(input.getTransactionId())
                                                .orElseThrow(() -> new IllegalArgumentException(
                                                                "Exception not found after resolution"));

                                log.info("GraphQL resolve exception completed for transaction: {}, resolved by: {}, method: {}, at: {}",
                                                input.getTransactionId(), resolveResponse.getResolvedBy(),
                                                resolveResponse.getResolutionMethod(), resolveResponse.getResolvedAt());

                                return ResolveExceptionResult.builder()
                                                .success(true)
                                                .exception(exception)
                                                .errors(List.of())
                                                .build();

                        } catch (IllegalArgumentException e) {
                                log.warn("Resolution validation failed for transaction: {}, error: {}",
                                                input.getTransactionId(), e.getMessage());

                                return ResolveExceptionResult.builder()
                                                .success(false)
                                                .errors(List.of(GraphQLError.builder()
                                                                .message(e.getMessage())
                                                                .code("EXCEPTION_NOT_FOUND")
                                                                .build()))
                                                .build();

                        } catch (Exception e) {
                                log.error("GraphQL resolve exception failed for transaction: {}, error: {}",
                                                input.getTransactionId(), e.getMessage(), e);

                                return ResolveExceptionResult.builder()
                                                .success(false)
                                                .errors(List.of(GraphQLError.builder()
                                                                .message("Resolution operation failed: "
                                                                                + e.getMessage())
                                                                .code("RESOLUTION_OPERATION_FAILED")
                                                                .build()))
                                                .build();
                        }
                });
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