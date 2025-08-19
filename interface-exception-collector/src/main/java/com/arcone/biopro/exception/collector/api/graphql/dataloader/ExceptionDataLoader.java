package com.arcone.biopro.exception.collector.api.graphql.dataloader;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.MappedBatchLoader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * DataLoader for batching exception queries by transaction ID.
 * Implements the DataLoader pattern to prevent N+1 query problems
 * when loading exception data in GraphQL resolvers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExceptionDataLoader implements MappedBatchLoader<String, InterfaceException> {

    private final InterfaceExceptionRepository exceptionRepository;

    /**
     * Batch loads exceptions by their transaction IDs.
     * This method is called by the DataLoader framework to load multiple
     * exceptions in a single database query instead of individual queries.
     *
     * @param transactionIds Set of transaction IDs to load
     * @return CompletionStage containing a map of transaction ID to
     *         InterfaceException
     */
    @Override
    public CompletionStage<Map<String, InterfaceException>> load(Set<String> transactionIds) {
        if (transactionIds == null || transactionIds.isEmpty()) {
            log.debug("No transaction IDs provided for batch loading");
            return CompletableFuture.completedFuture(Map.of());
        }

        log.debug("Batch loading {} exceptions by transaction IDs", transactionIds.size());

        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            try {
                // Validate input size to prevent excessive batch sizes
                if (transactionIds.size() > 1000) {
                    log.warn(
                            "Large batch size detected: {} transaction IDs. Consider reducing batch size for better performance.",
                            transactionIds.size());
                }

                // Fetch all exceptions in a single query
                List<InterfaceException> exceptions = exceptionRepository.findByTransactionIdIn(transactionIds);

                long queryTime = System.currentTimeMillis() - startTime;
                log.debug("Loaded {} exceptions from database for {} requested IDs in {}ms",
                        exceptions.size(), transactionIds.size(), queryTime);

                // Convert list to map keyed by transaction ID
                Map<String, InterfaceException> exceptionMap = exceptions.stream()
                        .collect(Collectors.toMap(
                                InterfaceException::getTransactionId,
                                Function.identity(),
                                (existing, replacement) -> {
                                    log.warn("Duplicate transaction ID found: {}, keeping first occurrence",
                                            existing.getTransactionId());
                                    return existing;
                                }));

                // Log any missing exceptions for debugging
                Set<String> foundIds = exceptionMap.keySet();
                Set<String> missingIds = transactionIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .collect(Collectors.toSet());

                if (!missingIds.isEmpty()) {
                    log.debug("Could not find exceptions for transaction IDs: {}", missingIds);
                }

                long totalTime = System.currentTimeMillis() - startTime;
                log.debug("Exception batch loading completed in {}ms. Found: {}, Missing: {}",
                        totalTime, exceptionMap.size(), missingIds.size());

                return exceptionMap;

            } catch (Exception e) {
                long totalTime = System.currentTimeMillis() - startTime;
                log.error("Error batch loading exceptions by transaction IDs after {}ms: {}",
                        totalTime, transactionIds, e);

                // Return empty map instead of throwing to prevent GraphQL execution failure
                // Individual field resolvers can handle missing data appropriately
                return Map.<String, InterfaceException>of();
            }
        });
    }
}