package com.arcone.biopro.distribution.irradiation.domain.repository;

import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhCriteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventoryAggregateRepository {

    Flux<InventoryAggregate> findByUnitNumber(String unitNumber);

    Mono<InventoryAggregate> findByUnitNumberAndProductCode(String unitNumber, String productCode);

    Mono<InventoryAggregate> saveInventory(InventoryAggregate inventoryAggregate);

    Flux<InventoryAggregate> findAllAvailableShortDate(String location, String productFamily, AboRhCriteria aboRh, String temperatureCategory);

    Mono<Long> countAllAvailable(String location, String productFamily, AboRhCriteria abRh, String temperatureCategory);

    Mono<InventoryAggregate> findByLocationAndUnitNumberAndProductCode(String location, String unitNumber, String productCode);
}
