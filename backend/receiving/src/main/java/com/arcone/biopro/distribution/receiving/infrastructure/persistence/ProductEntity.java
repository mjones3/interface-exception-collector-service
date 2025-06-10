package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Table(name = "lk_product")
@Getter
@Setter
@ToString
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductEntity {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @Column("product_code")
    private String productCode;

    @Column("short_description")
    private String shortDescription;

    @Column("product_family")
    private String productFamily;

    @Column("active")
    private boolean active;

    @Column("create_date")
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;

}
