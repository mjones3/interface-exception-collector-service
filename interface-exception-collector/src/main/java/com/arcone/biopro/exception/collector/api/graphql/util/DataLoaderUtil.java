package com.arcone.biopro.exception.collector.api.graphql.util;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.api.graphql.config.DataLoaderConfig;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.entity.StatusChange;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.springframework.stereotype.Component;
import graphql.schema.DataFetchingEnvironment;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for accessing DataLoaders in GraphQL resolvers.
 * Provides type-safe access to DataLoader instances and handles
 * common DataLoader operations.
 */
@Component
@Slf4j
public class DataLoaderUtil {

    /**
     * Gets the exception DataLoader from the GraphQL context.
     *
     * @param environment the GraphQL data fetching environment
     * @return DataLoader for loading exceptions by transaction ID
     */
    public static DataLoader<String, InterfaceException> getExceptionLoader(DataFetchingEnvironment environment) {
        return environment.getDataLoader(DataLoaderConfig.EXCEPTION_LOADER);
    }

    /**
     * Gets the payload DataLoader from the GraphQL context.
     *
     * @param environment the GraphQL data fetching environment
     * @return DataLoader for loading payloads by transaction ID
     */
    public static DataLoader<String, PayloadResponse> getPayloadLoader(DataFetchingEnvironment environment) {
        return environment.getDataLoader(DataLoaderConfig.PAYLOAD_LOADER);
    }

    /**
     * Gets the retry history DataLoader from the GraphQL context.
     *
     * @param environment the GraphQL data fetching environment
     * @return DataLoader for loading retry history by transaction ID
     */
    public static DataLoader<String, List<RetryAttempt>> getRetryHistoryLoader(DataFetchingEnvironment environment) {
        return environment.getDataLoader(DataLoaderConfig.RETRY_HISTORY_LOADER);
    }

    /**
     * Gets the status change history DataLoader from the GraphQL context.
     *
     * @param environment the GraphQL data fetching environment
     * @return DataLoader for loading status change history by transaction ID
     */
    public static DataLoader<String, List<StatusChange>> getStatusChangeLoader(DataFetchingEnvironment environment) {
        return environment.getDataLoader(DataLoaderConfig.STATUS_CHANGE_LOADER);
    }

    /**
     * Loads an exception by transaction ID using the DataLoader.
     *
     * @param environment   the GraphQL data fetching environment
     * @param transactionId the transaction ID to load
     * @return CompletableFuture containing the exception or null if not found
     */
    public static CompletableFuture<InterfaceException> loadException(
            DataFetchingEnvironment environment, String transactionId) {

        if (transactionId == null || transactionId.trim().isEmpty()) {
            log.warn("Attempted to load exception with null or empty transaction ID");
            return CompletableFuture.completedFuture(null);
        }

        DataLoader<String, InterfaceException> loader = getExceptionLoader(environment);
        if (loader == null) {
            log.error("Exception DataLoader not found in GraphQL context");
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Exception DataLoader not available"));
        }

        return loader.load(transactionId);
    }

    /**
     * Loads a payload by transaction ID using the DataLoader.
     *
     * @param environment   the GraphQL data fetching environment
     * @param transactionId the transaction ID to load payload for
     * @return CompletableFuture containing the payload response
     */
    public static CompletableFuture<PayloadResponse> loadPayload(
            DataFetchingEnvironment environment, String transactionId) {

        if (transactionId == null || transactionId.trim().isEmpty()) {
            log.warn("Attempted to load payload with null or empty transaction ID");
            return CompletableFuture.completedFuture(
                    PayloadResponse.builder()
                            .transactionId(transactionId)
                            .retrieved(false)
                            .errorMessage("Invalid transaction ID")
                            .build());
        }

        DataLoader<String, PayloadResponse> loader = getPayloadLoader(environment);
        if (loader == null) {
            log.error("Payload DataLoader not found in GraphQL context");
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Payload DataLoader not available"));
        }

        return loader.load(transactionId);
    }

    /**
     * Loads retry history by transaction ID using the DataLoader.
     *
     * @param environment   the GraphQL data fetching environment
     * @param transactionId the transaction ID to load retry history for
     * @return CompletableFuture containing the list of retry attempts
     */
    public static CompletableFuture<List<RetryAttempt>> loadRetryHistory(
            DataFetchingEnvironment environment, String transactionId) {

        if (transactionId == null || transactionId.trim().isEmpty()) {
            log.warn("Attempted to load retry history with null or empty transaction ID");
            return CompletableFuture.completedFuture(List.of());
        }

        DataLoader<String, List<RetryAttempt>> loader = getRetryHistoryLoader(environment);
        if (loader == null) {
            log.error("Retry history DataLoader not found in GraphQL context");
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Retry history DataLoader not available"));
        }

        return loader.load(transactionId);
    }

    /**
     * Loads status change history by transaction ID using the DataLoader.
     *
     * @param environment   the GraphQL data fetching environment
     * @param transactionId the transaction ID to load status change history for
     * @return CompletableFuture containing the list of status changes
     */
    public static CompletableFuture<List<StatusChange>> loadStatusHistory(
            DataFetchingEnvironment environment, String transactionId) {

        if (transactionId == null || transactionId.trim().isEmpty()) {
            log.warn("Attempted to load status history with null or empty transaction ID");
            return CompletableFuture.completedFuture(List.of());
        }

        DataLoader<String, List<StatusChange>> loader = getStatusChangeLoader(environment);
        if (loader == null) {
            log.error("Status change DataLoader not found in GraphQL context");
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Status change DataLoader not available"));
        }

        return loader.load(transactionId);
    }

    /**
     * Checks if all required DataLoaders are available in the GraphQL context.
     *
     * @param environment the GraphQL data fetching environment
     * @return true if all DataLoaders are available, false otherwise
     */
    public static boolean areDataLoadersAvailable(DataFetchingEnvironment environment) {
        try {
            return getExceptionLoader(environment) != null &&
                    getPayloadLoader(environment) != null &&
                    getRetryHistoryLoader(environment) != null &&
                    getStatusChangeLoader(environment) != null;
        } catch (Exception e) {
            log.error("Error checking DataLoader availability", e);
            return false;
        }
    }

    /**
     * Logs DataLoader statistics for monitoring and debugging.
     *
     * @param environment the GraphQL data fetching environment
     */
    public static void logDataLoaderStats(DataFetchingEnvironment environment) {
        try {
            DataLoader<String, InterfaceException> exceptionLoader = getExceptionLoader(environment);
            DataLoader<String, PayloadResponse> payloadLoader = getPayloadLoader(environment);
            DataLoader<String, List<RetryAttempt>> retryLoader = getRetryHistoryLoader(environment);
            DataLoader<String, List<StatusChange>> statusLoader = getStatusChangeLoader(environment);

            log.debug("DataLoader stats - Exception: {}, Payload: {}, Retry: {}, Status: {}",
                    exceptionLoader != null ? "available" : "missing",
                    payloadLoader != null ? "available" : "missing",
                    retryLoader != null ? "available" : "missing",
                    statusLoader != null ? "available" : "missing");
        } catch (Exception e) {
            log.debug("Could not log DataLoader stats", e);
        }
    }
}