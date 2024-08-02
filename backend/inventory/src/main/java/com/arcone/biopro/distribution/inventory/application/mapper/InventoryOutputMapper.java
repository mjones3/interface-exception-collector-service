package com.arcone.biopro.distribution.inventory.application.mapper;

import com.arcone.biopro.distribution.inventory.application.dto.InventoryOutput;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryOutputMapper {

    InventoryOutput toOutput(Inventory domain);
}
