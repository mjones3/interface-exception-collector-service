package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class RecoveredPlasmaShipmentCriteria implements Validatable {

    private Integer id;
    private String customerCode;
    private String productType;

    public RecoveredPlasmaShipmentCriteria(Integer id, String customerCode, String productType) {
        this.id = id;
        this.customerCode = customerCode;
        this.productType = productType;
        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }

        if (this.customerCode == null || this.customerCode.isBlank()) {
            throw new IllegalArgumentException("Customer code cannot be null or blank");
        }

        if (this.productType == null || this.productType.isBlank()) {
            throw new IllegalArgumentException("Product type cannot be null or blank");
        }

    }
}
