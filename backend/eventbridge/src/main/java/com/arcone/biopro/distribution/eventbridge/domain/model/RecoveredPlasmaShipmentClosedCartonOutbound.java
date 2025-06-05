package com.arcone.biopro.distribution.eventbridge.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class RecoveredPlasmaShipmentClosedCartonOutbound implements Validatable {

    private final String cartonNumber;
    private final int totalProducts;
    private final List<RecoveredPlasmaShipmentClosedCartonItemOutbound> packedProducts;

    public RecoveredPlasmaShipmentClosedCartonOutbound(String cartonNumber, int totalProducts
        , List<RecoveredPlasmaShipmentClosedCartonItemOutbound> packedProducts) {
        this.cartonNumber = cartonNumber;
        this.totalProducts = totalProducts;
        this.packedProducts = packedProducts;
    }

    @Override
    public void checkValid() {
        if (cartonNumber == null || cartonNumber.isBlank()) {
            throw new IllegalStateException("Carton number is null");
        }

        if (packedProducts == null) {
            throw new IllegalStateException("Packed products is null");
        }

    }
}
