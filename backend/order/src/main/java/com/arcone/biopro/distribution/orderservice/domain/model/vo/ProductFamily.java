package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ProductFamily implements Validatable {

    private String productFamily;

    public ProductFamily(String productFamily) {
        this.productFamily = productFamily;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (productFamily == null || productFamily.isBlank()) {
            throw new IllegalArgumentException("productFamily cannot be null or blank");
        }
    }

}
