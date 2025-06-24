package com.arcone.biopro.distribution.eventbridge.domain.model;

import lombok.Builder;

@Builder
public record OrderRejectedOutbound(
    String externalId,
    String rejectedReason,
    String operation,
    String transactionId
) {
}