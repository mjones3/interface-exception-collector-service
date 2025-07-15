package com.arcone.biopro.distribution.irradiation.adapter.in.listener;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.ZonedDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(
    name = "DeviceCreated",
    title = "DeviceCreated",
    description = "Message for device modified process"
)
public record DeviceCreated(
    @Schema(
        title = "ID",
        description = "Id of the device",
        requiredMode = REQUIRED
    )
    String id,

    @Schema(
        title = "Location",
        description = "Device location",
        example = "1FS",
        requiredMode = REQUIRED
    )
    String location,

    @Schema(
        title = "Device Category",
        description = "Device category",
        requiredMode = REQUIRED
    )
    String deviceCategory,

    @Schema(
        title = "Status",
        description = "Device status",
        example = "AVAILABLE",
        requiredMode = REQUIRED
    )
    String status,

    @Schema(
        title = "Created date",
        description = "Device created date",
        requiredMode = REQUIRED
    )
    ZonedDateTime createDate,

        @Schema(
            title = "Modified date",
            description = "Device modified date",
            requiredMode = REQUIRED
        )
    ZonedDateTime modificationDate

) {}
