package com.arcone.biopro.distribution.irradiation.domain.irradiation.entity;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.ProductCode;
import lombok.Builder;
import lombok.Getter;

/**
 * Domain entity representing product determination mapping.
 */
@Getter
@Builder
public class ProductDetermination {
    private final Integer id;
    private final ProductCode sourceProductCode;
    private final ProductCode targetProductCode;
    private final boolean active;

    public ProductDetermination(Integer id, ProductCode sourceProductCode, ProductCode targetProductCode, boolean active) {
        this.id = id;
        this.sourceProductCode = sourceProductCode;
        this.targetProductCode = targetProductCode;
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public boolean matches(ProductCode productCode) {
        return sourceProductCode.equals(productCode) && active;
    }
}