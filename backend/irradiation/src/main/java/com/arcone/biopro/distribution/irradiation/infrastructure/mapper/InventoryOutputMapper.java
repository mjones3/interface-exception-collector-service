package com.arcone.biopro.distribution.irradiation.infrastructure.mapper;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client.InventoryOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryOutputMapper {
    @Mapping(target = "unitNumber.value", source = "unitNumber")
    @Mapping(target = "location.value", source = "location")
    @Mapping(target = "status", source = "inventoryStatus")
    Inventory toDomain(InventoryOutput inventoryOutput);
}

