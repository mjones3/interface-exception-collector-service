package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event;

import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.OrderDTO;
import lombok.Builder;

import java.io.Serializable;

@Builder
public record OrderReceivedEvent(
    String eventType,
    String eventVersion,
    OrderDTO payload
) implements Serializable {

}
