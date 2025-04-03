package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Immutable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@Table("vw_recovered_plasma_shipment_report")
@Immutable
public class RecoveredPlasmaShipmentReportEntity {

    @Id
    private Long id;

    @Column("shipment_number")
    private String shipmentNumber;

    @Column("status")
    private String status;

    @Column("customer_code")
    private String customerCode;

    @Column("customer_name")
    private String customerName;

    @Column("location_code")
    private String locationCode;

    @Column("location")
    private String location;

    @Column("product_type")
    private String productType;

    @Column("product_type_description")
    private String productTypeDescription;

    @Column("shipment_date")
    private LocalDate shipmentDate;

}
