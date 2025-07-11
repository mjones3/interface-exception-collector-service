package com.arcone.biopro.distribution.irradiation.domain.irradiation.entity;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;

public class Device {
    private final DeviceId deviceId;
    private final Location location;
    private final String status;

    public Device(DeviceId deviceId, Location location, String status) {
        this.deviceId = deviceId;
        this.location = location;
        this.status = status;
    }

    public boolean isAtLocation(Location targetLocation) {
        return this.location.equals(targetLocation);
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public Location getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }
}