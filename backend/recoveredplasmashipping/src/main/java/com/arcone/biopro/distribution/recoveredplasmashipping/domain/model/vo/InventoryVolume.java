package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class InventoryVolume implements Validatable {

    private final String type;
    private final Integer value;
    private final String unit;

    public InventoryVolume(String type, Integer value, String unit) {
        this.type = type;
        this.value = value;
        this.unit = unit;
        checkValid();
    }

    @Override
    public void checkValid() {
        if(type == null || type.isBlank()){
            throw new IllegalArgumentException("Volume Type is required");
        }

        if(value == null || value <= 0 ){
            throw new IllegalArgumentException("Volume Value is required");
        }
        if(unit == null || unit.isBlank()){
            throw new IllegalArgumentException("Volume Unit is required");
        }

    }
}
