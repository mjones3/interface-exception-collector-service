package com.arcone.biopro.distribution.customer.application.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.ZonedDateTime;

public record EventMessage<T>(
    String eventId,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    ZonedDateTime occurredOn,
    String eventType,
    String eventVersion,
    T payload
) {
}
