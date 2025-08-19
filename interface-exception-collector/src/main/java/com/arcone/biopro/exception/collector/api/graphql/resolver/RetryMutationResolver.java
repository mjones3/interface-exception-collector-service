package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.RetryExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkRetryInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkRetryResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.CancelRetryResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.AcknowledgeExceptionResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkAcknowledgeInput;
import com.arcone.biopro.exception.collector.api.graphql.dto.BulkAcknowledgeResult;
import com.arcone.biopro.exception.collector.api.graphql.dto.GraphQLError;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLRetryService;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLAcknowledgmentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * GraphQL resolver for retry and acknowledgment operation mutations.
 * Handles retry, bulk retry, cancel retry, acknowledgment, and bulk
 * acknowledgment operations
 * with proper authentication, validation, and audit logging.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class RetryMutationResolver {

        private final GraphQLRetryService graphQLRetryService;
        private final GraphQLAcknowledgmentService graphQLAcknowledgmentService;

        /**
         * Initiates a retry operation for a single exception.
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

                return graphQLRetryService.retryException(input, authentication)
                                .thenApply(result -> {
                                        log.info("GraphQL retry exception completed for transaction: {}, success: {}",
                                                        input.getTransactionId(), result.isSuccess());
                                        return result;
                                })
                                .exceptionally(throwable -> {
                                        log.error("GraphQL retry exception failed for transaction: {}, error: {}",
                                                        input.getTransactionId(), throwable.getMessage(), throwable);

                                        return RetryExceptionResult.builder()
                                                        .success(false)
                                                        .errors(List.of(GraphQLError.builder()
                                                                        .message("Retry operation failed: "
                                                                                        + throwable.getMessage())
                                                                        .code("RETRY_OPERATION_FAILED")
                                                                        .build()))
                                                        .build();
                                });
        }

        /**
         * Initiates retry operations for multiple exceptions in bulk.
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

                return graphQLRetryService.bulkRetryExceptions(input, authentication)
                                .thenApply(result -> {
                                        log.info("GraphQL bulk retry completed: {} successful, {} failed",
                                                        result.getSuccessCount(), result.getFailureCount());
                                        return result;
                                })
                                .exceptionally(throwable -> {
                                        log.error("GraphQL bulk retry failed, error: {}", throwable.getMessage(),
                                                        throwable);

                                        return BulkRetryResult.builder()
                                                        .successCount(0)
                                                        .failureCount(input.getTransactionIds().size())
                                                        .results(new ArrayList<>())
                                                        .errors(List.of(GraphQLError.builder()
                                                                        .message("Bulk retry operation failed: "
                                                                                        + throwable.getMessage())
                                                                        .code("BULK_RETRY_OPERATION_FAILED")
                                                                        .build()))
                                                        .build();
                                });
        }

        /**
         * Cancels a pending retry operation.
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

                return graphQLRetryService.cancelRetry(transactionId, reason, authentication)
                                .thenApply(result -> {
                                        log.info("GraphQL cancel retry completed for transaction: {}, success: {}",
                                                        transactionId, result.isSuccess());
                                        return result;
                                })
                                .exceptionally(throwable -> {
                                        log.error("GraphQL cancel retry failed for transaction: {}, error: {}",
                                                        transactionId, throwable.getMessage(), throwable);

                                        return CancelRetryResult.builder()
                                                        .success(false)
                                                        .errors(List.of(GraphQLError.builder()
                                                                        .message("Cancel retry operation failed: "
                                                                                        + throwable.getMessage())
                                                                        .code("CANCEL_RETRY_OPERATION_FAILED")
                                                                        .build()))
                                                        .build();
                                });
        }

        /**
         * Acknowledges a single exception with proper validation and audit logging.
         * Implements optimistic locking to handle concurrent updates.
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

                return graphQLAcknowledgmentService.acknowledgeException(input, authentication)
                                .thenApply(result -> {
                                        log.info("GraphQL acknowledge exception completed for transaction: {}, success: {}",
                                                        input.getTransactionId(), result.isSuccess());
                                        return result;
                                })
                                .exceptionally(throwable -> {
                                        log.error("GraphQL acknowledge exception failed for transaction: {}, error: {}",
                                                        input.getTransactionId(), throwable.getMessage(), throwable);

                                        return AcknowledgeExceptionResult.builder()
                                                        .success(false)
                                                        .errors(List.of(GraphQLError.builder()
                                                                        .message("Acknowledgment operation failed: "
                                                                                        + throwable.getMessage())
                                                                        .code("ACKNOWLEDGMENT_OPERATION_FAILED")
                                                                        .build()))
                                                        .build();
                                });
        }

        /**
         * Acknowledges multiple exceptions in bulk with individual error handling.
         * Implements optimistic locking for each individual acknowledgment.
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

                return graphQLAcknowledgmentService.bulkAcknowledgeExceptions(input, authentication)
                                .thenApply(result -> {
                                        log.info("GraphQL bulk acknowledge completed: {} successful, {} failed",
                                                        result.getSuccessCount(), result.getFailureCount());
                                        return result;
                                })
                                .exceptionally(throwable -> {
                                        log.error("GraphQL bulk acknowledge failed, error: {}", throwable.getMessage(),
                                                        throwable);

                                        return BulkAcknowledgeResult.builder()
                                                        .successCount(0)
                                                        .failureCount(input.getTransactionIds().size())
                                                        .results(new ArrayList<>())
                                                        .errors(List.of(GraphQLError.builder()
                                                                        .message("Bulk acknowledgment operation failed: "
                                                                                        + throwable.getMessage())
                                                                        .code("BULK_ACKNOWLEDGMENT_OPERATION_FAILED")
                                                                        .build()))
                                                        .build();
                                });
        }
}