package com.arcone.biopro.distribution.irradiation.application.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RecoveredPlasmaShipmentClosedInput(
    String shipmentNumber,
    List<CartonInput> cartonList
) {}
