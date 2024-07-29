package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Builder
public record ShipmentItemShortDateProductResponseDTO(
    Long id,
    Long shipmentItemId,
    String unitNumber,
    String productCode,
    String storageLocation,
    String comments,
    ZonedDateTime createDate,
    ZonedDateTime modificationDate

) implements Serializable {
}
