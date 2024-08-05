package com.arcone.biopro.distribution.inventory.application.mapper;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryOutputMapper {

    @Mapping(target = "unitNumber", source = "unitNumber.value")
    @Mapping(target = "productCode", source = "productCode.value")
    InventoryOutput toOutput(Inventory domain);
}
