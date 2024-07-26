package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
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
        // TODO Fix this with generate order number from the application instead of database approach.
        /*if (this.orderNumber == null) {
            throw new IllegalArgumentException("orderNumber cannot be null");
        }*/
    }

}
