package com.arcone.biopro.exception.collector.api.graphql.dataloader;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Test DataLoader for exceptions.
 */
public class ExceptionDataLoader {
    private final InterfaceExceptionRepository repository;

    public ExceptionDataLoader(InterfaceExceptionRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<Map<String, InterfaceException>> load(Set<String> transactionIds) {
        return CompletableFuture.supplyAsync(() -> {
            return repository.findByTransactionIdIn(transactionIds)
                    .stream()
                    .collect(Collectors.toMap(
                            InterfaceException::getTransactionId,
                            exception -> exception
                    ));
        });
    }
}