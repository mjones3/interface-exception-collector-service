package com.arcone.biopro.distribution.partnerorderproviderservice.infrastructure.event;

import com.arcone.biopro.distribution.partnerorderproviderservice.infrastructure.listener.dto.OrderDTO;

import java.io.Serializable;

public record OrderReceivedEvent(
    OrderDTO orderDTO
) implements Serializable {

}
