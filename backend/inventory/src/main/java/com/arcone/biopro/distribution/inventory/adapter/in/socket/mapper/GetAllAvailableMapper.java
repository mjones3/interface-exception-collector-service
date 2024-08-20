package com.arcone.biopro.distribution.inventory.adapter.in.socket.mapper;

import com.arcone.biopro.distribution.inventory.adapter.in.socket.dto.*;
import com.arcone.biopro.distribution.inventory.application.dto.*;
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

    @Mapping(target = "location", source = "locationCode")
    @Mapping(target = "shortDescription", ignore = true)
    @Mapping(target = "expirationDate", ignore = true)
    @Mapping(target = "collectionDate", ignore = true)
    @Mapping(target = "productFamily", ignore = true)
    @Mapping(target = "aboRh", ignore = true)
    InventoryInput toInput(InventoryValidationRequest dto);

    @Mapping(target = "inventoryResponseDTO", source = "inventoryOutput")
    @Mapping(target = "inventoryNotificationDTO.errorCode", source = "errorMessage.code")
    @Mapping(target = "inventoryNotificationDTO.errorMessage", source = "errorMessage.description")
    InventoryValidationResponseDTO toResponse(ValidateInventoryOutput output);

    @Mapping(target = "locationCode", source = "location")
    @Mapping(target = "productDescription", source = "shortDescription")
    InventoryResponseDTO toResponse(InventoryOutput output);

}
