package com.arcone.biopro.distribution.shipping.domain.model.vo;

import com.arcone.biopro.distribution.shipping.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class Product implements Validatable {

    private String unitNumber;
    private String productCode;

    public Product(String unitNumber, String productCode) {
        this.unitNumber = unitNumber;
        this.productCode = productCode;
        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.unitNumber == null || this.unitNumber.isBlank()) {
            throw new IllegalArgumentException("Unit Number cannot be null or blank");
        }

        if (this.productCode == null || this.productCode.isBlank()) {
            throw new IllegalArgumentException("Product Code cannot be null or blank");
        }
    }
}
