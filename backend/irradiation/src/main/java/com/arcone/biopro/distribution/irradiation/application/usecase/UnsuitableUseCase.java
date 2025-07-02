package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.UnsuitableInput;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.repository.InventoryAggregateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UnsuitableUseCase implements UseCase<Mono<Void>, UnsuitableInput> {

    InventoryAggregateRepository inventoryAggregateRepository;

    @Override
    @Transactional
    public Mono<Void> execute(UnsuitableInput input) {
        if (Objects.isNull(input.productCode())) {
            return inventoryAggregateRepository.findByUnitNumber(input.unitNumber())
                .flatMap(aggregate -> processInventory(aggregate, input.reasonKey()))
                .then()
                .doOnSuccess(v -> log.info("Processed all inventories for unit number: {}", input.unitNumber()))
                .doOnError(e -> log.error("Error processing inventories for unit number: {}, error: {}", input.unitNumber(), e.getMessage(), e));
        } else {
            return inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber(), input.productCode())
                .switchIfEmpty(Mono.fromRunnable(() -> log.warn("Product not found for unitNumber: {}, productCode: {}. Skipping.", input.unitNumber(), input.productCode())))
                .flatMap(aggregate -> processInventory(aggregate, input.reasonKey()))
                .then();
        }
    }

    private Mono<Void> processInventory(InventoryAggregate aggregate, String reason) {
        return inventoryAggregateRepository.saveInventory(aggregate.unsuit(reason))
            .doOnSuccess(updatedAggregate -> log.info("Product status updated to UNSUITABLE: {}", updatedAggregate.getInventory()))
            .doOnError(e -> log.error("Error updating product to UNSUITABLE. Inventory: {}, error: {}", aggregate.getInventory(), e.getMessage(), e))
            .then();
    }
}
