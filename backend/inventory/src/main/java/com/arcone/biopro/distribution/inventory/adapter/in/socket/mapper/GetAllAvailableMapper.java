package com.arcone.biopro.distribution.inventory.adapter.in.socket.mapper;

import com.arcone.biopro.distribution.inventory.adapter.in.socket.dto.AvailableInventoryCriteriaDTO;
import com.arcone.biopro.distribution.inventory.adapter.in.socket.dto.GetAvailableInventoryCommandDTO;
import com.arcone.biopro.distribution.inventory.adapter.in.socket.dto.GetAvailableInventoryResponseDTO;
import com.arcone.biopro.distribution.inventory.application.dto.GetAllAvailableInventoriesInput;
import com.arcone.biopro.distribution.inventory.application.dto.GetAllAvailableInventoriesOutput;
import com.arcone.biopro.distribution.inventory.application.dto.InventoryCriteria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GetAllAvailableMapper {

    @Mapping(target = "location", source = "locationCode")
    @Mapping(target ="inventoryCriteria", source = "availableInventoryCriteriaDTOS")
    GetAllAvailableInventoriesInput toInput(GetAvailableInventoryCommandDTO dto);

    @Mapping(target = "aboRh", source = "bloodType")
    InventoryCriteria toInput(AvailableInventoryCriteriaDTO dto);

    GetAvailableInventoryResponseDTO toResponse(GetAllAvailableInventoriesOutput output);

}
