package com.arcone.biopro.distribution.shipping.application.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum UseCaseMessageType {

    EXTERNAL_TRANSFER_DATE_BEFORE_SHIP_DATE(1,  NotificationType.CAUTION, "The transfer date is before the last shipped date"),
    EXTERNAL_TRANSFER_LOCATION_DOES_NOT_MATCH(2,  NotificationType.CAUTION,"The product location doesn't match the last shipped location"),
    EXTERNAL_TRANSFER_PRODUCT_NOT_SHIPPED(3,  NotificationType.CAUTION,"This product has not been shipped"),
    EXTERNAL_TRANSFER_NOT_FOUND(4,  NotificationType.WARN,"External transfer not found"),
    EXTERNAL_TRANSFER_DUPLICATED_PRODUCT(5,  NotificationType.WARN,"Product already added"),
    ;

    Integer code;
    NotificationType type;
    String message;

}
