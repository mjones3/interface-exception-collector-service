package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.LabelInvalidedInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryUpdatedApplicationEvent;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryUpdateType;
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
public class LabelInvalidedUseCase implements UseCase<Mono<InventoryOutput>, LabelInvalidedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryEventPublisher inventoryEventPublisher;
    InventoryOutputMapper mapper;

    @Override
    public Mono<InventoryOutput> execute(LabelInvalidedInput input) {
        var productCode = ProductCodeUtil.retrieveFinalProductCodeWithoutSixthDigit(input.productCode());
        return inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber(), productCode)
            .switchIfEmpty(Mono.error(InventoryNotFoundException::new))
            .flatMap(inventoryAggregate -> inventoryAggregateRepository.saveInventory(inventoryAggregate.invalidLabel())
                .map(InventoryAggregate::getInventory)
                .doOnSuccess(inventory -> inventoryEventPublisher.publish(new InventoryUpdatedApplicationEvent(inventory, InventoryUpdateType.LABEL_INVALIDED)))
                .map(mapper::toOutput));
    }
}
