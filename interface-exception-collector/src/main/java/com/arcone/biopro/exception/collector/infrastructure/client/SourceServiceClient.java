package com.arcone.biopro.exception.collector.infrastructure.client;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for source service clients that retrieve original payloads
 * and submit retry requests to external interface services.
 */
public interface SourceServiceClient {

    /**
     * Retrieves the original payload for an exception from the source service.
     *
     * @param exception the interface exception to retrieve payload for
     * @return CompletableFuture containing the payload response
     */
    CompletableFuture<PayloadResponse> getOriginalPayload(InterfaceException exception);

    /**
     * Submits a retry request to the source service.
     *
     * @param exception the interface exception to retry
     * @param payload   the original payload to resubmit
     * @return CompletableFuture containing the retry result
     */
    CompletableFuture<ResponseEntity<Object>> submitRetry(InterfaceException exception, Object payload);

    /**
     * Checks if this client supports the given interface type.
     *
     * @param interfaceType the interface type to check
     * @return true if supported, false otherwise
     */
    boolean supports(String interfaceType);

    /**
     * Gets the service name for this client.
     *
     * @return the service name
     */
    String getServiceName();
}