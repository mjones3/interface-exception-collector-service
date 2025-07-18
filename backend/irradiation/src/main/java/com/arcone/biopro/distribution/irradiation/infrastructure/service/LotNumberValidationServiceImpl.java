package com.arcone.biopro.distribution.irradiation.infrastructure.service;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.service.LotNumberValidationService;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client.ValidateSupplyRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class LotNumberValidationServiceImpl implements LotNumberValidationService {

    private final RSocketRequester supplyRSocketRequester;

    @Override
    public Mono<Boolean> validateLotNumber(String lotNumber, String type) {
        log.debug("Validating lot number: {} of type: {}", lotNumber, type);

        return supplyRSocketRequester
            .route("validateSupply")
            .data(new ValidateSupplyRequestDTO(lotNumber, type))
            .retrieveMono(Boolean.class)
            .doOnSuccess(isValid -> log.debug("Lot number validation result: {}", isValid))
            .doOnError(error -> log.error("Error validating lot number: {}", error.getMessage()));
    }
}
