package com.arcone.biopro.distribution.inventory.domain.repository;

import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import reactor.core.publisher.Mono;

public interface InventoryAggregateRepository {

    Mono<InventoryAggregate> findByUnitNumberAndProductCode(String unitNumber, String productCode);

    Mono<InventoryAggregate> saveInventory(InventoryAggregate inventoryAggregate);

    Mono<Boolean> existsByLocationAndUnitNumberAndProductCode(String location, String unitNumber, String productCode);
}
