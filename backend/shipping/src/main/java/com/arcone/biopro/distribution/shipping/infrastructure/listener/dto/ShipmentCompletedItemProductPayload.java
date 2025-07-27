package com.arcone.biopro.distribution.shipping.infrastructure.listener.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

@Builder
public record ShipmentCompletedItemProductPayload(
    String unitNumber,
    String productFamily,
    String productCode,
    String productDescription,
    String aboRh,
    ZonedDateTime collectionDate,
    LocalDateTime expirationDate,
    ZonedDateTime createDate,
    Map<String,String> attributes
) implements Serializable {
}
