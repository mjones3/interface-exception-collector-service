package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder(toBuilder = true)
@Table( "bld_import_item_consequence")
public class ImportItemConsequenceEntity implements Persistable<Long> {

    @Id
    @InsertOnlyProperty
    @Column("id")
    private Long id;

    @Column("import_item_id")
    private Long importItemId;

    @Column("item_consequence_type")
    private String consequenceType;

    @Column("item_consequence_reason")
    private String consequenceReason;

    @Override
    public boolean isNew() {
        return this.id == null;
    }
}
