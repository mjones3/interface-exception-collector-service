package com.arcone.biopro.distribution.order.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;


@Schema(
    name = "OrderRejectedPayload",
    title = "OrderRejectedPayload",
    description = "Order Rejected Event Payload"
)
@Builder
public record OrderRejectedDTO(
    @Schema(
        title = "External Order ID",
        description = "The external order ID",
        example = "ABC56865",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String externalId,
    @Schema(
        title = "Rejected Reason",
        description = "The rejected reason",
        example = "Order already exists",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String rejectedReason
) implements Serializable {

}
