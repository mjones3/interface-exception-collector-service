package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductCompletedInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.application.mapper.VolumeInputMapper;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductCompletedUseCase implements UseCase<Mono<InventoryOutput>, ProductCompletedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryOutputMapper inventoryOutputMapper;
    VolumeInputMapper volumeInputMapper;

    @Override
    public Mono<InventoryOutput> execute(ProductCompletedInput productCompletedInput) {

        return inventoryAggregateRepository.findByUnitNumberAndProductCode(productCompletedInput.unitNumber(), productCompletedInput.productCode())
            .switchIfEmpty(Mono.error(InventoryNotFoundException::new))
            .flatMap(inventoryAggregate -> inventoryAggregateRepository
                .saveInventory(inventoryAggregate.completeProduct(volumeInputMapper.toDomain(productCompletedInput.volumes()), productCompletedInput.aboRh()))
            .map(InventoryAggregate::getInventory)
            .map(inventory -> inventoryOutputMapper.toOutput(inventory,inventoryAggregate.getProperties()))
            .doOnSuccess(response -> log.info("Product volume was updated to completed: {}", response))
            .doOnError(e -> log.error("Error occurred during product completed. Input: {}, error: {}", productCompletedInput, e.getMessage(), e)));

    }
}
