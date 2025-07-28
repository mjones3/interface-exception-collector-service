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
    public Mono<Void> execute(Input input) {
        // Query lk_configuration table using prefix OUT_OF_STORAGE_productFamily, example OUT_OF_STORAGE_APHERESIS_PLATELETS_LEUKOREDUCED
        // the response is the minutes
        // compare the input.storageTime and batch start time, if the diff in minutes is higher than the configurable amount, trigger quarantine
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
