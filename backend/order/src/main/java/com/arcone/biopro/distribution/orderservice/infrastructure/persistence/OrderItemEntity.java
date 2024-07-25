package com.arcone.biopro.distribution.orderservice.infrastructure.persistence;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@With
@Builder(toBuilder = true)
@Table(name = "bld_order_item")
public class OrderItemEntity {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @NotNull
    @Column("order_id")
    private Long orderId;

    @NotNull
    @Column("product_family")
    private String productFamily;

    @NotNull
    @Column("blood_type")
    private String bloodType;

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

    @NotNull
    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;

}
