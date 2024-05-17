package com.arcone.biopro.distribution.shippingservice.domain.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
@Builder
@Table(name = "bld_shipment_item")
public class ShipmentItem implements Serializable, Persistable<Long> {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @NotNull
    @Column("shipment_id")
    private Long shipmentId;

    @NotNull
    @Column("inventory_id")
    private Long inventoryId;

    @NotNull
    @Column("create_date")
    private ZonedDateTime createDate;

    @NotNull
    @Column("modification_date")
    private ZonedDateTime modificationDate;

    @Override
    public boolean isNew() {
        return createDate == null;
    }

}
