package com.arcone.biopro.distribution.eventbridge.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Schema(
    name = "OrderRejectedOutbound",
    title = "OrderRejectedOutbound",
    description = "Order Rejected Outbound Event"
)
@Builder
public record OrderRejectedOutboundPayload(
    @Schema(name = "externalId", title = "External ID", description = "The external order ID", requiredMode = REQUIRED)
    String externalId,
    @Schema(name = "rejectedReason", title = "Rejected reason", description = "The reason for rejection", requiredMode = REQUIRED)
    String rejectedReason,
    @Schema(name = "operation", title = "Operation", description = "The operation that was rejected", requiredMode = REQUIRED)
    String operation,
    @Schema(name = "transactionId", title = "Transaction ID", description = "The transaction ID", requiredMode = NOT_REQUIRED)
    String transactionId
) implements Serializable {
}
