package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryAlreadyExistsException;
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
class LabelAppliedUseCase implements UseCase<Mono<InventoryOutput>, InventoryInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryOutputMapper mapper;

    @Override
    public Mono<InventoryOutput> execute(InventoryInput input) {
        return inventoryAggregateRepository.existsByLocationAndUnitNumberAndProductCode(input.location(), input.unitNumber(), input.productCode())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new InventoryAlreadyExistsException());
                } else {
                    return inventoryAggregateRepository.saveInventory(
                        InventoryAggregate.builder().build().createInventory(
                            input.unitNumber(),
                            input.productCode(),
                            input.productDescription(),
                            input.expirationDate(),
                            input.collectionDate(),
                            input.location(),
                            input.productFamily(),
                            input.aboRh())
                    );
                }
            })
            .map(InventoryAggregate::getInventory)
            .map(mapper::toOutput);
    }
}
