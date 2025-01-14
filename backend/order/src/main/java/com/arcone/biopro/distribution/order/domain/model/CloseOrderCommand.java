package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class CloseOrderCommand implements Validatable {

    private Long orderId;
    private String employeeId;
    private String reason;
    private String comments;

    public CloseOrderCommand(Long orderId, String employeeId, String reason, String comments) {
        this.orderId = orderId;
        this.employeeId = employeeId;
        this.reason = reason;
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

        if (this.reason == null || this.reason.isEmpty()) {
            throw new IllegalArgumentException("reason cannot be null or empty");
        }
    }
}
