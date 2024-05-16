package com.arcone.biopro.distribution.shippingservice.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
@Builder
@Table("bld_shipping_service")
public class ShippingService implements Serializable, Persistable<Long> {

    @Id
    @Column("id")
    private Long id;

    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;

    @JsonIgnore
    @Override
    public boolean isNew() {
        return createDate == null;
    }
}
