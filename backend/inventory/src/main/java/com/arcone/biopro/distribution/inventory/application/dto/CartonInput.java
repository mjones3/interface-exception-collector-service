package com.arcone.biopro.distribution.inventory.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record CartonInput(
    String cartonNumber,
    List<ShipmentPackedProductInput> packedProducts
) {}