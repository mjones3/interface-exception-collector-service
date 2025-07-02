package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.AddQuarantineInput;
import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.irradiation.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.irradiation.domain.event.InventoryUpdatedApplicationEvent;
import com.arcone.biopro.distribution.irradiation.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.InventoryUpdateType;
import com.arcone.biopro.distribution.irradiation.domain.repository.InventoryAggregateRepository;
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
public class AddQuarantinedUseCase implements UseCase<Mono<InventoryOutput>, AddQuarantineInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryEventPublisher inventoryEventPublisher;

    InventoryOutputMapper mapper;

    @Override
    public Mono<InventoryOutput> execute(AddQuarantineInput addQuarantineInput) {
        return inventoryAggregateRepository.findByUnitNumberAndProductCode(addQuarantineInput.product().unitNumber(), addQuarantineInput.product().productCode())
            .switchIfEmpty(Mono.error(InventoryNotFoundException::new))
            .flatMap(la -> inventoryAggregateRepository.saveInventory(la.addQuarantine(addQuarantineInput.quarantineId(), addQuarantineInput.reason(), addQuarantineInput.comments())))
            .map(InventoryAggregate::getInventory)
            .doOnSuccess(inventory -> inventoryEventPublisher.publish(new InventoryUpdatedApplicationEvent(inventory, InventoryUpdateType.QUARANTINE_APPLIED)))
            .map(mapper::toOutput);    }
}
