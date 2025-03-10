package com.arcone.biopro.distribution.shipping.infrastructure.listener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Schema(
    name = "ExternalTransferCompletedPayload",
    title = "ExternalTransferCompletedPayload",
    description = "External Transfer Completed Event Payload"
)
@Builder
public record ExternalTransferCompletedOutputPayload(
    @Schema(
        title = "customerCodeTo",
        description = "Customer Code To",
        example = "ABC123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String customerCodeTo,
    @Schema(
        title = "customerCodeFrom",
        description = "Customer Code From",
        example = "ABC123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String customerCodeFrom,
    @Schema(
        title = "hospitalTransferId",
        description = "Hospital Transfer Id",
        example = "#XYZ56",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    String hospitalTransferId,
    @Schema(
        title = "transferDate",
        description = "Transfer Date",
        example = "2025-01-01",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    LocalDate transferDate,
    @Schema(
        title = "Create date",
        description = "The date order was created",
        example = "2024-10-03T15:44:42.328889299Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    ZonedDateTime createDate,
    @Schema(
        title = "Create Employee ID",
        description = "The employee ID that created the record",
        example = "4c973896-5761-41fc-8217-07c5d13a004b",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String createdByEmployeeId,
    List<ExternalTransferOutputItem> externalTransferItems
) implements Serializable {


}
