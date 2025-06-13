package com.arcone.biopro.distribution.inventory.adapter.in.listener.discarded;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

@Schema(
    name = "ProductDiscarded",
    title = "ProductDiscarded",
    description = "Message for discarded product process"
)
public record ProductDiscardedMessage(
    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "Key for the reason description")
    String reasonDescriptionKey,

    @Schema(description = "Additional comments")
    String comments,

    @Schema(description = "User who triggered the discard")
    String triggeredBy,

    @Schema(description = "User who performed the discard")
    String performedBy,

    @Schema(description = "Date and time when the discard was created")
    ZonedDateTime createDate
) {
}
