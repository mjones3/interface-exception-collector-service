package com.arcone.biopro.distribution.irradiation.adapter.in.listener;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "CheckInCompleted",
    title = "CheckInCompleted",
    description = "Message for product modified process"
)
public record CheckInCompleted(
    @Schema(
        title = "ID",
        description = "Id of the product",
        requiredMode = REQUIRED
    )
    String id,

    @Schema(
        title = "Location",
        description = "Product location",
        example = "1FS",
        requiredMode = REQUIRED
    )
    String location,

    @Schema(
        title = "Device Category",
        description = "Product device category",
        requiredMode = REQUIRED
    )
    String deviceCategory,

    @Schema(
        title = "Status",
        description = "Product status",
        example = "AVAILABLE",
        requiredMode = REQUIRED
    )
    String status
) {}
