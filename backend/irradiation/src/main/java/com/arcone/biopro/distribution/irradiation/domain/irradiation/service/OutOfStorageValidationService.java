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
     * @param deviceUse the device used for processing
     * @param storageTime the time when the product was stored
     * @return processing result
     */
    Mono<ProcessingResult> processProductStoredEvent(String unitNumber, String productCode, String deviceUse, ZonedDateTime storageTime);
    
    /**
     * Marks the product stored event as processed.
     *
     * @param unitNumber the unit number of the product
     * @param productCode the product code
     * @param deviceUse the device used for processing
     * @return completion signal
     */
    Mono<Void> markEventAsProcessed(String unitNumber, String productCode, String deviceUse);
    
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