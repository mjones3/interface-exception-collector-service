package com.arcone.biopro.distribution.irradiation.adapter.in.socket.dto;

import com.arcone.biopro.distribution.irradiation.application.dto.Product;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhCriteria;

import java.io.Serializable;
import java.util.List;

public record Inventory(String productFamily, AboRhCriteria aboRh, Integer quantityAvailable, List<Product> shortDateProducts) implements Serializable {
}
