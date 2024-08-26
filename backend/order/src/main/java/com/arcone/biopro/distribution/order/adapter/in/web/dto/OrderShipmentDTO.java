package com.arcone.biopro.distribution.order.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record OrderShipmentDTO(
   Long id,
   Long orderId,
   Long shipmentId,
   String shipmentStatus,
   ZonedDateTime createDate
) implements Serializable {
}
