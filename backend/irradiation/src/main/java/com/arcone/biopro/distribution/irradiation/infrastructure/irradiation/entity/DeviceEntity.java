package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("bld_device")
public class DeviceEntity {
    @Id
    private Long id;
    
    @Column("device_id")
    private String deviceId;
    
    @Column("location")
    private String location;

    public DeviceEntity() {}

    public DeviceEntity(String deviceId, String location) {
        this.deviceId = deviceId;
        this.location = location;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}