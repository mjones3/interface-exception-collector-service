package com.arcone.biopro.distribution.inventory.application.dto;


public record UpdateQuarantineInput(Product product, Long quarantineId, String reason, String comments, boolean stopsManufacturing ) {
}
