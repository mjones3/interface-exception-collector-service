package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;


import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@Builder(toBuilder = true)
@Table("bld_recovered_plasma_shipment")
public class RecoveredPlasmaShipmentEntity {

    @Id
    @InsertOnlyProperty
    private Long id;

    @Column("customer_code")
    private String customerCode;

    @Column("customer_name")
    private String customerName;

    @Column("location_code")
    private String locationCode;

    @Column("product_type")
    private String productType;

    @Column("shipment_number")
    private String shipmentNumber;

    @Column("status")
    private String status;

    @Column("create_employee_id")
    private String createEmployeeId;

    @Column("close_employee_id")
    private String closeEmployeeId;

    @Column("close_date")
    private ZonedDateTime closeDate;

    @Column("transportation_reference_number")
    private String transportationReferenceNumber;

    @Column("schedule_date")
    private LocalDate scheduleDate;

    @Column("shipment_date")
    private ZonedDateTime shipmentDate;

    @Column("carton_tare_weight")
    private BigDecimal cartonTareWeight;

    @Column("unsuitable_unit_report_document_status")
    private String unsuitableUnitReportDocumentStatus;

    @Column("customer_state")
    private String customerState;

    @Column("customer_postal_code")
    private String customerPostalCode;

    @Column("customer_country")
    private String customerCountry;

    @Column("customer_country_code")
    private String customerCountryCode;

    @Column("customer_city")
    private String customerCity;

    @Column("customer_district")
    private String customerDistrict;

    @Column("customer_address_line1")
    private String customerAddressLine1;

    @Column("customer_address_line2")
    private String customerAddressLine2;

    @Column("customer_address_contact_name")
    private String customerAddressContactName;

    @Column("customer_address_phone_number")
    private String customerAddressPhoneNumber;

    @Column("customer_address_department_name")
    private String customerAddressDepartmentName;

    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;

    @Column("delete_date")
    private ZonedDateTime deleteDate;
}
