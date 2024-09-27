package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class GeneratePickListCommand implements Validatable {
    private String locationCode;
    private List<GeneratePickListProductCriteria> productCriteria;

    public GeneratePickListCommand(String locationCode, List<GeneratePickListProductCriteria> productCriteria) {
        this.locationCode = locationCode;
        this.productCriteria = productCriteria;
    }

    @Override
    public void checkValid() {

        if (this.locationCode == null || this.locationCode.isBlank()) {
            throw new IllegalArgumentException("locationCode cannot be null or blank");
        }

        if (this.productCriteria == null || this.productCriteria.isEmpty()) {
            throw new IllegalArgumentException("productCriteria cannot be null or empty");
        }

    }
}
