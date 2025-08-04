package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.QuarantineProduct;
import com.arcone.biopro.distribution.irradiation.adapter.out.kafka.dto.QuarantineProductInput;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.service.OutOfStorageValidationService;
import com.arcone.biopro.distribution.irradiation.infrastructure.event.ProductQuarantinedApplicationEvent;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductStoredUseCase implements UseCase<Mono<Void>, ProductStoredUseCase.Input> {

    private final OutOfStorageValidationService outOfStorageValidationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Mono<Void> execute(Input input) {
        return outOfStorageValidationService.processProductStoredEvent(
                input.unitNumber(),
                input.productCode(),
                input.storageTime()
        )
        .flatMap(result -> {
            if (!result.batchClosed()) {
                log.info("Batch is not closed for unit: {}, product: {}, ignoring product stored event",
                        input.unitNumber(), input.productCode());
                return Mono.empty();
            }

            if (result.alreadyProcessed()) {
                log.info("Product stored event already processed for unit: {}, product: {}, ignoring",
                        input.unitNumber(), input.productCode());
                return Mono.empty();
            }

            if (result.shouldQuarantine()) {
                log.info("Product {} with unit {} has exceeded out-of-storage time limit, triggering quarantine",
                        input.productCode(), input.unitNumber());

                QuarantineProduct quarantineProduct = QuarantineProduct.builder()
                        .products(List.of(QuarantineProductInput.builder()
                                .unitNumber(input.unitNumber())
                                .productCode(input.productCode())
                                .build()))
                        .triggeredBy("IRRADIATION_SYSTEM")
                        .reasonKey("OUT_OF_STORAGE_TIME_EXCEEDED")
                        .comments("Product exceeded maximum out-of-storage time before irradiation")
                        .performedBy(input.performedBy())
                        .build();

                eventPublisher.publishEvent(new ProductQuarantinedApplicationEvent(quarantineProduct));
            }

            // Only mark as processed if we actually performed timing validation
            if (result.validationPerformed()) {
                return outOfStorageValidationService.markEventAsProcessed(
                        input.unitNumber(),
                        input.productCode()
                );
            }

            return Mono.empty();
        });
    }



    @Builder
    public record Input(
        String unitNumber,
        String productCode,
        String deviceStored,
        String deviceUsed,
        String storageLocation,
        String location,
        String locationType,
        ZonedDateTime storageTime,
        String performedBy
    ) {}
}
