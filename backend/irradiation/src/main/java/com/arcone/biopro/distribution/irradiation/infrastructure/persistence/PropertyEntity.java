package com.arcone.biopro.distribution.irradiation.infrastructure.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode(exclude = {"createDate", "modificationDate"})
@Table("bld_inventory_property")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PropertyEntity implements Serializable, Persistable<UUID> {

    @Id
    @Column("id")
    UUID id;

    @NotBlank
    @Column("key")
    String key;

    @NotBlank
    @Column("value")
    String value;

    @NotNull
    @Column("inventory_id")
    UUID inventoryId;

    @NotNull
    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    ZonedDateTime createDate;

    @NotNull
    @Column("modification_date")
    @LastModifiedDate
    ZonedDateTime modificationDate;

    @JsonIgnore
    @Override
    public boolean isNew() {
        return true;
    }
}
