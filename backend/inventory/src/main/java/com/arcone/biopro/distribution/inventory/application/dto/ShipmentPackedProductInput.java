package com.arcone.biopro.distribution.inventory.application.dto;

import lombok.Builder;

@Builder
public record ShipmentPackedProductInput(
    String unitNumber,
    String productCode,
    String status
) {}