package com.arcone.biopro.distribution.inventory.domain.repository;

import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.graphql.data.GraphQlRepository;
import reactor.core.publisher.Mono;

@GraphQlRepository
public interface InventoryRepository extends ReactiveCrudRepository<Inventory, Long> {
}
