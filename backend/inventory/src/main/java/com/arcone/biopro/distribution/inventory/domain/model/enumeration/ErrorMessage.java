package com.arcone.biopro.distribution.inventory.domain.model.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorMessage {
    INVENTORY_NOT_FOUND_IN_LOCATION(1, "This product is not in this location and cannot be shipped."),
    INVENTORY_IS_EXPIRED(2, "This product is expired and cannot be shipped."),
    INVENTORY_IS_UNSUITABLE(3, "This product is unsuitable and cannot be shipped."),
    INVENTORY_IS_QUARANTINED(4, "This product is quarantined and cannot be shipped."),
    INVENTORY_IS_DISCARDED(5, "This product is discarded and cannot be shipped."),
    INVENTORY_NOT_EXIST(6, "This product not exist and cannot be shipped.");

    Integer code;
    String description;
}
