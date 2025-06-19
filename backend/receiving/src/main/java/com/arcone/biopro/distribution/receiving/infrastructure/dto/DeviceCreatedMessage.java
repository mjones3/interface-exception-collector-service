package com.arcone.biopro.distribution.receiving.infrastructure.dto;

import com.arcone.biopro.distribution.receiving.infrastructure.event.AbstractEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(
    name = "DeviceCreated",
    title = "DeviceCreated",
    description = "Device Created Event"
)

@Builder
public class DeviceCreatedMessage extends AbstractEvent<DevicePayload> {
}
