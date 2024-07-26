package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ShippingMethod implements Validatable {

    private String shippingMethod;

    public ShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (shippingMethod == null || shippingMethod.isBlank()) {
            throw new IllegalArgumentException("shippingMethod cannot be null or blank");
        }
    }

}
