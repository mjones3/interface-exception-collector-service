package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.LabelInvalidatedInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import com.arcone.biopro.distribution.inventory.domain.util.ProductCodeUtil;
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
public class LabelInvalidatedUseCase implements UseCase<Mono<InventoryOutput>, LabelInvalidatedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryOutputMapper mapper;

    @Override
    public Mono<InventoryOutput> execute(LabelInvalidatedInput input) {
        var productCode = ProductCodeUtil.retrieveFinalProductCodeWithoutSixthDigit(input.productCode());
        return inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber(), productCode)
            .switchIfEmpty(Mono.error(InventoryNotFoundException::new))
            .flatMap(inventoryAggregate -> inventoryAggregateRepository.saveInventory(inventoryAggregate.invalidLabel())
                .map(InventoryAggregate::getInventory)
                .map(mapper::toOutput));
    }
}
