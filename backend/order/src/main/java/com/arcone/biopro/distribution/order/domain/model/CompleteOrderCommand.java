package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class CompleteOrderCommand implements Validatable {

    private Long orderId;
    private String employeeId;
    private String comments;

    public CompleteOrderCommand(Long orderId, String employeeId, String comments) {
        this.orderId = orderId;
        this.employeeId = employeeId;
        this.comments = comments;

        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.orderId == null) {
            throw new IllegalArgumentException("orderID cannot be null");
        }

        if (this.employeeId == null || this.employeeId.isEmpty()) {
            throw new IllegalArgumentException("employeeId cannot be null or empty");
        }
    }
}
