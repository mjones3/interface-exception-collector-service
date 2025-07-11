package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("bld_batch")
public class BatchEntity {
    @Id
    private Long id;
    
    @Column("device_id")
    private String deviceId;
    
    @Column("start_time")
    private LocalDateTime startTime;
    
    @Column("end_time")
    private LocalDateTime endTime;
    
    @Column("create_date")
    private LocalDateTime createDate;
    
    @Column("modification_date")
    private LocalDateTime modificationDate;

    public BatchEntity() {}

    public BatchEntity(String deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        this.deviceId = deviceId;
        this.startTime = startTime;
        this.endTime = endTime;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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