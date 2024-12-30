package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.CheckInCompletedInput;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductCreatedInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryCreatedEvent;
import com.arcone.biopro.distribution.inventory.domain.exception.InvalidUpdateProductStatusException;
import com.arcone.biopro.distribution.inventory.domain.exception.InventoryAlreadyExistsException;
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
public class CheckInCompletedUseCase implements UseCase<Mono<InventoryOutput>, CheckInCompletedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryOutputMapper mapper;

    public Mono<InventoryOutput> execute(CheckInCompletedInput checkInCompletedInput) {
        return inventoryAggregateRepository
            .findByUnitNumberAndProductCode(checkInCompletedInput.unitNumber(), checkInCompletedInput.productCode())
            .flatMap(aggregate -> Mono.<InventoryAggregate>error(new InventoryAlreadyExistsException()))
            .switchIfEmpty(Mono.defer(() -> buildAggregate(checkInCompletedInput)))
            .flatMap(inventoryAggregateRepository::saveInventory)
            .map(InventoryAggregate::getInventory)
            .map(mapper::toOutput)
            .doOnSuccess(response -> log.info("Product created/updated: {}", response))
            .doOnError(e -> log.error("Error occurred during product creation/update. Input: {}, error: {}", checkInCompletedInput, e.getMessage(), e));
    }

    private Mono<InventoryAggregate> buildAggregate(CheckInCompletedInput checkInCompletedInput) {
        return Mono.just(mapper.toAggregate(checkInCompletedInput));
    }
}
