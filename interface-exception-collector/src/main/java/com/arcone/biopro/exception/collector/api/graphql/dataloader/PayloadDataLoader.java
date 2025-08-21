package com.arcone.biopro.exception.collector.api.graphql.dataloader;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.application.service.PayloadRetrievalService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
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
 * DataLoader for batching original payload retrieval operations.
 * Implements the DataLoader pattern to efficiently retrieve payloads
 * from external services while preventing N+1 query problems.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PayloadDataLoader implements MappedBatchLoader<String, PayloadResponse> {

    private final PayloadRetrievalService payloadRetrievalService;
    private final InterfaceExceptionRepository exceptionRepository;

    /**
     * Batch loads original payloads by transaction IDs.
     * This method retrieves the exception details first, then batches
     * the payload retrieval operations to external services.
     *
     * @param transactionIds Set of transaction IDs to load payloads for
     * @return CompletionStage containing a map of transaction ID to PayloadResponse
     */
    @Override
    public CompletionStage<Map<String, PayloadResponse>> load(Set<String> transactionIds) {
        if (transactionIds == null || transactionIds.isEmpty()) {
            log.debug("No transaction IDs provided for payload batch loading");
            return CompletableFuture.completedFuture(Map.of());
        }

        log.debug("Batch loading payloads for {} transaction IDs", transactionIds.size());

        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();
            Map<String, PayloadResponse> results = new HashMap<>();

            try {
                // Validate batch size to prevent excessive external service calls
                if (transactionIds.size() > 50) {
                    log.warn(
                            "Large payload batch size detected: {} transaction IDs. This may impact external service performance.",
                            transactionIds.size());
                }

                // First, get the exceptions to understand interface types
                List<InterfaceException> exceptions = exceptionRepository.findByTransactionIdIn(transactionIds);

                long queryTime = System.currentTimeMillis() - startTime;
                log.debug("Found {} exceptions for payload loading in {}ms", exceptions.size(), queryTime);

                // Create futures for all payload retrieval operations with timeout handling
                Map<String, CompletableFuture<PayloadResponse>> payloadFutures = exceptions.stream()
                        .collect(Collectors.toMap(
                                InterfaceException::getTransactionId,
                                exception -> payloadRetrievalService.getOriginalPayload(exception)
                                        .orTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 30 second timeout
                                        .exceptionally(throwable -> {
                                            log.error("Payload retrieval failed for transaction ID: {} - {}",
                                                    exception.getTransactionId(), throwable.getMessage());
                                            return PayloadResponse.builder()
                                                    .transactionId(exception.getTransactionId())
                                                    .retrieved(false)
                                                    .errorMessage("Payload retrieval timeout or error: "
                                                            + throwable.getMessage())
                                                    .build();
                                        })));

                // Wait for all futures to complete and collect results
                for (Map.Entry<String, CompletableFuture<PayloadResponse>> entry : payloadFutures.entrySet()) {
                    try {
                        PayloadResponse response = entry.getValue().get();
                        results.put(entry.getKey(), response);

                        if (response.isRetrieved()) {
                            log.debug("Successfully loaded payload for transaction ID: {}", entry.getKey());
                        } else {
                            log.debug("Payload not available for transaction ID: {} - {}",
                                    entry.getKey(), response.getErrorMessage());
                        }
                    } catch (Exception e) {
                        log.error("Failed to load payload for transaction ID: {}", entry.getKey(), e);
                        // Create error response for failed payload retrieval
                        PayloadResponse errorResponse = PayloadResponse.builder()
                                .transactionId(entry.getKey())
                                .retrieved(false)
                                .errorMessage("Failed to retrieve payload: " + e.getMessage())
                                .build();
                        results.put(entry.getKey(), errorResponse);
                    }
                }

                // Handle transaction IDs that don't have corresponding exceptions
                Set<String> foundTransactionIds = exceptions.stream()
                        .map(InterfaceException::getTransactionId)
                        .collect(Collectors.toSet());

                for (String transactionId : transactionIds) {
                    if (!foundTransactionIds.contains(transactionId)) {
                        log.debug("No exception found for transaction ID: {}", transactionId);
                        PayloadResponse notFoundResponse = PayloadResponse.builder()
                                .transactionId(transactionId)
                                .retrieved(false)
                                .errorMessage("Exception not found for transaction ID: " + transactionId)
                                .build();
                        results.put(transactionId, notFoundResponse);
                    }
                }

                long totalTime = System.currentTimeMillis() - startTime;
                long successCount = results.values().stream()
                        .mapToLong(response -> response.isRetrieved() ? 1 : 0)
                        .sum();

                log.debug("Completed batch payload loading for {} transaction IDs in {}ms. Success: {}, Failed: {}",
                        transactionIds.size(), totalTime, successCount, results.size() - successCount);

                return results;

            } catch (Exception e) {
                long totalTime = System.currentTimeMillis() - startTime;
                log.error("Error in batch payload loading for transaction IDs after {}ms: {}",
                        totalTime, transactionIds, e);

                // Return error responses for all requested transaction IDs
                for (String transactionId : transactionIds) {
                    if (!results.containsKey(transactionId)) {
                        results.put(transactionId, PayloadResponse.builder()
                                .transactionId(transactionId)
                                .retrieved(false)
                                .errorMessage("Batch payload loading failed: " + e.getMessage())
                                .build());
                    }
                }

                return results;
            }
        });
    }
}