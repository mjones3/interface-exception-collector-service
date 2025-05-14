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
@Table(name = "bld_unacceptable_units_report")
public class UnacceptableUnitReportEntity {

    @Id
    @InsertOnlyProperty
    private Long id;

    @Column("shipment_id")
    private Long shipmentId;

    @Column("carton_number")
    private String cartonNumber;

    @Column("carton_sequence_number")
    private Integer cartonSequenceNumber;

    @Column("unit_number")
    private String unitNumber;

    @Column("product_code")
    private String productCode;

    @Column("failure_reason")
    private String failureReason;

    @NotNull
    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    private ZonedDateTime createDate;

}
