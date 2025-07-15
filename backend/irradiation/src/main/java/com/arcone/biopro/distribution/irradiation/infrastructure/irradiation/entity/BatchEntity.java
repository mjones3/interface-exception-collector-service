package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Table("bld_batch")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchEntity implements Serializable, Persistable<Long> {
    @Id
    private Long id;

    @Column("device_id")
    private String deviceId;

    @Column("start_time")
    private LocalDateTime startTime;

    @Column("end_time")
    private LocalDateTime endTime;

    @Column("delete_date")
    private LocalDateTime deleteDate;

    @CreatedDate
    @Column("create_date")
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;

    public BatchEntity(String deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        this.deviceId = deviceId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createDate = ZonedDateTime.now();
        this.modificationDate = ZonedDateTime.now();
    }

    @Override
    public boolean isNew() {
        return id == null;
    }
}
