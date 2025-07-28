package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.UpdateQuarantineInput;
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
public class UpdateQuarantinedUseCase implements UseCase<Mono<InventoryOutput>, UpdateQuarantineInput> {

    InventoryAggregateRepository inventoryAggregateRepository;

    InventoryOutputMapper mapper;

    @Override
    public Mono<InventoryOutput> execute(UpdateQuarantineInput input) {
        return inventoryAggregateRepository.findByUnitNumberAndProductCode(input.product().unitNumber(), input.product().productCode())
            .switchIfEmpty(Mono.error(InventoryNotFoundException::new))
            .flatMap(la -> inventoryAggregateRepository.saveInventory(la.updateQuarantine(input.quarantineId(), input.reason(), input.comments(), input.stopsManufacturing())))
            .map(InventoryAggregate::getInventory)
            .map(mapper::toOutput);    }
}
