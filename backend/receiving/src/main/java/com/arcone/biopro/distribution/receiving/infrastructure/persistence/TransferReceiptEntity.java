package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Builder(toBuilder = true)
@Table("bld_transfer_receipt")
public class TransferReceiptEntity implements Persistable<Long> {

    @Id
    @InsertOnlyProperty
    @Column("id")
    private Long id;

    @Column("order_number")
    private String orderNumber;

    @Column("external_order_id")
    private String externalOrderId;

    @Column("temperature_category")
    private String temperatureCategory;

    @Column("transit_start_date_time")
    private LocalDateTime transitStartDateTime;

    @Column("transit_end_date_time")
    private LocalDateTime transitEndDateTime;

    @Column("transit_time_zone")
    private String transitTimeZone;

    @Column("total_transit_time")
    private String totalTransitTime;

    @Column("transit_time_result")
    private String transitTimeResult;

    @Column( "temperature")
    private BigDecimal temperature;

    @Column("thermometer_code")
    private String thermometerCode;

    @Column("location_code")
    private String locationCode;

    @Column("received_different_location")
    private Boolean receivedDifferentLocation;

    @Column("status")
    private String status;

    @Column("comments")
    private String comments;

    @Column("employee_id")
    private String employeeId;

    @Column("delete_date")
    private ZonedDateTime deleteDate;

    @CreatedDate
    @InsertOnlyProperty
    @Column("create_date")
    private ZonedDateTime createDate;

    @LastModifiedDate
    @Column("modification_date")
    private ZonedDateTime modificationDate;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return this.id == null;
    }
}
