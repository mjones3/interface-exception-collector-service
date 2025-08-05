package com.arcone.biopro.distribution.irradiation.domain.irradiation.entity;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.BatchId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;

import java.time.LocalDateTime;

public class Batch {
    private final BatchId id;
    private final DeviceId deviceId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public Batch(BatchId id, DeviceId deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = id;
        this.deviceId = deviceId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isActive() {
        return endTime == null;
    }

    public BatchId getId() {
        return id;
    }

    public DeviceId getDeviceId() {
        return deviceId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
