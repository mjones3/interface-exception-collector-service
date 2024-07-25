package com.arcone.biopro.distribution.shipping.domain.model;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.BloodType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 255)
    @Column("product_family")
    private String productFamily;

    @NotNull
    @Column("blood_type")
    private BloodType bloodType;

    @NotNull
    @Column("quantity")
    private Integer quantity;

    @Column("comments")
    private String comments;

    @NotNull
    @Column("create_date")
    @InsertOnlyProperty
    @CreatedDate
    private ZonedDateTime createDate;

    @NotNull
    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;

    @Override
    public boolean isNew() {
        return createDate == null;
    }

}
