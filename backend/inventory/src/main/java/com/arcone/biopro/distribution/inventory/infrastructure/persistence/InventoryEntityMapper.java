package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryEntityMapper {

    @Mapping(target = "unitNumber", source = "unitNumber.value")
    @Mapping(target = "productCode", source = "productCode.value")
    InventoryEntity toEntity(Inventory domain);

    @Mapping(target = "unitNumber.value", source = "unitNumber")
    @Mapping(target = "productCode.value", source = "productCode")
    Inventory toDomain(InventoryEntity inventoryEntity);

    @Mapping(target = "inventory", source = "inventoryEntity")
    InventoryAggregate toAggregate(InventoryEntity inventoryEntity);

    List<InventoryAggregate> toAggregate(List<InventoryEntity> inventoryEntity);
}
