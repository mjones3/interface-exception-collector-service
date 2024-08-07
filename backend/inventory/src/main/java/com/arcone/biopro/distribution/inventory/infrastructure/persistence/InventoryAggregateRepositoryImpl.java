package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.graphql.data.GraphQlRepository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@GraphQlRepository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryAggregateRepositoryImpl implements InventoryAggregateRepository {

    InventoryEntityRepository inventoryEntityRepository;

    InventoryEntityMapper inventoryEntityMapper;

    @Override
    public Mono<InventoryAggregate> findByUnitNumberAndProductCode(String unitNumber, String productCode) {
        return inventoryEntityRepository.findByUnitNumberAndProductCode(unitNumber, productCode)
            .map(inventoryEntityMapper::toDomain)
            .flatMap(inventory -> Mono.just(InventoryAggregate.builder().inventory(inventory).build()));
    }

    @Override
    public Mono<InventoryAggregate> saveInventory(InventoryAggregate inventoryAggregate) {
        return inventoryEntityRepository
            .save(inventoryEntityMapper.toEntity(inventoryAggregate.getInventory()))
            .then(Mono.just(inventoryAggregate));
    }

    @Override
    public Mono<Boolean> existsByLocationAndUnitNumberAndProductCode(String location, String unitNumber, String productCode) {
        return inventoryEntityRepository.existsByLocationAndUnitNumberAndProductCode(location, unitNumber, productCode);
    }
}
