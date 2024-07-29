package com.arcone.biopro.distribution.inventory.domain.event;

import java.io.Serializable;

public record InventoryCreatedEvent(Long id) implements Serializable {
}
