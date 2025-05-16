package com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

@Schema(
    name = "UpdateQuarantined",
    title = "UpdateQuarantined",
    description = "Message for updating quarantined product"
)
public record UpdateQuarantinedMessage(
    @Schema(description = "Quarantine identifier")
    Long id,

    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "New reason for quarantine")
    String newReason,

    @Schema(description = "Additional comments")
    String comments,

    @Schema(description = "Indicates if quarantine stops manufacturing process")
    Boolean stopsManufacturing,

    @Schema(description = "User who performed the quarantine update")
    String performedBy,

    @Schema(description = "Date and time when quarantine update was created")
    ZonedDateTime createDate
) {
}
