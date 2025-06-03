package com.arcone.biopro.distribution.inventory.adapter.in.listener.quarantine;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

@Schema(
    name = "QuarantineRemoved",
    title = "QuarantineRemoved",
    description = "Message for removing product from quarantine"
)
public record RemoveQuarantinedMessage(
    @Schema(description = "Quarantine identifier")
    Long id,

    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "Reason for removing from quarantine")
    String reason,

    @Schema(description = "Indicates if quarantine stops manufacturing process")
    Boolean stopsManufacturing,

    @Schema(description = "User who performed the quarantine removal")
    String performedBy,

    @Schema(description = "Date and time when quarantine removal was created")
    ZonedDateTime createDate
) {
}
