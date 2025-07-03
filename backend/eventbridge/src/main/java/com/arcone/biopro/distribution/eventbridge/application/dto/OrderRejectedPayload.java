package com.arcone.biopro.distribution.eventbridge.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;

@Schema(
    name = "OrderRejectedPayload",
    title = "OrderRejectedPayload",
    description = "Order Rejected Event Payload"
)
@Builder
public record OrderRejectedPayload(
    @Schema(name = "externalId", title = "External ID", description = "The external order ID", example = "bb5df8a6-e9ec-42ea-90e8-ac9b7326c8ae", requiredMode = REQUIRED)
    String externalId,
    @Schema(name = "rejectedReason", title = "Rejected reason", description = "The reason for rejection", example = "Domain not found", requiredMode = REQUIRED)
    String rejectedReason,
    @Schema(name = "operation", title = "Operation", description = "The operation that was rejected", example = "MODIFY_ORDER", requiredMode = REQUIRED)
    String operation,
    @Schema(name = "transactionId", title = "Transaction ID", description = "The transaction ID", requiredMode = NOT_REQUIRED)
    String transactionId
) implements Serializable {
}