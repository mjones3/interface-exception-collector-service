package com.arcone.biopro.exception.collector.api.graphql.service;

import com.arcone.biopro.exception.collector.api.dto.RetryRequest;
import com.arcone.biopro.exception.collector.api.dto.RetryResponse;
import com.arcone.biopro.exception.collector.api.graphql.dto.*;
import com.arcone.biopro.exception.collector.application.service.RetryService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.exception.ExceptionNotFoundException;
import com.arcone.biopro.exception.collector.domain.exception.RetryNotAllowedException;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * GraphQL-specific retry service that handles retry operations with circuit
 * breaker patterns
 * and proper error handling for GraphQL responses.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GraphQLRetryService {

        private final RetryService retryService;
        private final InterfaceExceptionRepository exceptionRepository;
        private final RetryAttemptRepository retryAttemptRepository;
        private final RetryValidationService validationService;

        /**
         * Retries a single exception with circuit breaker and timeout protection.
         *
         * @param input  the retry input
         * @param userId the user initiating the retry
         * @return CompletableFuture containing the retry result
         */
        @CircuitBreaker(name = "retry-service", fallbackMethod = "retryExceptionFallback")
        @TimeLimiter(name = "retry-service")
        @Retry(name = "retry-service")
        public CompletableFuture<RetryExceptionResult> retryException(RetryExceptionInput input,
                        org.springframework.security.core.Authentication authentication) {
                return CompletableFuture.supplyAsync(() -> {
                        try {
                                log.info("Processing GraphQL retry for transaction: {} by user: {}",
                                                input.getTransactionId(), authentication.getName());

                                // Validate retry request and user permissions
                                validationService.validateRetryRequest(input, authentication);

                                // Convert GraphQL input to service request
                                RetryRequest retryRequest = RetryRequest.builder()
                                                .reason(input.getReason())
                                                .initiatedBy(authentication.getName())
                                                .build();

                                // Execute retry through existing service
                                RetryResponse retryResponse = retryService.initiateRetry(input.getTransactionId(),
                                                retryRequest);

                                // Fetch updated exception and retry attempt
                                InterfaceException exception = exceptionRepository
                                                .findByTransactionId(input.getTransactionId())
                                                .orElseThrow(() -> new ExceptionNotFoundException(
                                                                "Exception not found after retry initiation"));

                                RetryAttempt retryAttempt = retryAttemptRepository.findById(retryResponse.getRetryId())
                                                .orElse(null);

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
                                                                .code("VALIDATION_ERROR")
                                                                .build()))
                                                .build();

                        } catch (RetryNotAllowedException e) {
                                log.warn("Retry not allowed for transaction: {}, error: {}",
                                                input.getTransactionId(), e.getMessage());

                                return RetryExceptionResult.builder()
                                                .success(false)
                                                .errors(List.of(GraphQLError.builder()
                                                                .message(e.getMessage())
                                                                .code("RETRY_NOT_ALLOWED")
                                                                .build()))
                                                .build();

                        } catch (Exception e) {
                                log.error("Retry operation failed for transaction: {}, error: {}",
                                                input.getTransactionId(), e.getMessage(), e);

                                return RetryExceptionResult.builder()
                                                .success(false)
                                                .errors(List.of(GraphQLError.builder()
                                                                .message("Internal error occurred during retry operation")
                                                                .code("INTERNAL_ERROR")
                                                                .build()))
                                                .build();
                        }
                });
        }

        /**
         * Fallback method for retry exception when circuit breaker is open.
         */
        public CompletableFuture<RetryExceptionResult> retryExceptionFallback(
                        RetryExceptionInput input, org.springframework.security.core.Authentication authentication,
                        Exception ex) {

                log.error("Retry service circuit breaker activated for transaction: {}, error: {}",
                                input.getTransactionId(), ex.getMessage());

                return CompletableFuture.completedFuture(
                                RetryExceptionResult.builder()
                                                .success(false)
                                                .errors(List.of(GraphQLError.builder()
                                                                .message("Retry service is temporarily unavailable. Please try again later.")
                                                                .code("SERVICE_UNAVAILABLE")
                                                                .build()))
                                                .build());
        }

        /**
         * Performs bulk retry operations with individual error handling.
         *
         * @param input  the bulk retry input
         * @param userId the user initiating the retries
         * @return CompletableFuture containing the bulk retry result
         */
        @CircuitBreaker(name = "bulk-retry-service", fallbackMethod = "bulkRetryExceptionsFallback")
        @TimeLimiter(name = "bulk-retry-service")
        public CompletableFuture<BulkRetryResult> bulkRetryExceptions(BulkRetryInput input,
                        org.springframework.security.core.Authentication authentication) {
                return CompletableFuture.supplyAsync(() -> {
                        log.info("Processing bulk retry for {} transactions by user: {}",
                                        input.getTransactionIds().size(), authentication.getName());

                        // Validate bulk retry request and user permissions
                        validationService.validateBulkRetryRequest(input.getTransactionIds(), authentication);
                        validationService.validateBulkOperationSize(input.getTransactionIds().size(), authentication);

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

                                        // Process each retry synchronously to avoid overwhelming the system
                                        RetryExceptionResult result = retryException(singleInput, authentication)
                                                        .join();
                                        results.add(result);

                                        if (result.isSuccess()) {
                                                successCount++;
                                        } else {
                                                failureCount++;
                                        }

                                } catch (CompletionException e) {
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

                        log.info("Bulk retry completed: {} successful, {} failed", successCount, failureCount);

                        return BulkRetryResult.builder()
                                        .successCount(successCount)
                                        .failureCount(failureCount)
                                        .results(results)
                                        .errors(List.of())
                                        .build();
                });
        }

        /**
         * Fallback method for bulk retry when circuit breaker is open.
         */
        public CompletableFuture<BulkRetryResult> bulkRetryExceptionsFallback(
                        BulkRetryInput input, org.springframework.security.core.Authentication authentication,
                        Exception ex) {

                log.error("Bulk retry service circuit breaker activated, error: {}", ex.getMessage());

                return CompletableFuture.completedFuture(
                                BulkRetryResult.builder()
                                                .successCount(0)
                                                .failureCount(input.getTransactionIds().size())
                                                .results(List.of())
                                                .errors(List.of(GraphQLError.builder()
                                                                .message("Bulk retry service is temporarily unavailable. Please try again later.")
                                                                .code("SERVICE_UNAVAILABLE")
                                                                .build()))
                                                .build());
        }

        /**
         * Cancels a pending retry operation.
         *
         * @param transactionId the transaction ID
         * @param reason        the cancellation reason
         * @param userId        the user cancelling the retry
         * @return CompletableFuture containing the cancel result
         */
        @CircuitBreaker(name = "cancel-retry-service", fallbackMethod = "cancelRetryFallback")
        @TimeLimiter(name = "cancel-retry-service")
        public CompletableFuture<CancelRetryResult> cancelRetry(String transactionId, String reason,
                        org.springframework.security.core.Authentication authentication) {
                return CompletableFuture.supplyAsync(() -> {
                        try {
                                log.info("Processing cancel retry for transaction: {} by user: {}", transactionId,
                                                authentication.getName());

                                // Validate cancel retry request and user permissions
                                validationService.validateCancelRetryRequest(transactionId, authentication);

                                // Find the latest pending retry attempt
                                InterfaceException exception = exceptionRepository.findByTransactionId(transactionId)
                                                .orElseThrow(() -> new ExceptionNotFoundException(
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

                                // Cancel the retry through existing service
                                boolean cancelled = retryService.cancelRetry(transactionId,
                                                latestAttempt.get().getAttemptNumber());

                                if (cancelled) {
                                        // Fetch updated exception
                                        exception = exceptionRepository.findByTransactionId(transactionId)
                                                        .orElseThrow(() -> new ExceptionNotFoundException(
                                                                        "Exception not found after cancellation"));

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

                        } catch (ExceptionNotFoundException e) {
                                log.warn("Cancel retry failed - exception not found: {}", transactionId);

                                return CancelRetryResult.builder()
                                                .success(false)
                                                .errors(List.of(GraphQLError.builder()
                                                                .message(e.getMessage())
                                                                .code("NOT_FOUND")
                                                                .build()))
                                                .build();

                        } catch (Exception e) {
                                log.error("Cancel retry operation failed for transaction: {}, error: {}",
                                                transactionId, e.getMessage(), e);

                                return CancelRetryResult.builder()
                                                .success(false)
                                                .errors(List.of(GraphQLError.builder()
                                                                .message("Internal error occurred during cancel operation")
                                                                .code("INTERNAL_ERROR")
                                                                .build()))
                                                .build();
                        }
                });
        }

        /**
         * Fallback method for cancel retry when circuit breaker is open.
         */
        public CompletableFuture<CancelRetryResult> cancelRetryFallback(
                        String transactionId, String reason,
                        org.springframework.security.core.Authentication authentication, Exception ex) {

                log.error("Cancel retry service circuit breaker activated for transaction: {}, error: {}",
                                transactionId, ex.getMessage());

                return CompletableFuture.completedFuture(
                                CancelRetryResult.builder()
                                                .success(false)
                                                .errors(List.of(GraphQLError.builder()
                                                                .message("Cancel retry service is temporarily unavailable. Please try again later.")
                                                                .code("SERVICE_UNAVAILABLE")
                                                                .build()))
                                                .build());
        }

}