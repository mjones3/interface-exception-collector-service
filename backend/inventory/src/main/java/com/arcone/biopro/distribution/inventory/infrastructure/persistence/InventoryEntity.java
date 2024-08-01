package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.Location;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table("bld_inventory")
public class InventoryEntity implements Serializable, Persistable<Long> {

    @Id
    @Column("id")
    private Long id;

    @NotBlank
    @Column("unit_number")
    private UnitNumber unitNumber;

    @NotBlank
    @Column("product_code")
    private ProductCode productCode;

    @NotNull
    @Column("status")
    private InventoryStatus inventoryStatus;

    @NotNull
    @Column("expiration_date")
    private ZonedDateTime expirationDate;

    @NotNull
    @Column("location")
    private Location location;

    @NotNull
    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @NotNull
    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;

    @JsonIgnore
    @Override
    public boolean isNew() {
        return createDate == null;
    }
}
