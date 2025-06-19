package com.arcone.biopro.distribution.shipping.domain.model;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.IneligibleStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
@Builder
@Table(name = "bld_shipment_item_removed")
public class ShipmentItemRemoved implements Serializable, Persistable<Long> {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @NotNull
    @Column("shipment_id")
    private Long shipmentId;

    @NotNull
    @Column("unit_number")
    private String unitNumber;

    @NotNull
    @Column("product_code")
    private String productCode;

    @Size(max = 255)
    @Column("product_family")
    private String productFamily;

    @Column("ineligible_status")
    private IneligibleStatus ineligibleStatus;

    @NotNull
    @Column("removed_date")
    @InsertOnlyProperty
    @CreatedDate
    private ZonedDateTime removedDate;

    @NotNull
    @Column("removed_by_employee_id")
    private String removedByEmployeeId;

    @Override
    public boolean isNew() {
        return removedDate == null;
    }
}
