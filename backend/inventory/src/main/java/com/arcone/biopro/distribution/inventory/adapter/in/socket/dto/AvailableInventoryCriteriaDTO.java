package com.arcone.biopro.distribution.inventory.adapter.in.socket.dto;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;

import java.io.Serializable;

public record AvailableInventoryCriteriaDTO(ProductFamily productFamily, AboRhCriteria bloodType) implements Serializable {
}
