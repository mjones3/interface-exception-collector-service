package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class ValidateInventoryCommand implements Validatable {

    private final String unitNumber;
    private final String productCode;
    private final String locationCode;

    public ValidateInventoryCommand(String unitNumber, String productCode, String locationCode) {
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.locationCode = locationCode;
        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.unitNumber == null || this.unitNumber.isBlank()) {
            throw new IllegalArgumentException("Unit Number is required");
        }

        if (this.productCode == null || this.productCode.isBlank()) {
            throw new IllegalArgumentException("Product Code is required");
        }

        if (this.locationCode == null || this.locationCode.isBlank()) {
            throw new IllegalArgumentException("Location Code is required");
        }

    }
}
