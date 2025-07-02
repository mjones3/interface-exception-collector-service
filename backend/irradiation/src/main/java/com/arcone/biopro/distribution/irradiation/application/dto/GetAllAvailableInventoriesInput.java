package com.arcone.biopro.distribution.irradiation.application.dto;

import java.util.List;

public record GetAllAvailableInventoriesInput(String location, List<InventoryCriteria> inventoryCriteria) {
}
