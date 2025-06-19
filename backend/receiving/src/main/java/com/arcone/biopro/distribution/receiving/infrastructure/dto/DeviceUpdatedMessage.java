package com.arcone.biopro.distribution.receiving.infrastructure.dto;

import com.arcone.biopro.distribution.receiving.infrastructure.event.AbstractEvent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(
    name = "DeviceUpdated",
    title = "DeviceUpdated",
    description = "Device Updated Event"
)
@Builder
public class DeviceUpdatedMessage extends AbstractEvent<DevicePayload> {

}
