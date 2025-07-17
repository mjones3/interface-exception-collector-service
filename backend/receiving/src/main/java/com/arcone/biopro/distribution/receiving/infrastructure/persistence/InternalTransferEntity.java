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

import java.time.ZonedDateTime;

@Data
@Builder(toBuilder = true)
@Table("bld_internal_transfer")
public class InternalTransferEntity implements Persistable<Long> {

    @Id
    @InsertOnlyProperty
    @Column("id")
    private Long id;

    @Column("order_number")
    private Long orderNumber;

    @Column("external_order_id")
    private String externalOrderId;

    @Column("location_code_from")
    private String locationCodeFrom;

    @Column("location_code_to")
    private String locationCodeTo;

    @Column( "temperature_category")
    private String temperatureCategory;

    @Column("label_status")
    private String labelStatus;

    @Column("quarantined_products")
    private Boolean quarantinedProducts;

    @Column("employee_id")
    private String employeeId;

    @CreatedDate
    @InsertOnlyProperty
    @Column("create_date")
    private ZonedDateTime createDate;

    @LastModifiedDate
    @Column("modification_date")
    private ZonedDateTime modificationDate;

    @Column("delete_date")
    private ZonedDateTime deleteDate;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return this.id == null;
    }
}
