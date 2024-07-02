package com.arcone.biopro.distribution.shippingservice.domain.model;

import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.VisualInspection;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
@Builder
@Table(name = "bld_shipment_item_packed")
public class ShipmentItemPacked implements Serializable, Persistable<Long> {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @NotNull
    @Column("shipment_item_id")
    private Long shipmentItemId;

    @NotNull
    @Column("unit_number")
    private String unitNumber;

    @NotNull
    @Column("product_code")
    private String productCode;

    @NotNull
    @Column("product_description")
    private String productDescription;

    @Column("packed_by_employee_id")
    private String packedByEmployeeId;

    @NotNull
    @Column("abo_rh")
    private String aboRh;

    @NotNull
    @Column("expiration_date")
    private ZonedDateTime expirationDate;

    @Column("collection_date")
    private ZonedDateTime collectionDate;

    @NotNull
    @Column("create_date")
    @InsertOnlyProperty
    @CreatedDate
    private ZonedDateTime createDate;

    @NotNull
    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;
    @NotNull
    @Column("visual_inspection")
    private VisualInspection visualInspection;

    @Override
    public boolean isNew() {
        return createDate == null;
    }

}
