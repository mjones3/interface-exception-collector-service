package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder(toBuilder = true)
@Table(name = "lk_system_process_property")
public class SystemProcessPropertyEntity {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @Column("system_process_type")
    private String systemProcessType;

    @Column("property_key")
    @NotNull
    private String propertyKey;

    @Column("property_value")
    @NotNull
    private String propertyValue;
}
