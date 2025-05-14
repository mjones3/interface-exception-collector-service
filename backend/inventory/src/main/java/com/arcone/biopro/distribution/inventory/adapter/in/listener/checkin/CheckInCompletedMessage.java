package com.arcone.biopro.distribution.inventory.adapter.in.listener.checkin;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

@Schema(
    name = "CheckInCompleted",
    title = "CheckInCompleted",
    description = "Message for completed check-in process"
)
public record CheckInCompletedMessage(
    @Schema(description = "Unit number identifier")
    String unitNumber,

    @Schema(description = "Product code")
    String productCode,

    @Schema(description = "Product description")
    String productDescription,

    @Schema(description = "Product family type")
    String productFamily,

    @Schema(description = "Collection location")
    String collectionLocation,

    @Schema(description = "Time when the draw was performed", format = "date-time")
    ZonedDateTime drawTime,

    @Schema(description = "Blood type ABO and RH factor")
    AboRhType aboRh
) {
}
