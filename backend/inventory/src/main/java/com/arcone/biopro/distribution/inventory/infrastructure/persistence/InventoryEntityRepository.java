package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.graphql.data.GraphQlRepository;
import reactor.core.publisher.Mono;

@GraphQlRepository
public interface InventoryEntityRepository extends ReactiveCrudRepository<InventoryEntity, Long> {

    Mono<InventoryEntity> findByUnitNumberAndProductCode(String unitNumber, String productCode);

    Mono<Boolean> existsByUnitNumberAndProductCode(String unitNumber, String productCode);

}
