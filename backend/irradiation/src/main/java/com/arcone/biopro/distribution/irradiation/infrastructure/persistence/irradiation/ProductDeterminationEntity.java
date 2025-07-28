package com.arcone.biopro.distribution.irradiation.infrastructure.persistence.irradiation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("lk_product_determination")
class ProductDeterminationEntity implements Serializable, Persistable<Integer> {

    @Id
    private Integer id;

    @Column("source_product_code")
    private String sourceProductCode;

    @Column("target_product_code")
    private String targetProductCode;

    @Column("target_product_description")
    private String targetProductDescription;

    private Boolean active;

    @Column("create_date")
    private Instant createDate;

    @Column("modification_date")
    private Instant modificationDate;

    @Override
    public boolean isNew() {
        return id == null;
    }
}