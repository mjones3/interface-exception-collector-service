package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

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
@Table("lk_temperature_category_product_code")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TemperatureCategoryEntity {

    @Id
    @Column("product_code")
    String productCode;

    @Column("temperature_category")
    String temperatureCategory;
}
