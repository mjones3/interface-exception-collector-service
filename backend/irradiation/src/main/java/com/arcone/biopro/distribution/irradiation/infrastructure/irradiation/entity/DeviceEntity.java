package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("bld_device")
public class DeviceEntity {
    @Id
    private Long id;
    
    @Column("device_id")
    private String deviceId;
    
    @Column("location")
    private String location;
    
    @Column("status")
    private String status;
    
    @Column("create_date")
    private LocalDateTime createDate;
    
    @Column("modification_date")
    private LocalDateTime modificationDate;

    public DeviceEntity() {}

    public DeviceEntity(String deviceId, String location, String status) {
        this.deviceId = deviceId;
        this.location = location;
        this.status = status;
        this.createDate = LocalDateTime.now();
        this.modificationDate = LocalDateTime.now();
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(LocalDateTime modificationDate) {
        this.modificationDate = modificationDate;
    }
}