package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class OrderStatus implements Validatable {

    private String orderStatus;

    public OrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (orderStatus == null || orderStatus.isBlank()) {
            throw new IllegalArgumentException("orderStatus cannot be null or blank");
        }
    }

}
