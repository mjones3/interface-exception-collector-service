package com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

@Schema(
    name = "ProductQuarantined",
    title = "ProductQuarantined",
    description = "Message for adding product to quarantine"
)
public record AddQuarantinedMessage(
    @Schema(description = "Quarantine identifier")
    Long id,

    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "Reason for quarantine")
    String reason,

    @Schema(description = "Additional comments")
    String comments,

    @Schema(description = "Indicates if quarantine stops manufacturing process")
    Boolean stopsManufacturing,

    @Schema(description = "User who performed the quarantine")
    String performedBy,

    @Schema(description = "Date and time when quarantine was created")
    ZonedDateTime createDate
) {
}
