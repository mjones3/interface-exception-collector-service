package com.arcone.biopro.distribution.irradiation.application.usecase;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductStoredUseCase implements UseCase<Mono<Void>, ProductStoredUseCase.Input> {


    @Override
    public Mono<Void> execute(Input args) {
        log.info("To check if time out of storage is higher than the configurable amount of time and trigger quarantine if yes");
        return Mono.empty();
    }

    @Builder
    public record Input(
        String unitNumber,
        String productCode,
        String deviceStored,
        String deviceUse,
        String storageLocation,
        String location,
        String locationType,
        ZonedDateTime storageTime,
        String performedBy
    ) {}
}
