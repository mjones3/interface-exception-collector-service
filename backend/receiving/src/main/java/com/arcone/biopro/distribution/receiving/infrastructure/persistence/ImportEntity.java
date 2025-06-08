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
@Table( "bld_import")
public class ImportEntity implements Persistable<Long> {

        @Id
        @InsertOnlyProperty
        @Column( "id")
        private Long id;

        @Column( "temperature_category")
        private String temperatureCategory;

        @Column( "transit_start_date_time")
        private LocalDateTime transitStartDateTime;

        @Column( "transit_start_time_zone")
        private String transitStartTimeZone;

        @Column( "transit_end_date_time")
        private LocalDateTime transitEndDateTime;

        @Column( "transit_end_time_zone")
        private String transitEndTimeZone;

        @Column( "total_transit_time")
        private String totalTransitTime;

        @Column( "transit_time_result")
        private String transitTimeResult;

        @Column( "temperature")
        private BigDecimal temperature;

        @Column( "thermometer_code")
        private String thermometerCode;

        @Column( "temperature_result")
        private String temperatureResult;

        @Column( "location_code")
        private String locationCode;

        @Column( "comments")
        private String comments;

        @Column( "status")
        private String status;

        @Column( "employee_id")
        private String employeeId;

        @Column( "delete_date")
        private ZonedDateTime deleteDate;

        @CreatedDate
        @InsertOnlyProperty
        @Column( "create_date")
        private ZonedDateTime createDate;

        @LastModifiedDate
        @Column( "modification_date")
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
