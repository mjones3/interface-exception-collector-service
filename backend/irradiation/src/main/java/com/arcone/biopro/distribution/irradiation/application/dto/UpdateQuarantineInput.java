package com.arcone.biopro.distribution.irradiation.application.dto;


public record UpdateQuarantineInput(Product product, Long quarantineId, String reason, String comments) {
}
