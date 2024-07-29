package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ProductCategory implements Validatable {

    private String productCategory;

    public ProductCategory(String productCategory) {
        this.productCategory = productCategory;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (productCategory == null || productCategory.isBlank()) {
            throw new IllegalArgumentException("productCategory cannot be null or blank");
        }
    }
}
