package com.arcone.biopro.distribution.receiving.application.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum UseCaseMessageType {

    ENTER_SHIPPING_INFORMATION_ERROR(1,  UseCaseNotificationType.SYSTEM, "Not able to get Shipping Information. Contact Support."),
    VALIDATE_DEVICE_ERROR(2,  UseCaseNotificationType.WARN, "Thermometer does not exist."),
    VALIDATE_TEMPERATURE_SYSTEM_ERROR(3,  UseCaseNotificationType.SYSTEM, "Not able to validate temperature. Contact Support."),
    VALIDATE_TRANSIT_TIME_SYSTEM_ERROR(4,  UseCaseNotificationType.SYSTEM, "Not able to validate transit time. Contact Support."),
    IMPORT_CREATE_SUCCESS(5,  UseCaseNotificationType.SYSTEM, "Import created successfully.");

    Integer code;
    UseCaseNotificationType type;
    String message;

}
