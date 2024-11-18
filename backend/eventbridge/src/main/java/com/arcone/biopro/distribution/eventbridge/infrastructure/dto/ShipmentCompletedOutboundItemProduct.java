package com.arcone.biopro.distribution.eventbridge.infrastructure.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

@Builder
public record ShipmentCompletedOutboundItemProduct(
    String unitNumber,
    String productCode,
    String bloodType,
    ZonedDateTime collectionDate,
    LocalDateTime expirationDate,
    Map<String,String> attributes
) implements Serializable {
}
