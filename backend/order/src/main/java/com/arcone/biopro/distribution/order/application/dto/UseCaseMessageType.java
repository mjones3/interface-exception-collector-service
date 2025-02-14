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
    ORDER_IS_ALREADY_COMPLETED(3,  UseCaseNotificationType.ERROR, "Order is already completed"),
    ORDER_IS_NOT_IN_PROGRESS_AND_CANNOT_BE_COMPLETED(4,  UseCaseNotificationType.ERROR, "Order is not in-progress and cannot be completed"),
    ORDER_HAS_AN_OPEN_SHIPMENT(5,  UseCaseNotificationType.ERROR, "Order has an open shipment"),
    COMPLETE_ORDER_ERROR(6,  UseCaseNotificationType.ERROR, "Cannot complete order"),
    ORDER_IS_ALREADY_CANCELLED(7,  UseCaseNotificationType.ERROR, "Order is already cancelled"),
    ORDER_IS_NOT_OPEN_AND_CANNOT_BE_CANCELLED(8, UseCaseNotificationType.ERROR, "Order is not open and cannot be cancelled"),
    NO_ORDER_TO_BE_CANCELLED(9,  UseCaseNotificationType.ERROR, "There is no order to be cancelled"),
    ;

    Integer code;
    UseCaseNotificationType type;
    String message;

}
