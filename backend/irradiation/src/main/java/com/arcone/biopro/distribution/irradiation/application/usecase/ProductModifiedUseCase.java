package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.dto.ProductModifiedInput;
import com.arcone.biopro.distribution.irradiation.application.mapper.InventoryOutputMapper;
import com.arcone.biopro.distribution.irradiation.application.service.ConfigurationService;
import com.arcone.biopro.distribution.irradiation.domain.exception.InventoryNotFoundException;
import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.PropertyKey;
import com.arcone.biopro.distribution.irradiation.domain.repository.InventoryAggregateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductModifiedUseCase implements UseCase<Mono<InventoryOutput>, ProductModifiedInput> {

    InventoryAggregateRepository inventoryAggregateRepository;
    InventoryOutputMapper mapper;
    ConfigurationService configurationService;

    @Override
    @Transactional
    public Mono<InventoryOutput> execute(ProductModifiedInput input) {
        log.info("Processing product modified event for unit: {}, product: {}",
                 input.unitNumber(), input.productCode());
        return inventoryAggregateRepository
            .findByUnitNumberAndProductCode(input.unitNumber(), input.parentProductCode())
            .switchIfEmpty(Mono.error(new InventoryNotFoundException(String.format("Inventory not found for unit number %s and product code %s", input.unitNumber(), input.parentProductCode()))))
            .flatMap(parentAggregate -> inventoryAggregateRepository.saveInventory(parentAggregate.modifyProduct()))
            .map(parentAggregate -> mapper.toAggregate(input, parentAggregate.getInventory()))
            .flatMap(this::updateTemperatureCategory)
            .map(aggregate ->  this.updateProperties(input, aggregate))
            .flatMap(inventoryAggregateRepository::saveInventory)
            .map(InventoryAggregate::getInventory)
            .map(mapper::toOutput);
    }

    private Mono<InventoryAggregate> updateTemperatureCategory(InventoryAggregate aggregate) {
        return configurationService.lookUpTemperatureCategory(aggregate.getInventory().getProductCode().value())
            .map(aggregate::updateTemperatureCategory)
            .switchIfEmpty(Mono.just(aggregate));
    }

    private InventoryAggregate updateProperties(ProductModifiedInput input, InventoryAggregate aggregate) {
        if (input.properties() != null && input.properties().containsKey(PropertyKey.TIMEZONE_RELEVANT.name())) {
            String value = input.properties().get(PropertyKey.TIMEZONE_RELEVANT.name());
            if ("Y".equals(value)) {
                aggregate.addTimezoneRelevantFlag();
            }
        }

        return aggregate;
    }
}
