package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductConvertedInput;
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
public class ProductConvertedUseCase implements UseCase<Mono<InventoryOutput>, ProductConvertedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryOutputMapper inventoryOutputMapper;

    @Override
    public Mono<InventoryOutput> execute(ProductConvertedInput productConvertedInput) {
        return inventoryAggregateRepository.findByUnitNumberAndProductCode(productConvertedInput.unitNumber().value(), productConvertedInput.productCode().value())
            .switchIfEmpty(Mono.error(InventoryNotFoundException::new))
            .flatMap(aggregate -> inventoryAggregateRepository.saveInventory(aggregate.convertProduct()))
            .doOnSuccess(aggregate -> log.info("Product converted: {}", aggregate))
            .map(InventoryAggregate::getInventory)
            .map(inventoryOutputMapper::toOutput);
    }
}
