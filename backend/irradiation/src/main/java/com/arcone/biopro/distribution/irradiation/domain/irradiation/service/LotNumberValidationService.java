package com.arcone.biopro.distribution.irradiation.domain.irradiation.service;

import reactor.core.publisher.Mono;

/**
 * Domain service for validating lot numbers with the supply service.
 */
public interface LotNumberValidationService {

    /**
     * Validates a lot number with the supply service.
     *
     * @param lotNumber the lot number to validate
     * @param type the type of lot number
     * @return a Mono that emits true if the lot number is valid, false otherwise
     */
    Mono<Boolean> validateLotNumber(String lotNumber, String type);
}
