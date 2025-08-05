package com.arcone.biopro.distribution.inventory.application.dto;

import java.util.List;

public record GetAllAvailableInventoriesOutput(String location, List<InventoryFamily> inventories) {
}
