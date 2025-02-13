package com.arcone.biopro.distribution.order.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class CancelOrderCommand implements Validatable {

    private String externalId;
    private String employeeId;
    private String reason;

    public CancelOrderCommand(String externalId, String employeeId, String reason ) {
        this.externalId = externalId;
        this.employeeId = employeeId;
        this.reason = reason;
        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.externalId == null) {
            throw new IllegalArgumentException("External ID cannot be null");
        }

        if (this.employeeId == null || this.employeeId.isEmpty()) {
            throw new IllegalArgumentException("employeeId cannot be null or empty");
        }

        if (this.reason == null || this.reason.isEmpty()) {
            throw new IllegalArgumentException("reason cannot be null or empty");
        }
    }
}
