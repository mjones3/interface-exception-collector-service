package com.arcone.biopro.distribution.inventory.adapter.in.socket.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;

import java.io.Serializable;

public record AvailableInventoryCriteriaDTO(String productFamily, AboRhCriteria bloodType) implements Serializable {
}
