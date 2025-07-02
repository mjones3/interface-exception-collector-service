package com.arcone.biopro.distribution.irradiation.adapter.in.socket.mapper;

import com.arcone.biopro.distribution.irradiation.adapter.in.socket.dto.*;
import com.arcone.biopro.distribution.irradiation.application.dto.*;
import com.arcone.biopro.distribution.irradiation.domain.model.vo.NotificationMessage;
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

    @Mapping(target = "inventoryLocation", source = "locationCode")
    @Mapping(target = "shortDescription", ignore = true)
    @Mapping(target = "expirationDate", ignore = true)
    @Mapping(target = "collectionDate", ignore = true)
    @Mapping(target = "productFamily", ignore = true)
    @Mapping(target = "aboRh", ignore = true)
    @Mapping(target = "collectionTimeZone", ignore = true)
    @Mapping(target = "collectionLocation", ignore = true)
    @Mapping(target = "isLicensed", ignore = true)
    @Mapping(target = "weight", ignore = true)
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
