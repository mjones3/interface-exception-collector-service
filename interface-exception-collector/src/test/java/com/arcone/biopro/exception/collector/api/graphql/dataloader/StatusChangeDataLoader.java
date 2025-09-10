package com.arcone.biopro.exception.collector.api.graphql.dataloader;

import com.arcone.biopro.exception.collector.domain.entity.StatusChange;
import com.arcone.biopro.exception.collector.infrastructure.repository.StatusChangeRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Test DataLoader for status changes.
 */
public class StatusChangeDataLoader {
    private final StatusChangeRepository repository;

    public StatusChangeDataLoader(StatusChangeRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<Map<String, List<StatusChange>>> load(Set<String> transactionIds) {
        return CompletableFuture.supplyAsync(() -> {
            List<StatusChange> statusChanges = repository.findByInterfaceExceptionTransactionIdInOrderByChangedAtDesc(transactionIds);
            
            return transactionIds.stream()
                    .collect(Collectors.toMap(
                            txId -> txId,
                            txId -> statusChanges.stream()
                                    .filter(change -> change.getInterfaceException().getTransactionId().equals(txId))
                                    .collect(Collectors.toList())
                    ));
        });
    }
}