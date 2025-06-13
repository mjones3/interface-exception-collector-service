package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@Builder(toBuilder = true)
@Table(name = "bld_recovered_plasma_shipment_history")
public class ShipmentHistoryEntity {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @Column("shipment_id")
    @NotNull
    private Long shipmentId;

    @Column("comments")
    @NotNull
    private String comments;

    @NotNull
    @Column("create_employee_id")
    private String createEmployeeId;

    @NotNull
    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    private ZonedDateTime createDate;

}
