package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.RemoveQuarantineInput;
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

@Service("REMOVE_QUARANTINE_USE_CASE")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RemoveQuarantinedUseCase implements UseCase<Mono<InventoryOutput>, RemoveQuarantineInput> {

    InventoryAggregateRepository inventoryAggregateRepository;

    InventoryOutputMapper mapper;

    @Override
    public Mono<InventoryOutput> execute(RemoveQuarantineInput removeQuarantineInput) {
        return inventoryAggregateRepository.findByUnitNumberAndProductCode(removeQuarantineInput.product().unitNumber(), removeQuarantineInput.product().productCode())
            .switchIfEmpty(Mono.error(InventoryNotFoundException::new))
            .flatMap(la -> inventoryAggregateRepository.saveInventory(la.removeQuarantine(removeQuarantineInput.quarantineId())))
            .map(InventoryAggregate::getInventory)
            .map(mapper::toOutput);    }
}
