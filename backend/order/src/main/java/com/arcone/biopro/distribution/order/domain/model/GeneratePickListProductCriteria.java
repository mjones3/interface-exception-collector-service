package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class GeneratePickListProductCriteria implements Validatable {

    private String productFamily;
    private String bloodType;

    public GeneratePickListProductCriteria(String productFamily, String bloodType) {
        this.productFamily = productFamily;
        this.bloodType = bloodType;
        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.productFamily == null || this.productFamily.isBlank()) {
            throw new IllegalArgumentException("productFamily cannot be null or blank");
        }

        if (this.bloodType == null || this.bloodType.isBlank()) {
            throw new IllegalArgumentException("bloodType cannot be null or blank");
        }
    }
}
