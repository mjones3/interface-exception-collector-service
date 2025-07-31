package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("bld_batch_item")
public class BatchItemEntity implements Serializable, Persistable<Long> {
    @Id
    private Long id;

    @Column("batch_id")
    private Long batchId;

    @Column("unit_number")
    private String unitNumber;

    @Column("product_code")
    private String productCode;

    @Column("lot_number")
    private String lotNumber;

    @Column("new_product_code")
    private String newProductCode;

    @Column("expiration_date")
    private LocalDateTime expirationDate;

    @Column("product_family")
    private String productFamily;

    @CreatedDate
    @Column("create_date")
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;

    @Column("delete_date")
    private LocalDateTime deleteDate;

    @Column("is_timing_rule_validated")
    private Boolean isTimingRuleValidated;

    @Override
    public boolean isNew() {
        return id == null;
    }
}
