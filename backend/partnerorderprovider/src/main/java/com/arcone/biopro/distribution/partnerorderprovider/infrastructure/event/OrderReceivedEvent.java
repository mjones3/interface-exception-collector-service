package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.event;

import com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto.OrderDTO;

import java.io.Serializable;

public record OrderReceivedEvent(
    OrderDTO orderDTO
) implements Serializable {

}
