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
@Table("bld_internal_transfer_item")
public class InternalTransferItemEntity implements Persistable<Long> {

    @Id
    @InsertOnlyProperty
    @Column("id")
    private Long id;

    @Column("internal_transfer_id")
    private Long internalTransferId;

    @Column("unit_number")
    private String unitNumber;

    @Column("product_code")
    private String productCode;

    @Column("product_description")
    private String productDescription;

    @CreatedDate
    @InsertOnlyProperty
    @Column("create_date")
    private ZonedDateTime createDate;

    @LastModifiedDate
    @Column("modification_date")
    private ZonedDateTime modificationDate;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return this.id == null;
    }
}
