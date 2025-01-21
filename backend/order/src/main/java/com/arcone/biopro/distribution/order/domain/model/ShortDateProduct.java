package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ShortDateProduct implements Validatable {

    private String unitNumber;
    private String productCode;
    private String aboRh;
    private String storageLocation;

    public ShortDateProduct(String unitNumber, String productCode , String aboRh, String storageLocation) {
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        this.aboRh = aboRh;
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
        if (this.aboRh == null) {
            throw new IllegalArgumentException("aboRh cannot be null or blank");
        }
    }
}
