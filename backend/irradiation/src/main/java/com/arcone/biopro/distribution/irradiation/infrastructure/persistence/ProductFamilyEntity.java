package com.arcone.biopro.distribution.irradiation.infrastructure.persistence;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@Table("lk_product_family")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductFamilyEntity {

    @Id
    @Column("id")
    UUID id;

    @Column("product_family")
    String productFamily;

    @Column("time_frame")
    Integer timeFrame;
}
