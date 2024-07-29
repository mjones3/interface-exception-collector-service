package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ShipmentType implements Validatable {

    private String shipmentType;

    public ShipmentType(String shipmentType) {
        this.shipmentType = shipmentType;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (shipmentType == null || shipmentType.isBlank()) {
            throw new IllegalArgumentException("shipmentType cannot be null or blank");
        }
    }

}
