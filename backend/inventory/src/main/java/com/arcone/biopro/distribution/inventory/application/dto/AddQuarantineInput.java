package com.arcone.biopro.distribution.inventory.application.dto;


public record AddQuarantineInput(Product product, Long quarantineId, String reason, String comments) {
}
