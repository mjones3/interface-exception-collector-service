package com.arcone.biopro.distribution.order.domain.model.vo;

import com.arcone.biopro.distribution.order.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class OrderNumber implements Validatable {

    private Long orderNumber;

    public OrderNumber(Long orderNumber) {
        this.orderNumber = orderNumber;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.orderNumber == null) {
            throw new IllegalArgumentException("orderNumber cannot be null");
        }
    }

}
