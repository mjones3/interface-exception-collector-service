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
@Table("bld_transfer_receipt_item_consequence")
public class TransferReceiptItemConsequenceEntity implements Persistable<Long> {

    @Id
    @InsertOnlyProperty
    @Column("id")
    private Long id;

    @Column("internal_transfer_item_id")
    private Long internalTransferItemId;

    @Column("item_consequence_type")
    private String itemConsequenceType;

    @Column("item_consequence_reason_key")
    private String itemConsequenceReasonKey;

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
