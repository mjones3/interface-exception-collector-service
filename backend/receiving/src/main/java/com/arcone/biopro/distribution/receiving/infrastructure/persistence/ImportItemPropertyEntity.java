package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@Builder(toBuilder = true)
@Table( "bld_import_item_property")
public class ImportItemPropertyEntity implements Persistable<Long> {

    @Id
    @InsertOnlyProperty
    @Column("id")
    private Long id;

    @Column("import_item_id")
    private Long importItemId;

    @Column("property_key")
    private String propertyKey;

    @Column("property_value")
    private String propertyValue;

    @Override
    public boolean isNew() {
        return this.id == null;
    }
}
