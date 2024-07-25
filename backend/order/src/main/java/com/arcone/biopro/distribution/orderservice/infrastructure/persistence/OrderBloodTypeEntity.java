package com.arcone.biopro.distribution.orderservice.infrastructure.persistence;

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

import java.time.ZonedDateTime;

@Data
@Builder(toBuilder = true)
@Table(name = "lk_order_blood_type")
public class OrderBloodTypeEntity implements Persistable<Long> {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @NotNull
    @Column("product_family")
    private String productFamily;

    @NotNull
    @Column("blood_type")
    private String bloodType;

    @NotNull
    @Column("description_key")
    private String descriptionKey;

    @NotNull
    @Column("order_number")
    private int orderNumber;

    @NotNull
    @Column("active")
    private boolean active;

    @NotNull
    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @NotNull
    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;

    @Override
    public boolean isNew() {
        return this.id == null;
    }

}
