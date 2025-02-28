package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Schema(
    name = "CancelOrderPayload",
    title = "CancelOrderPayload",
    description = "Cancel Order Received Event Payload"
)
@Builder
@Getter
public class CancelOrderDTO implements Serializable {

    @Schema(
        title = "External Order ID",
        description = "The external order ID",
        example = "ABC56865",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String externalId;

    @Schema(
        title = "Cancel Date",
        description = "The cancel date",
        example = "2025-01-01 11:09:55",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String cancelDate;

    @Schema(
        title = "Cancel Employee Code",
        description = "The cancel employee code",
        example = "ee1bf88e-2137-4a17-835a-d43e7b738374",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String cancelEmployeeCode;

    @Schema(
        title = "Cancel Reason",
        description = "The cancel reason",
        example = "Customer no longer need",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String cancelReason;
}
