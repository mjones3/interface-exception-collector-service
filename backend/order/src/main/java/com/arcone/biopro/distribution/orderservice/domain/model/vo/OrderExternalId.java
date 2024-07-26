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
    private static final String ALPHANUMERIC_PATTERN = "^[a-zA-Z0-9]+$";

    public OrderExternalId(String orderExternalId) {
        this.orderExternalId = orderExternalId;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.orderExternalId == null || this.orderExternalId.isBlank()) {
            throw new IllegalArgumentException("orderExternalId cannot be null or blank");
        }

        if(!isValidId(orderExternalId)){
            throw new IllegalArgumentException("orderExternalId is not a valid Format");
        }
    }

    private static boolean isValidId(String orderExternalId) {
        return orderExternalId.matches(ALPHANUMERIC_PATTERN);
    }
}
