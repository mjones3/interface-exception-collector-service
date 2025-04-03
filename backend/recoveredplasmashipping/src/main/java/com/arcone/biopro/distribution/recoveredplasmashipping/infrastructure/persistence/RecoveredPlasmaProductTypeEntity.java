package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@Builder
@Table("lk_recovered_plasma_product_type")
public class RecoveredPlasmaProductTypeEntity {

    @Id
    private Integer id;

    @Column("product_type")
    private String productType;

    @Column("product_type_description")
    private String productTypeDescription;

    @Column("order_number")
    private Integer orderNumber;

    private Boolean active;

    @Column("create_date")
    private ZonedDateTime createDate;

    @Column("modification_date")
    private ZonedDateTime modificationDate;
}
