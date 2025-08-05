package com.arcone.biopro.distribution.irradiation.domain.irradiation.service;

import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

/**
 * Domain service for validating out-of-storage time limits.
 */
public interface OutOfStorageValidationService {
    
    /**
     * Processes the product stored event in a single operation.
     *
     * @param unitNumber the unit number of the product
     * @param productCode the product code
     * @param storageTime the time when the product was stored
     * @return processing result
     */
    Mono<ProcessingResult> processProductStoredEvent(String unitNumber, String productCode, ZonedDateTime storageTime);
    
    /**
     * Marks the product stored event as processed.
     *
     * @param unitNumber the unit number of the product
     * @param productCode the product code
     * @return completion signal
     */
    Mono<Void> markEventAsProcessed(String unitNumber, String productCode);
    
    /**
     * Result of processing a product stored event.
     */
    record ProcessingResult(
        boolean batchClosed,
        boolean alreadyProcessed,
        boolean shouldQuarantine,
        boolean validationPerformed
    ) {}
}