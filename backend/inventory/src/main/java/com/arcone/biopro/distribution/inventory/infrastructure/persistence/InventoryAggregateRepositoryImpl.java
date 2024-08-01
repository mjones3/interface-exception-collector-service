package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import org.springframework.graphql.data.GraphQlRepository;

@GraphQlRepository
public class InventoryAggregateRepositoryImpl implements InventoryAggregateRepository {
}
