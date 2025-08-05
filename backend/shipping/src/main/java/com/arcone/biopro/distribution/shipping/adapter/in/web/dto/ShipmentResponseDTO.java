package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentPriority;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ShipmentStatus;
import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record ShipmentResponseDTO(

    Long id,
    Long orderNumber,
    ShipmentPriority priority,
    ShipmentStatus status,
    ZonedDateTime createDate

) implements Serializable {
}
