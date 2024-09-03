package com.arcone.biopro.distribution.inventory.infrastructure.persistence;


import com.arcone.biopro.distribution.inventory.domain.model.enumeration.QuarantineReason;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode(exclude = {"createDate"})
@Table("bld_product_quarantine")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductQuarantinedEntity implements Serializable, Persistable<UUID> {

    @Id
    @Column("id")
    UUID id;

    @Column("product_id")
    @NotNull
    UUID productId;

    @Column("reason")
    @NotNull
    QuarantineReason reason;

    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    ZonedDateTime createDate;

    @JsonIgnore
    @Override
    public boolean isNew() {
        return true;
    }

}

