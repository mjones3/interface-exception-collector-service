package com.arcone.biopro.distribution.inventory.application.dto;


public record QuarantineProductInput(Product product, Long quarantineId, String quarantineReason) {
}
