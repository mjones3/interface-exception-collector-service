package com.arcone.biopro.distribution.inventory.domain.model;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ErrorMessage;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
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

    public InventoryAggregate checkIfIsValidToShip(String location) {

        switch (inventory.getInventoryStatus()) {
            case QUARANTINED:  errorMessage = ErrorMessage.INVENTORY_IS_QUARANTINED; break;
            case DISCARDED:  errorMessage = ErrorMessage.INVENTORY_IS_DISCARDED; break;
            case UNSUITABLE:  errorMessage = ErrorMessage.INVENTORY_IS_UNSUITABLE; break;
        }

        if (isExpired()) {
            errorMessage =  ErrorMessage.INVENTORY_IS_EXPIRED;
        }

        if (!inventory.getLocation().equals(location)) {
            errorMessage =  ErrorMessage.INVENTORY_NOT_FOUND_IN_LOCATION;
        }

        return this;
    }

    public InventoryAggregate completeShipment() {
        inventory.setInventoryStatus(InventoryStatus.SHIPPED);
        return this;
    }

    public InventoryAggregate updateStorage(String deviceStored, String storageLocation) {
        inventory.setDeviceStored(deviceStored);
        inventory.setStorageLocation(storageLocation);
        return this;
    }

}
