package com.arcone.biopro.distribution.inventory.domain.model.vo;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;

import java.time.ZonedDateTime;

public record History(
    InventoryStatus inventoryStatus,
    String reason,
    String comments,
    ZonedDateTime createDate) {

    public History(InventoryStatus inventoryStatus, String reason, String comments) {
        this(inventoryStatus, reason, comments, ZonedDateTime.now());
    }
}
