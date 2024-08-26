package com.arcone.biopro.distribution.inventory.domain.model.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ProductFamily {

    PLASMA_TRANSFUSABLE(30),
    WHOLE_BLOOD(5);

    Integer timeFrame;
}
