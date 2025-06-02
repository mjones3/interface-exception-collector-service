package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface PropertyEntityRepository extends ReactiveCrudRepository<PropertyEntity, UUID> {
    Flux<PropertyEntity> findByInventoryId(UUID inventoryId);
}
