package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.application.dto.ProductCreatedInput;
import com.arcone.biopro.distribution.inventory.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.inventory.domain.event.InventoryCreatedEvent;
import com.arcone.biopro.distribution.inventory.domain.exception.InvalidUpdateProductStatusException;
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
public class ProductCreatedUseCase implements UseCase<Mono<InventoryOutput>, ProductCreatedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryOutputMapper mapper;
    InventoryEventPublisher publisher;

    @Override
    public Mono<InventoryOutput> execute(ProductCreatedInput productCreatedInput) {
        return inventoryAggregateRepository
            .findByUnitNumberAndProductCode(productCreatedInput.unitNumber(), productCreatedInput.productCode())
            .switchIfEmpty(Mono.defer(() -> buildAggregate(productCreatedInput)))
            .filter(aggregate -> aggregate.isAvailable() && !aggregate.getIsLabeled() && !aggregate.isQuarantined())
            .switchIfEmpty(Mono.error(InvalidUpdateProductStatusException::new))
            .flatMap(inventoryAggregateRepository::saveInventory)
            .doOnSuccess(aggregate -> publisher.publish(new InventoryCreatedEvent(aggregate)))
            .map(InventoryAggregate::getInventory)
            .map(mapper::toOutput)
            .doOnSuccess(response -> log.info("Product created/updated: {}", response))
            .doOnError(e -> log.error("Error occurred during product creation/update. Input: {}, error: {}", productCreatedInput, e.getMessage(), e));
    }

    private Mono<InventoryAggregate> buildAggregate(ProductCreatedInput productCreatedInput) {
        return Mono.just(mapper.toAggregate(productCreatedInput));
    }
}
