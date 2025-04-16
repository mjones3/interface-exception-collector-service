package com.arcone.biopro.distribution.recoveredplasmashipping.application.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum UseCaseMessageType {

    INVENTORY_SERVICE_IS_DOWN(1,  UseCaseNotificationType.ERROR, "Inventory Service is down."),
    SHIPMENT_CREATED_SUCCESS(2,  UseCaseNotificationType.SUCCESS, "Shipment created successfully"),
    CARTON_CREATED_SUCCESS(3,  UseCaseNotificationType.SUCCESS, "Carton created successfully");

    Integer code;
    UseCaseNotificationType type;
    String message;

}
