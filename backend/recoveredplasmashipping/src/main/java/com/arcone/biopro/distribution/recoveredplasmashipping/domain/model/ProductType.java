package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class ProductType implements Validatable {

    private Integer id;
    private String productType;
    private String productTypeDescription;

    public ProductType(Integer id, String productType, String productTypeDescription) {
        this.id = id;
        this.productType = productType;
        this.productTypeDescription = productTypeDescription;
    }

    @Override
    public void checkValid() {

        if (this.id == null ) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        if (this.productType == null || this.productType.isBlank()) {
            throw new IllegalArgumentException("Product Type cannot be null or blank");
        }

        if (this.productTypeDescription == null || this.productTypeDescription.isBlank()) {
            throw new IllegalArgumentException("Description cannot be null or blank");
        }
    }
}
