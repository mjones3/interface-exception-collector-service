package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
@Builder(toBuilder = true)
@Table(name = "bld_recovered_plasma_shipment_carton")
public class CartonEntity {

    @Id
    @InsertOnlyProperty
    private Long id;

    @Column("recovered_plasma_shipment_id")
    private Long shipmentId;

    @Column("carton_number")
    private String cartonNumber;

    @Column("carton_sequence_number")
    private Integer cartonSequenceNumber;

    @Column("status")
    private String status;

    @Column("total_weight")
    private BigDecimal totalWeight;

    @Column("total_volume")
    private BigDecimal totalVolume;

    @Column("calculated_total_weight")
    private BigDecimal calculatedTotalWeight;

    @Column("calculated_total_volume")
    private BigDecimal calculatedTotalVolume;

    @Column("create_employee_id")
    private String createEmployeeId;

    @Column("close_employee_id")
    private String closeEmployeeId;

    @Column("close_date")
    private ZonedDateTime closeDate;

    @Column("delete_date")
    private ZonedDateTime deleteDate;

    @NotNull
    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @NotNull
    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;

    @Column("repack_employee_id")
    private String repackEmployeeId;

    @Column("repack_comments")
    private String repackComments;

    @Column("repack_date")
    private ZonedDateTime repackDate;

}
