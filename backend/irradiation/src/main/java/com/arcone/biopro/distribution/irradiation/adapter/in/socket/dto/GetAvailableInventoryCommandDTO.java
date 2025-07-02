package com.arcone.biopro.distribution.irradiation.adapter.in.socket.dto;


import java.io.Serializable;
import java.util.List;

public record GetAvailableInventoryCommandDTO(String locationCode, List<AvailableInventoryCriteriaDTO> availableInventoryCriteriaDTOS) implements Serializable {
}
