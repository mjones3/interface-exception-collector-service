package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.dto.ProductCreatedInput;
import com.arcone.biopro.distribution.irradiation.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.irradiation.application.service.ConfigurationService;
import com.arcone.biopro.distribution.irradiation.domain.event.InventoryCreatedEvent;
import com.arcone.biopro.distribution.irradiation.domain.event.InventoryEventPublisher;
import com.arcone.biopro.distribution.irradiation.domain.exception.InvalidUpdateProductStatusException;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
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
public class ProductCreatedUseCase implements UseCase<Mono<InventoryOutput>, ProductCreatedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryOutputMapper mapper;
    InventoryEventPublisher publisher;
    ConfigurationService configurationService;

    @Override
    public Mono<InventoryOutput> execute(ProductCreatedInput productCreatedInput) {
        return inventoryAggregateRepository
            .findByUnitNumberAndProductCode(productCreatedInput.unitNumber(), productCreatedInput.productCode())
            .switchIfEmpty(Mono.defer(() -> buildAggregate(productCreatedInput)))
            .filter(aggregate -> aggregate.isAvailable() && !aggregate.getIsLabeled() && !aggregate.isQuarantined())
            .switchIfEmpty(Mono.error(InvalidUpdateProductStatusException::new))
            .flatMap(aggregate ->
                configurationService.lookUpTemperatureCategory(aggregate.getInventory().getProductCode().value())
                    .map(aggregate::updateTemperatureCategory)
                    .switchIfEmpty(Mono.just(aggregate))
            )
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
