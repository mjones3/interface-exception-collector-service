package com.arcone.biopro.exception.collector.api.graphql.resolver;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.api.graphql.service.GraphQLSecurityService;
import com.arcone.biopro.exception.collector.api.graphql.util.DataLoaderUtil;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.entity.StatusChange;
import graphql.GraphQLException;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * GraphQL field resolver for nested exception data.
 * Demonstrates the use of DataLoaders to efficiently load related data
 * and prevent N+1 query problems in GraphQL resolvers.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ExceptionFieldResolver {

        private final GraphQLSecurityService securityService;

        /**
         * Resolves the original payload field for an exception using DataLoader.
         * This method is called when the originalPayload field is requested
         * in a GraphQL query for an Exception type.
         * 
         * Security: Requires OPERATIONS or ADMIN role and payload access permissions.
         *
         * @param exception   the parent exception object
         * @param environment the GraphQL data fetching environment
         * @return CompletableFuture containing the payload response
         */
        @SchemaMapping(typeName = "Exception", field = "originalPayload")
        @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
        public CompletableFuture<PayloadResponse> originalPayload(
                        InterfaceException exception,
                        DataFetchingEnvironment environment) {

                if (exception == null) {
                        log.warn("Attempted to resolve payload for null exception");
                        return CompletableFuture.failedFuture(
                                        new GraphQLException("Exception cannot be null"));
                }

                if (exception.getTransactionId() == null || exception.getTransactionId().trim().isEmpty()) {
                        log.warn("Attempted to resolve payload for exception with null/empty transaction ID");
                        return CompletableFuture.failedFuture(
                                        new GraphQLException("Exception transaction ID cannot be null or empty"));
                }

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                // Check field-level security
                if (!securityService.canViewPayload(exception, authentication)) {
                        log.warn("User {} denied access to payload for transaction {}",
                                        authentication != null ? authentication.getName() : "anonymous",
                                        exception.getTransactionId());
                        return CompletableFuture.failedFuture(
                                        new GraphQLException(
                                                        "Access denied: Insufficient privileges to view payload data"));
                }

                log.debug("Resolving original payload for exception: {} (user: {})",
                                exception.getTransactionId(),
                                authentication != null ? authentication.getName() : "anonymous");

                // Use DataLoader to batch and cache payload retrieval
                return DataLoaderUtil.loadPayload(environment, exception.getTransactionId())
                                .whenComplete((result, throwable) -> {
                                        if (throwable != null) {
                                                log.error("Error loading payload for transaction {}: {}",
                                                                exception.getTransactionId(), throwable.getMessage());
                                        } else {
                                                log.debug("Successfully loaded payload for transaction: {}, retrieved: {}",
                                                                exception.getTransactionId(),
                                                                result != null ? result.isRetrieved() : false);
                                        }
                                })
                                .exceptionally(throwable -> {
                                        log.error("Failed to load payload for transaction {}: {}",
                                                        exception.getTransactionId(), throwable.getMessage());
                                        return PayloadResponse.builder()
                                                        .transactionId(exception.getTransactionId())
                                                        .retrieved(false)
                                                        .errorMessage("Failed to retrieve payload: "
                                                                        + throwable.getMessage())
                                                        .build();
                                });
        }

        /**
         * Resolves the retry history field for an exception using DataLoader.
         * This method is called when the retryHistory field is requested
         * in a GraphQL query for an Exception type.
         *
         * @param exception   the parent exception object
         * @param environment the GraphQL data fetching environment
         * @return CompletableFuture containing the list of retry attempts
         */
        @SchemaMapping(typeName = "Exception", field = "retryHistory")
        public CompletableFuture<List<RetryAttempt>> retryHistory(
                        InterfaceException exception,
                        DataFetchingEnvironment environment) {

                if (exception == null) {
                        log.warn("Attempted to resolve retry history for null exception");
                        return CompletableFuture.failedFuture(
                                        new GraphQLException("Exception cannot be null"));
                }

                if (exception.getTransactionId() == null || exception.getTransactionId().trim().isEmpty()) {
                        log.warn("Attempted to resolve retry history for exception with null/empty transaction ID");
                        return CompletableFuture.completedFuture(List.of());
                }

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                // Check field-level security
                if (!securityService.canViewRetryHistory(exception, authentication)) {
                        log.warn("User {} denied access to retry history for transaction {}",
                                        authentication != null ? authentication.getName() : "anonymous",
                                        exception.getTransactionId());
                        return CompletableFuture.failedFuture(
                                        new GraphQLException(
                                                        "Access denied: Insufficient privileges to view retry history"));
                }

                log.debug("Resolving retry history for exception: {} (user: {})",
                                exception.getTransactionId(),
                                authentication != null ? authentication.getName() : "anonymous");

                // Use DataLoader to batch and cache retry history retrieval
                return DataLoaderUtil.loadRetryHistory(environment, exception.getTransactionId())
                                .whenComplete((result, throwable) -> {
                                        if (throwable != null) {
                                                log.error("Error loading retry history for transaction {}: {}",
                                                                exception.getTransactionId(), throwable.getMessage());
                                        } else {
                                                log.debug("Successfully loaded retry history for transaction: {}, count: {}",
                                                                exception.getTransactionId(),
                                                                result != null ? result.size() : 0);
                                        }
                                })
                                .exceptionally(throwable -> {
                                        log.error("Failed to load retry history for transaction {}: {}",
                                                        exception.getTransactionId(), throwable.getMessage());
                                        return List.of(); // Return empty list on error
                                });
        }

        /**
         * Resolves the retry count field dynamically based on retry history.
         * This demonstrates how to use DataLoader results for computed fields.
         *
         * @param exception   the parent exception object
         * @param environment the GraphQL data fetching environment
         * @return CompletableFuture containing the retry count
         */
        @SchemaMapping(typeName = "Exception", field = "actualRetryCount")
        public CompletableFuture<Integer> actualRetryCount(
                        InterfaceException exception,
                        DataFetchingEnvironment environment) {

                log.debug("Resolving actual retry count for exception: {}", exception.getTransactionId());

                // Use DataLoader to get retry history and compute count
                return DataLoaderUtil.loadRetryHistory(environment, exception.getTransactionId())
                                .thenApply(retryAttempts -> {
                                        int count = retryAttempts != null ? retryAttempts.size() : 0;
                                        log.debug("Actual retry count for transaction {}: {}",
                                                        exception.getTransactionId(), count);
                                        return count;
                                })
                                .exceptionally(throwable -> {
                                        log.error("Error computing retry count for transaction {}: {}",
                                                        exception.getTransactionId(), throwable.getMessage());
                                        return 0; // Return 0 as fallback
                                });
        }

        /**
         * Resolves whether an exception has any successful retries.
         * This demonstrates complex field resolution using DataLoader data.
         *
         * @param exception   the parent exception object
         * @param environment the GraphQL data fetching environment
         * @return CompletableFuture containing true if there are successful retries
         */
        @SchemaMapping(typeName = "Exception", field = "hasSuccessfulRetries")
        public CompletableFuture<Boolean> hasSuccessfulRetries(
                        InterfaceException exception,
                        DataFetchingEnvironment environment) {

                log.debug("Checking for successful retries for exception: {}", exception.getTransactionId());

                return DataLoaderUtil.loadRetryHistory(environment, exception.getTransactionId())
                                .thenApply(retryAttempts -> {
                                        if (retryAttempts == null || retryAttempts.isEmpty()) {
                                                return false;
                                        }

                                        boolean hasSuccessful = retryAttempts.stream()
                                                        .anyMatch(retry -> retry.getResultSuccess() != null
                                                                        && retry.getResultSuccess());

                                        log.debug("Exception {} has successful retries: {}",
                                                        exception.getTransactionId(), hasSuccessful);

                                        return hasSuccessful;
                                })
                                .exceptionally(throwable -> {
                                        log.error("Error checking successful retries for transaction {}: {}",
                                                        exception.getTransactionId(), throwable.getMessage());
                                        return false; // Return false as fallback
                                });
        }

        /**
         * Resolves the status history field for an exception using DataLoader.
         * This method is called when the statusHistory field is requested
         * in a GraphQL query for an Exception type.
         * 
         * Provides audit trail of all status changes for the exception.
         *
         * @param exception   the parent exception object
         * @param environment the GraphQL data fetching environment
         * @return CompletableFuture containing the list of status changes
         */
        @SchemaMapping(typeName = "Exception", field = "statusHistory")
        public CompletableFuture<List<StatusChange>> statusHistory(
                        InterfaceException exception,
                        DataFetchingEnvironment environment) {

                if (exception == null) {
                        log.warn("Attempted to resolve status history for null exception");
                        return CompletableFuture.failedFuture(
                                        new GraphQLException("Exception cannot be null"));
                }

                if (exception.getTransactionId() == null || exception.getTransactionId().trim().isEmpty()) {
                        log.warn("Attempted to resolve status history for exception with null/empty transaction ID");
                        return CompletableFuture.completedFuture(List.of());
                }

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                // Check field-level security
                if (!securityService.canViewStatusHistory(exception, authentication)) {
                        log.warn("User {} denied access to status history for transaction {}",
                                        authentication != null ? authentication.getName() : "anonymous",
                                        exception.getTransactionId());
                        return CompletableFuture.failedFuture(
                                        new GraphQLException(
                                                        "Access denied: Insufficient privileges to view status history"));
                }

                log.debug("Resolving status history for exception: {} (user: {})",
                                exception.getTransactionId(),
                                authentication != null ? authentication.getName() : "anonymous");

                // Use DataLoader to batch and cache status history retrieval
                return DataLoaderUtil.loadStatusHistory(environment, exception.getTransactionId())
                                .whenComplete((result, throwable) -> {
                                        if (throwable != null) {
                                                log.error("Error loading status history for transaction {}: {}",
                                                                exception.getTransactionId(), throwable.getMessage());
                                        } else {
                                                log.debug("Successfully loaded status history for transaction: {}, count: {}",
                                                                exception.getTransactionId(),
                                                                result != null ? result.size() : 0);
                                        }
                                })
                                .exceptionally(throwable -> {
                                        log.error("Failed to load status history for transaction {}: {}",
                                                        exception.getTransactionId(), throwable.getMessage());
                                        return List.of(); // Return empty list on error
                                });
        }
}