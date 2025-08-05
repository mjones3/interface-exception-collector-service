package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client;

import lombok.Builder;

@Builder
public record InventoryQuarantineOutput(String reason, String comments, Boolean stopsManufacturing) {
}

