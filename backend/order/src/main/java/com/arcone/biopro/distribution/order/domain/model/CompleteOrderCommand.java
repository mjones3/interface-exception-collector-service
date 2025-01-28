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
    private Boolean createBackOrder;

    public CompleteOrderCommand(Long orderId, String employeeId, String comments , Boolean createBackOrder) {
        this.orderId = orderId;
        this.employeeId = employeeId;
        this.comments = comments;
        this.createBackOrder = createBackOrder;

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

        if (this.createBackOrder == null) {
            throw new IllegalArgumentException("create back order cannot be null");
        }
    }
}
