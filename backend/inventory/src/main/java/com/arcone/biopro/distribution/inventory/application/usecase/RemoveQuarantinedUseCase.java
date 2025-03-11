package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.RemoveQuarantineInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryUpdatedApplicationEvent;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryUpdateType;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service("REMOVE_QUARANTINE_USE_CASE")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RemoveQuarantinedUseCase implements UseCase<Mono<InventoryOutput>, RemoveQuarantineInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryEventPublisher inventoryEventPublisher;

    InventoryOutputMapper mapper;

    @Override
    public Mono<InventoryOutput> execute(RemoveQuarantineInput removeQuarantineInput) {
        return inventoryAggregateRepository.findByUnitNumberAndProductCode(removeQuarantineInput.product().unitNumber(), removeQuarantineInput.product().productCode())
            .switchIfEmpty(Mono.error(InventoryNotFoundException::new))
            .flatMap(la -> inventoryAggregateRepository.saveInventory(la.removeQuarantine(removeQuarantineInput.quarantineId())))
            .map(InventoryAggregate::getInventory)
            .doOnSuccess(inventory -> inventoryEventPublisher.publish(new InventoryUpdatedApplicationEvent(inventory, InventoryUpdateType.QUARANTINE_REMOVED)))
            .map(mapper::toOutput);    }
}
