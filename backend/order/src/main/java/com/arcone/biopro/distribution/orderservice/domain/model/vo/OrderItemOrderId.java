package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class OrderItemOrderId implements Validatable {

    private Long orderId;

    public OrderItemOrderId(Long orderId) {
        this.orderId = orderId;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.orderId == null) {
            throw new IllegalArgumentException("orderId cannot be null");
        }
    }

}
