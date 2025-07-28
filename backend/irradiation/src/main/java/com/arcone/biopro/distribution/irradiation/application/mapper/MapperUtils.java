package com.arcone.biopro.distribution.irradiation.application.mapper;

import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client.InventoryOutput;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface MapperUtils {
    @Named("hasImportedFlag")
    default boolean hasImportedFlag(InventoryOutput inventoryOutput) {
        if (inventoryOutput.properties() == null) {
            return false;
        }
        return inventoryOutput.properties().stream().anyMatch(property -> "IMPORTED".equals(property.getKey()) && "Y".equals(property.getValue()));
    }
}
