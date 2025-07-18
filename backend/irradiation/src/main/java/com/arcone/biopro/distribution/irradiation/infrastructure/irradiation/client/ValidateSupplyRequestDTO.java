package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.client;

/**
 * DTO for sending validation requests to the supply service.
 */
public record ValidateSupplyRequestDTO(String lotNumber, String type) {
}
