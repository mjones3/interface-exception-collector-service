package com.arcone.biopro.distribution.inventory.adapter.in.socket.mapper;

import com.arcone.biopro.distribution.inventory.adapter.in.socket.dto.*;
import com.arcone.biopro.distribution.inventory.application.dto.*;
import com.arcone.biopro.distribution.inventory.domain.model.vo.NotificationMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GetAllAvailableMapper {

    @Mapping(target = "location", source = "locationCode")
    @Mapping(target = "inventoryCriteria", source = "availableInventoryCriteriaDTOS")
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
    @Mapping(target = "inventoryNotificationsDTO", source = "notificationMessages")
    InventoryValidationResponseDTO toResponse(ValidateInventoryOutput output);

    @Mapping(target = "errorName", source = "name")
    @Mapping(target = "errorCode", source = "code")
    @Mapping(target = "errorMessage", source = "message")
    @Mapping(target = "errorType", source = "type")
    InventoryNotificationDTO toResponse(NotificationMessage notificationMessage);

    @Mapping(target = "locationCode", source = "location")
    @Mapping(target = "productDescription", source = "shortDescription")
    InventoryResponseDTO toResponse(InventoryOutput output);
}
