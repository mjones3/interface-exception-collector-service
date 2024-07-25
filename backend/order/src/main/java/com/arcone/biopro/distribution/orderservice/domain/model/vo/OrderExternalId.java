package com.arcone.biopro.distribution.orderservice.domain.model.vo;

import com.arcone.biopro.distribution.orderservice.domain.model.Validatable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class OrderExternalId implements Validatable {

    private String orderExternalId;

    public OrderExternalId(String orderExternalId) {
        this.orderExternalId = orderExternalId;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.orderExternalId == null || this.orderExternalId.isBlank()) {
            throw new IllegalArgumentException("orderExternalId cannot be null or blank");
        }
    }

}
