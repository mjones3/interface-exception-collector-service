package com.arcone.biopro.distribution.irradiation.application.usecase;

import reactor.core.publisher.Mono;

/**
 * Use case for validating lot numbers with the supply service.
 */
public interface ValidateLotNumberUseCase {
    
    /**
     * Executes the use case with the given lot number and type.
     *
     * @param lotNumber the lot number to validate
     * @param type the type of lot number
     * @return a Mono that emits true if the lot number is valid, false otherwise
     */
    Mono<Boolean> execute(String lotNumber, String type);
}