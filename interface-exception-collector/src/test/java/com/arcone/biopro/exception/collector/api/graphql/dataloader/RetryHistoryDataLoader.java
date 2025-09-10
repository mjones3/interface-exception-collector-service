package com.arcone.biopro.exception.collector.api.graphql.dataloader;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Test DataLoader for retry history.
 */
public class RetryHistoryDataLoader {
    private final RetryAttemptRepository retryRepository;
    private final InterfaceExceptionRepository exceptionRepository;

    public RetryHistoryDataLoader(RetryAttemptRepository retryRepository, InterfaceExceptionRepository exceptionRepository) {
        this.retryRepository = retryRepository;
        this.exceptionRepository = exceptionRepository;
    }

    public CompletableFuture<Map<String, List<RetryAttempt>>> load(Set<String> transactionIds) {
        return CompletableFuture.supplyAsync(() -> {
            List<InterfaceException> exceptions = exceptionRepository.findByTransactionIdIn(transactionIds);
            List<RetryAttempt> retryAttempts = retryRepository.findByInterfaceExceptionIn(exceptions);
            
            return transactionIds.stream()
                    .collect(Collectors.toMap(
                            txId -> txId,
                            txId -> retryAttempts.stream()
                                    .filter(retry -> retry.getInterfaceException().getTransactionId().equals(txId))
                                    .sorted((a, b) -> a.getAttemptNumber().compareTo(b.getAttemptNumber()))
                                    .collect(Collectors.toList())
                    ));
        });
    }
}