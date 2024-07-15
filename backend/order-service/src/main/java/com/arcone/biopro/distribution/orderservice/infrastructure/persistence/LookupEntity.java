package com.arcone.biopro.distribution.orderservice.infrastructure.persistence;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder(toBuilder = true)
@Table(name = "lk_lookup")
public class LookupEntity implements Persistable<Long> {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @NotNull
    @Column("type")
    private String type;

    @NotNull
    @Column("option_value")
    private String optionValue;

    @NotNull
    @Column("description_key")
    private String descriptionKey;

    @NotNull
    @Column("order_number")
    private int orderNumber;

    @NotNull
    @Column("active")
    private boolean active;

    @Override
    public boolean isNew() {
        return this.id == null;
    }

}
