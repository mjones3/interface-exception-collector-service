package com.arcone.biopro.exception.collector.api.graphql.dataloader;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.MappedBatchLoader;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * DataLoader for batching retry history queries by transaction ID.
 * Implements the DataLoader pattern to efficiently load retry attempts
 * for multiple exceptions in a single batch operation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RetryHistoryDataLoader implements MappedBatchLoader<String, List<RetryAttempt>> {

    private final RetryAttemptRepository retryAttemptRepository;
    private final InterfaceExceptionRepository exceptionRepository;

    /**
     * Batch loads retry history by transaction IDs.
     * This method first resolves the transaction IDs to exception entities,
     * then batches the retry attempt queries to minimize database calls.
     *
     * @param transactionIds Set of transaction IDs to load retry history for
     * @return CompletionStage containing a map of transaction ID to List of
     *         RetryAttempt
     */
    @Override
    public CompletionStage<Map<String, List<RetryAttempt>>> load(Set<String> transactionIds) {
        if (transactionIds == null || transactionIds.isEmpty()) {
            log.debug("No transaction IDs provided for retry history batch loading");
            return CompletableFuture.completedFuture(Map.of());
        }

        log.debug("Batch loading retry history for {} transaction IDs", transactionIds.size());

        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            try {
                // Validate batch size
                if (transactionIds.size() > 500) {
                    log.warn(
                            "Large retry history batch size detected: {} transaction IDs. Consider reducing batch size.",
                            transactionIds.size());
                }

                // First, get the exceptions to get their entity IDs
                List<InterfaceException> exceptions = exceptionRepository.findByTransactionIdIn(transactionIds);

                long queryTime = System.currentTimeMillis() - startTime;
                log.debug("Found {} exceptions for retry history loading in {}ms", exceptions.size(), queryTime);

                // Initialize empty lists for all requested transaction IDs
                Map<String, List<RetryAttempt>> retryHistoryMap = transactionIds.stream()
                        .collect(Collectors.toMap(
                                transactionId -> transactionId,
                                transactionId -> List.<RetryAttempt>of()));

                if (exceptions.isEmpty()) {
                    log.debug("No exceptions found for retry history loading, returning empty results");
                    return retryHistoryMap;
                }

                // Get all retry attempts for these exceptions in a single query
                List<RetryAttempt> allRetryAttempts = retryAttemptRepository.findByInterfaceExceptionIn(exceptions);

                long retryQueryTime = System.currentTimeMillis() - startTime - queryTime;
                log.debug("Found {} retry attempts for {} exceptions in {}ms",
                        allRetryAttempts.size(), exceptions.size(), retryQueryTime);

                // Group retry attempts by their exception's transaction ID
                Map<String, List<RetryAttempt>> groupedRetries = allRetryAttempts.stream()
                        .collect(Collectors.groupingBy(
                                retry -> retry.getInterfaceException().getTransactionId(),
                                Collectors.toList()));

                // Sort retry attempts by attempt number for each transaction and update the map
                for (Map.Entry<String, List<RetryAttempt>> entry : groupedRetries.entrySet()) {
                    List<RetryAttempt> sortedRetries = entry.getValue().stream()
                            .sorted((r1, r2) -> r1.getAttemptNumber().compareTo(r2.getAttemptNumber()))
                            .collect(Collectors.toList());

                    retryHistoryMap.put(entry.getKey(), sortedRetries);

                    log.debug("Loaded {} retry attempts for transaction ID: {}",
                            sortedRetries.size(), entry.getKey());
                }

                // Log summary
                long totalRetries = retryHistoryMap.values().stream()
                        .mapToLong(List::size)
                        .sum();

                long totalTime = System.currentTimeMillis() - startTime;
                log.debug("Completed batch retry history loading in {}ms: {} transaction IDs, {} total retry attempts",
                        totalTime, transactionIds.size(), totalRetries);

                return retryHistoryMap;

            } catch (Exception e) {
                long totalTime = System.currentTimeMillis() - startTime;
                log.error("Error in batch retry history loading for transaction IDs after {}ms: {}",
                        totalTime, transactionIds, e);

                // Return empty lists for all transaction IDs on error
                return transactionIds.stream()
                        .collect(Collectors.toMap(
                                transactionId -> transactionId,
                                transactionId -> List.<RetryAttempt>of()));
            }
        });
    }
}