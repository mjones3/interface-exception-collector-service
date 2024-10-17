package com.arcone.biopro.distribution.shipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class Reason {

    private  final Long id;
    private final String type;
    private final String reasonKey;
    private final boolean requireComments;
    private final Integer orderNumber;
    private final boolean active;

    public Reason(Long id, String type, String reasonKey, boolean requireComments, Integer orderNumber, boolean active) {
        this.id = id;
        this.type = type;
        this.reasonKey = reasonKey;
        this.requireComments = requireComments;
        this.orderNumber = orderNumber;
        this.active = active;

        checkValid();
    }


    public void checkValid() {
        if (this.id == null ) {
            throw new IllegalArgumentException("ID cannot be null or blank");
        }
        if (this.type == null || this.type.isBlank()) {
            throw new IllegalArgumentException("type cannot be null or blank");
        }
        if (this.reasonKey == null || this.reasonKey.isBlank()) {
            throw new IllegalArgumentException("reasonKey cannot be null or blank");
        }
        if (this.orderNumber == null) {
            throw new IllegalArgumentException("orderNumber cannot be null");
        }
    }
}
