package com.arcone.biopro.exception.collector.application.service;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.infrastructure.config.CacheConfig;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.infrastructure.client.SourceServiceClient;
import com.arcone.biopro.exception.collector.infrastructure.client.SourceServiceClientRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for retrieving original payloads from source interface services.
 * Uses the SourceServiceClientRegistry to route requests to appropriate clients
 * with circuit breaker pattern for resilience against external service
 * failures.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayloadRetrievalService {

    private final SourceServiceClientRegistry clientRegistry;

    /**
     * Retrieves the original payload for an exception from the appropriate source
     * service.
     * Uses circuit breaker, retry, and timeout patterns for resilience.
     * Results are cached for 30 minutes to improve performance.
     *
     * @param exception the interface exception to retrieve payload for
     * @return CompletableFuture containing the payload response
     */
    @CircuitBreaker(name = "payload-retrieval", fallbackMethod = "getOriginalPayloadFallback")
    @TimeLimiter(name = "payload-retrieval")
    @Retry(name = "payload-retrieval")
    @Cacheable(value = CacheConfig.PAYLOAD_CACHE, key = "#exception.transactionId + ':' + #exception.interfaceType.name()", condition = "#exception.transactionId != null")
    public CompletableFuture<PayloadResponse> getOriginalPayload(InterfaceException exception) {
        log.info("Retrieving original payload for transaction: {}, interface: {}",
                exception.getTransactionId(), exception.getInterfaceType());

        try {
            SourceServiceClient client = clientRegistry.getClient(exception.getInterfaceType());
            return client.getOriginalPayload(exception);
        } catch (IllegalArgumentException e) {
            log.error("No client available for interface type: {}", exception.getInterfaceType());
            return CompletableFuture.completedFuture(
                    PayloadResponse.builder()
                            .transactionId(exception.getTransactionId())
                            .interfaceType(exception.getInterfaceType().name())
                            .sourceService("unknown")
                            .retrieved(false)
                            .errorMessage("No client available for interface type: " + exception.getInterfaceType())
                            .build());
        }
    }

    /**
     * Fallback method for payload retrieval when circuit breaker is open.
     */
    public CompletableFuture<PayloadResponse> getOriginalPayloadFallback(InterfaceException exception, Exception ex) {
        log.error("Payload retrieval circuit breaker activated for transaction: {}, interface: {}, error: {}",
                exception.getTransactionId(), exception.getInterfaceType(), ex.getMessage());

        return CompletableFuture.completedFuture(
                PayloadResponse.builder()
                        .transactionId(exception.getTransactionId())
                        .interfaceType(exception.getInterfaceType().name())
                        .sourceService("fallback")
                        .retrieved(false)
                        .errorMessage("Payload retrieval service is temporarily unavailable: " + ex.getMessage())
                        .build());
    }

    /**
     * Submits a retry request to the appropriate source service.
     *
     * @param exception the interface exception to retry
     * @param payload   the original payload to resubmit
     * @return CompletableFuture containing the retry result
     */
    @CircuitBreaker(name = "retry-submission", fallbackMethod = "submitRetryFallback")
    @TimeLimiter(name = "retry-submission")
    @Retry(name = "retry-submission")
    public CompletableFuture<ResponseEntity<Object>> submitRetry(InterfaceException exception, Object payload) {
        log.info("Submitting retry for transaction: {}, interface: {}",
                exception.getTransactionId(), exception.getInterfaceType());

        try {
            SourceServiceClient client = clientRegistry.getClient(exception.getInterfaceType());
            return client.submitRetry(exception, payload);
        } catch (IllegalArgumentException e) {
            log.error("No client available for interface type: {}", exception.getInterfaceType());
            return CompletableFuture.failedFuture(
                    new RuntimeException("No client available for interface type: " + exception.getInterfaceType(), e));
        }
    }

    /**
     * Fallback method for retry submission when circuit breaker is open.
     */
    public CompletableFuture<ResponseEntity<Object>> submitRetryFallback(
            InterfaceException exception, Object payload, Exception ex) {
        log.error("Retry submission circuit breaker activated for transaction: {}, interface: {}, error: {}",
                exception.getTransactionId(), exception.getInterfaceType(), ex.getMessage());

        return CompletableFuture.failedFuture(
                new RuntimeException("Retry submission service is temporarily unavailable: " + ex.getMessage(), ex));
    }

    /**
     * Synchronous method to retrieve original payload (for backward compatibility).
     * Used by ExceptionController for immediate payload retrieval.
     * Results are cached for 30 minutes to improve performance.
     *
     * @param transactionId the transaction ID
     * @param interfaceType the interface type as string
     * @return the original payload object or null if not available
     */
    @Cacheable(value = CacheConfig.PAYLOAD_CACHE, key = "#transactionId + ':' + #interfaceType", condition = "#transactionId != null and #interfaceType != null")
    public Object getOriginalPayload(String transactionId, String interfaceType) {
        try {
            // Create a minimal exception object for the async method
            InterfaceException tempException = InterfaceException.builder()
                    .transactionId(transactionId)
                    .interfaceType(InterfaceType.valueOf(interfaceType))
                    .build();

            // Call the async method and wait for result
            CompletableFuture<PayloadResponse> future = getOriginalPayload(tempException);
            PayloadResponse response = future.get();

            return response.isRetrieved() ? response.getPayload() : null;

        } catch (Exception e) {
            log.error("Failed to retrieve payload synchronously for transaction: {}, interface: {}, error: {}",
                    transactionId, interfaceType, e.getMessage());
            return null;
        }
    }

    /**
     * Gets all available source service clients.
     *
     * @return list of all registered clients
     */
    public List<SourceServiceClient> getAllClients() {
        return clientRegistry.getAllClients();
    }

    /**
     * Checks if a client is available for the given interface type.
     *
     * @param interfaceType the interface type
     * @return true if a client is available, false otherwise
     */
    public boolean hasClientFor(InterfaceType interfaceType) {
        return clientRegistry.hasClient(interfaceType);
    }
}