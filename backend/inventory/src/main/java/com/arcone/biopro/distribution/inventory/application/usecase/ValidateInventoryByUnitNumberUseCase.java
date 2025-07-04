package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.ValidateInventoryByUnitNumberInput;
import com.arcone.biopro.distribution.inventory.application.dto.ValidateInventoryOutput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.MessageType;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class ValidateInventoryByUnitNumberUseCase implements UseCase<Flux<ValidateInventoryOutput>, ValidateInventoryByUnitNumberInput> {

    InventoryAggregateRepository inventoryAggregateRepository;

    InventoryOutputMapper mapper;

    @Override
    public Flux<ValidateInventoryOutput> execute(ValidateInventoryByUnitNumberInput input) {

        return inventoryAggregateRepository.findByUnitNumber(input.unitNumber())
            .filter(inventoryAggregate -> !List.of(InventoryStatus.MODIFIED, InventoryStatus.CONVERTED, InventoryStatus.DISCARDED).contains(inventoryAggregate.getInventory().getInventoryStatus()))
            .map(inventoryAggregate -> mapper.toValidateInventoryOutput(inventoryAggregate.checkIfIsValidToShip(input.inventoryLocation())))
            .switchIfEmpty(Mono.just(mapper.toOutput(MessageType.INVENTORY_NOT_EXIST)));
    }
}
