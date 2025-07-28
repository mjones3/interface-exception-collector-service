package com.arcone.biopro.distribution.irradiation.infrastructure.mapper;

import com.arcone.biopro.distribution.irradiation.application.mapper.MapperUtils;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client.InventoryOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = MapperUtils .class)
public interface InventoryOutputMapper {
    @Mapping(target = "unitNumber.value", source = "unitNumber")
    @Mapping(target = "location.value", source = "location")
    @Mapping(target = "status", source = "inventoryStatus")
    @Mapping(source = ".", target = "isImported", qualifiedByName = "hasImportedFlag")
    Inventory toDomain(InventoryOutput inventoryOutput);
}

