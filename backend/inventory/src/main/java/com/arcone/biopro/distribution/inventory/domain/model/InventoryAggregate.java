package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ErrorMessage;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class InventoryAggregate {

    Inventory inventory;

    ErrorMessage errorMessage;

    public Boolean isExpired() {
        return inventory.getExpirationDate().isBefore(LocalDateTime.now());
    }

    public InventoryAggregate checkIfIsValidToShip() {
        if (inventory.getInventoryStatus().equals(InventoryStatus.QUARANTINED)) {
            errorMessage = ErrorMessage.STATUS_IN_QUARANTINE;
        } else if (isExpired()) {
            errorMessage =  ErrorMessage.DATE_EXPIRED;
        } else {
            errorMessage =  null;
        }

        return this;
    }
}
