package com.arcone.biopro.distribution.shippingservice.domain.model;

import com.arcone.biopro.distribution.shippingservice.domain.model.enumeration.BloodType;
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
@Table(name = "bld_order_item")
public class OrderItem implements Serializable , Persistable<Long> {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @Column("order_id")
    private Long orderId;

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
    @CreatedDate
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @Override
    public boolean isNew() {
        return createDate == null;
    }
}
