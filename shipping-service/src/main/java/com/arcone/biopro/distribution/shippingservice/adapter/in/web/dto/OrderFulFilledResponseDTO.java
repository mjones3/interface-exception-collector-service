package com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto;

import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderPriority;
import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.OrderStatus;
import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record OrderFulFilledResponseDTO(

    Long id,
    Long orderNumber,
    OrderPriority priority,
    OrderStatus status,
    ZonedDateTime createDate

) implements Serializable {
}
