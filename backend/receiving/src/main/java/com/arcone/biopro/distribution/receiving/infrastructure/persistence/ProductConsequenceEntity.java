package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import lombok.Data;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "lk_product_consequence")
public class ProductConsequenceEntity implements Persistable<Long> {

    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @Column("product_category")
    private String productCategory;

    @Column("acceptable")
    private boolean acceptable;

    @Column("result_property")
    private String resultProperty;

    @Column("result_type")
    private String resultType;

    @Column("result_value")
    private String resultValue;

    @Column("consequence_type")
    private String consequenceType;

    @Column("consequence_reason")
    private String consequenceReason;

    @Column("order_number")
    private Integer orderNumber;

    @Column("active")
    private boolean active;

    @Override
    public boolean isNew() {
        return this.id == null;
    }
}



