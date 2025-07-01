package com.arcone.biopro.distribution.eventbridge.domain.model;

import lombok.Builder;

@Builder
public record OrderRejectedOutbound(
    String externalId,
    String rejectedReason,
    String operation,
    String transactionId
) {
    public OrderRejectedOutbound {
        if (externalId == null) throw new IllegalArgumentException("externalId cannot be null");
        if (rejectedReason == null) throw new IllegalArgumentException("rejectedReason cannot be null");
        if (operation == null) throw new IllegalArgumentException("operation cannot be null");
        if (transactionId == null) throw new IllegalArgumentException("transactionId cannot be null");
    }
}