package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class PickListItemShortDate implements Validatable {

    private String unitNumber;
    private String productCode;
    private String storageLocation;

    public PickListItemShortDate(String unitNumber, String productCode, String storageLocation) {
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.storageLocation = storageLocation;
        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.unitNumber == null) {
            throw new IllegalArgumentException("unitNumber cannot be null");
        }
        if (this.productCode == null) {
            throw new IllegalArgumentException("productCode cannot be null or blank");
        }
        if (this.storageLocation == null) {
            throw new IllegalArgumentException("storageLocation cannot be null");
        }

    }
}
