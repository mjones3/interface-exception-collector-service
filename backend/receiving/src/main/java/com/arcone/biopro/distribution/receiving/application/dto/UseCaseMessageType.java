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
    VALIDATE_DEVICE_ERROR(2,  UseCaseNotificationType.WARN, "Device not found.");

    Integer code;
    UseCaseNotificationType type;
    String message;

}
