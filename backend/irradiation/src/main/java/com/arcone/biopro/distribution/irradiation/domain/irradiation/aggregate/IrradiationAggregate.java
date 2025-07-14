package com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Batch;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;

import java.util.List;

public class IrradiationAggregate {
    private final Device device;
    private final List<Inventory> inventories;
    private final Batch batch;

    public IrradiationAggregate(Device device, List<Inventory> inventories, Batch batch) {
        this.device = device;
        this.inventories = inventories;
        this.batch = batch;
    }

    public boolean validateDevice(Location targetLocation) {
        return device != null && device.isAtLocation(targetLocation);
    }

    public boolean validateDeviceIsInUse(Batch batch) {
        return batch != null && batch.isActive();
    }

    public List<Inventory> getValidInventoriesForIrradiation(Location targetLocation) {
        return inventories.stream()
            .filter(inventory -> isValidInventoryForIrradiation(inventory, targetLocation))
            .toList();
    }

    private boolean isValidInventoryForIrradiation(Inventory inventory, Location targetLocation) {
        return "AVAILABLE".equals(inventory.getStatus()) &&
            inventory.getLocation().equals(targetLocation);
    }


    public Device getDevice() {
        return device;
    }

    public List<Inventory> getInventories() {
        return inventories;
    }
}
