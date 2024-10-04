package com.arcone.biopro.distribution.order.application.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Builder
public record ShipmentCompletedItemProductPayload(
    String unitNumber,
    String productFamily,
    String productCode,
    String aboRh,
    ZonedDateTime collectionDate,
    LocalDateTime expirationDate,
    ZonedDateTime createDate,
    List<Map<String,String>> attributes
) implements Serializable {
}
