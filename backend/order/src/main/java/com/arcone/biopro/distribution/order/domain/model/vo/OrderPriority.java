package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class OrderPriority implements Validatable {

    private String orderPriority;

    public OrderPriority(String orderPriority) {
        this.orderPriority = orderPriority;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (orderPriority == null || orderPriority.isBlank()) {
            throw new IllegalArgumentException("orderPriority cannot be null or blank");
        }
    }

}
