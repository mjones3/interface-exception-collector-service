package com.arcone.biopro.distribution.irradiation.application.mapper;

import com.arcone.biopro.distribution.irradiation.application.dto.BatchProductDTO;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client.InventoryOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = MapperUtils.class)
public interface BatchProductMapper {

    @Mapping(source = "inventoryStatus", target = "status")
    @Mapping(source = ".", target = "isImported", qualifiedByName = "hasImportedFlag")
    BatchProductDTO toDTO(InventoryOutput inventoryOutput);
}
