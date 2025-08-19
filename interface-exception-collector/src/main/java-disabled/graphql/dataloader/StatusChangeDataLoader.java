package com.arcone.biopro.exception.collector.api.graphql.dataloader;

import com.arcone.biopro.exception.collector.domain.entity.StatusChange;
import com.arcone.biopro.exception.collector.infrastructure.repository.StatusChangeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.MappedBatchLoader;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * DataLoader for batching status change history queries.
 * Prevents N+1 query problems when loading status changes for multiple
 * exceptions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StatusChangeDataLoader implements MappedBatchLoader<String, List<StatusChange>> {

    private final StatusChangeRepository statusChangeRepository;

    @Override
    public CompletionStage<Map<String, List<StatusChange>>> load(Set<String> transactionIds) {
        if (transactionIds == null || transactionIds.isEmpty()) {
            log.debug("No transaction IDs provided for status change batch loading");
            return CompletableFuture.completedFuture(Map.of());
        }

        log.debug("Batch loading status changes for {} transaction IDs", transactionIds.size());

        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            try {
                // Validate batch size
                if (transactionIds.size() > 500) {
                    log.warn(
                            "Large status change batch size detected: {} transaction IDs. Consider reducing batch size.",
                            transactionIds.size());
                }

                // Load all status changes for the given transaction IDs
                List<StatusChange> statusChanges = statusChangeRepository
                        .findByInterfaceExceptionTransactionIdInOrderByChangedAtDesc(transactionIds);

                long queryTime = System.currentTimeMillis() - startTime;
                log.debug("Loaded {} status changes from database in {}ms", statusChanges.size(), queryTime);

                // Group by transaction ID
                Map<String, List<StatusChange>> result = statusChanges.stream()
                        .collect(Collectors.groupingBy(
                                statusChange -> statusChange.getInterfaceException().getTransactionId(),
                                Collectors.toList()));

                // Ensure all requested transaction IDs have an entry (even if empty)
                transactionIds.forEach(transactionId -> result.computeIfAbsent(transactionId, k -> List.of()));

                // Log detailed statistics
                long totalChanges = result.values().stream().mapToLong(List::size).sum();
                long exceptionsWithChanges = result.values().stream()
                        .mapToLong(changes -> changes.isEmpty() ? 0 : 1)
                        .sum();

                long totalTime = System.currentTimeMillis() - startTime;
                log.debug(
                        "Completed batch status change loading in {}ms: {} transaction IDs, {} exceptions with changes, {} total changes",
                        totalTime, transactionIds.size(), exceptionsWithChanges, totalChanges);

                return result;

            } catch (Exception e) {
                long totalTime = System.currentTimeMillis() - startTime;
                log.error("Error loading status changes for transaction IDs after {}ms: {}",
                        totalTime, transactionIds, e);

                // Return empty lists for all transaction IDs on error
                return transactionIds.stream()
                        .collect(Collectors.toMap(
                                transactionId -> transactionId,
                                transactionId -> List.<StatusChange>of()));
            }
        });
    }
}