package com.arcone.biopro.distribution.inventory.domain.model.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorMessage {
    INVENTORY_NOT_FOUND(1, "This product is expired and cannot be shipped."),
    STATUS_IN_QUARANTINE(4, "This product is quarantined and cannot be shipped."),
    DATE_EXPIRED(2, "This product is expired and cannot be shipped.");

    Integer code;
    String description;
}
