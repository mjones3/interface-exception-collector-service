package com.arcone.biopro.distribution.receiving.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class InternalTransferItem implements Validatable {

    private final String unitNumber;
    private final String productCode;
    private final String productDescription;

    public InternalTransferItem(String unitNumber, String productCode, String productDescription) {
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.productDescription = productDescription;
        checkValid();
    }

    @Override
    public void checkValid() {
        if (unitNumber == null || unitNumber.isBlank()) {
            throw new IllegalArgumentException("Unit number is required");
        } else if (productCode == null || productCode.isBlank()) {
            throw new IllegalArgumentException("Product code is required");
        } else if (productDescription == null || productDescription.isBlank()) {
            throw new IllegalArgumentException("Product description is required");
        }
    }
}
