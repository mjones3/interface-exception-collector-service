package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder(toBuilder = true)
@Table(name = "lk_location_property")
public class LocationPropertyEntity {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @Column("location_id")
    @NotNull
    private Long locationId;

    @Column("property_key")
    @NotNull
    private String propertyKey;

    @Column("property_value")
    @NotNull
    private String propertyValue;
}
