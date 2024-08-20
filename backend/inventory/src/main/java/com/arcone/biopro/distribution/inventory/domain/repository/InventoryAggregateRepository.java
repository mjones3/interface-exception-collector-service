package com.arcone.biopro.distribution.inventory.domain.repository;

import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InventoryAggregateRepository {

    Mono<InventoryAggregate> findByUnitNumberAndProductCode(String unitNumber, String productCode);

    Mono<InventoryAggregate> findByUnitNumberAndProductCodeAndLocation(String unitNumber, String productCode, String location);

    Mono<InventoryAggregate> saveInventory(InventoryAggregate inventoryAggregate);

    Mono<Boolean> existsByLocationAndUnitNumberAndProductCode(String location, String unitNumber, String productCode);

    Flux<InventoryAggregate> findAllAvailable(String location, ProductFamily productFamily, AboRhCriteria aboRh);

    Flux<InventoryAggregate> findAllAvailableShortDate(String location, ProductFamily productFamily, AboRhCriteria aboRh);

    Mono<Long> countAllAvailable(String location, ProductFamily productFamily, AboRhCriteria abRh);

}
