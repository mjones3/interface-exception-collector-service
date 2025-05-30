package com.arcone.biopro.distribution.inventory.application.dto;

import lombok.Builder;

@Builder
public record AddQuarantineInput(Product product, Long quarantineId, String reason, String comments) {
}
