package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryInput;
import com.arcone.biopro.distribution.inventory.application.dto.ValidateInventoryOutput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ErrorMessage;
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
class ValidateInventoryUseCase implements UseCase<Mono<ValidateInventoryOutput>, InventoryInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryOutputMapper mapper;

    @Override
    public Mono<ValidateInventoryOutput> execute(InventoryInput input) {

        return inventoryAggregateRepository.findByUnitNumberAndProductCode(input.unitNumber(), input.productCode())
            .map(inventoryAggregate -> mapper.toValidateInventoryOutput(inventoryAggregate.checkIfIsValidToShip(input.location())))
            .switchIfEmpty(Mono.just(mapper.toOutput(ErrorMessage.INVENTORY_NOT_EXIST)));
    }
}
