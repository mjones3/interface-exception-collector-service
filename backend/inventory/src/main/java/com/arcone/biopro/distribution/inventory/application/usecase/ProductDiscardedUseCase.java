package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductDiscardedInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
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
public class ProductDiscardedUseCase implements UseCase<Mono<InventoryOutput>, ProductDiscardedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;

    InventoryOutputMapper inventoryOutputMapper;

    @Override
    public Mono<InventoryOutput> execute(ProductDiscardedInput input) {
        return inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber(), input.productCode())
            .switchIfEmpty(Mono.error(InventoryNotFoundException::new))
            .flatMap(inventoryAggregate -> inventoryAggregateRepository.saveInventory(inventoryAggregate.discardProduct(input.reason(), input.comments()))
                .map(InventoryAggregate::getInventory)
                .map(inventoryOutputMapper::toOutput));
    }
}
