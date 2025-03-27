package com.arcone.biopro.distribution.inventory.domain.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Volume implements Serializable {

    String type;
    Integer value;
    String unit;

    public Volume(String type, Integer value, String unit) {
        this.type = type;
        this.value = value;
        this.unit = unit;
    }
}
