package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.service.LotNumberValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateLotNumberUseCaseImpl implements ValidateLotNumberUseCase {

    private final LotNumberValidationService lotNumberValidationService;

    @Override
    public Mono<Boolean> execute(String lotNumber, String type) {
        log.debug("Executing ValidateLotNumberUseCase with lotNumber: {} and type: {}", lotNumber, type);

        return lotNumberValidationService.validateLotNumber(lotNumber, type)
            .flatMap(isValid -> {
                if (Boolean.TRUE.equals(isValid)) {
                    return Mono.just(true);
                } else {
                    return Mono.error(new RuntimeException("Validation failed"));
                }
            })
            .onErrorResume(error -> {
                log.error("Error validating lot number: {}", error.getMessage());
                return Mono.error(new RuntimeException("Validation failed"));
            });
    }
}
