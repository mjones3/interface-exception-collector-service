package com.arcone.biopro.distribution.irradiation.domain.irradiation.aggregate;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import java.util.List;

public class IrradiationAggregate {
    private final Device device;
    private final List<Inventory> inventories;

    public IrradiationAggregate(Device device, List<Inventory> inventories) {
        this.device = device;
        this.inventories = inventories;
    }

    public boolean validateDevice(Location targetLocation) {
        return device != null && device.isAtLocation(targetLocation);
    }

    public List<Inventory> getValidInventories(Location targetLocation) {
        return inventories.stream()
                .filter(inventory -> inventory.isAvailable() && inventory.isAtLocation(targetLocation))
                .toList();
    }

    public Device getDevice() {
        return device;
    }

    public List<Inventory> getInventories() {
        return inventories;
    }
}