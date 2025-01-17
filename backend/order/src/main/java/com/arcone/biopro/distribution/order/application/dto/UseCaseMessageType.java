package com.arcone.biopro.distribution.order.application.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum UseCaseMessageType {

    INVENTORY_SERVICE_IS_DOWN(1,  UseCaseNotificationType.ERROR, "Inventory Service is down."),
    ORDER_COMPLETED_SUCCESSFULLY(2,  UseCaseNotificationType.SUCCESS, "Order completed successfully"),
    COMPLETE_ORDER_ERROR(3,  UseCaseNotificationType.ERROR, "Cannot complete order");

    Integer code;
    UseCaseNotificationType type;
    String message;

}
