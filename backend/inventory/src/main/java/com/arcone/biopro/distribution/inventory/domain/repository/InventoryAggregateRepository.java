package com.arcone.biopro.distribution.inventory.domain.repository;

import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventoryAggregateRepository {

    Flux<InventoryAggregate> findByUnitNumber(String unitNumber);

    Mono<InventoryAggregate> findByUnitNumberAndProductCode(String unitNumber, String productCode);

    Mono<InventoryAggregate> saveInventory(InventoryAggregate inventoryAggregate);

    Mono<Boolean> existsByLocationAndUnitNumberAndProductCode(String location, String unitNumber, String productCode);

    Flux<InventoryAggregate> findAllAvailable(String location, String productFamily, AboRhCriteria aboRh);

    Flux<InventoryAggregate> findAllAvailableShortDate(String location, String productFamily, AboRhCriteria aboRh);

    Mono<Long> countAllAvailable(String location, String productFamily, AboRhCriteria abRh);

    Mono<InventoryAggregate> findByLocationAndUnitNumberAndProductCode(String location, String unitNumber, String productCode);
}
