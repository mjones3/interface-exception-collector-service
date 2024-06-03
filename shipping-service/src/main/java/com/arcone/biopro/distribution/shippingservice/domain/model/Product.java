package com.arcone.biopro.distribution.shippingservice.domain.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
@Builder
@Table(name = "bld_product")
public class Product implements Serializable, Persistable<Long> {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @NotEmpty
    @Size(max = 36)
    @Column("unit_number")
    private String unitNumber;

    @NotEmpty
    @Size(max = 50)
    @Column("product_code")
    private String productCode;

    @NotEmpty
    @Size(max = 255)
    @Column("product_description")
    private String productDescription;

    @NotEmpty
    @Size(max = 20)
    @Column("isbt_product_code")
    private String isbtProductCode;

    @NotNull
    @Column("expiration_date")
    private ZonedDateTime expirationDate;

    @NotNull
    @Column("quarantined")
    private Boolean quarantined;

    @NotNull
    @Column("discarded")
    private Boolean discarded;

    @NotNull
    @Column("labeled")
    private Boolean labeled;

    @NotEmpty
    @Size(max = 255)
    @Column("storage_location")
    private String storageLocation;

    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @Override
    public boolean isNew() {
        return createDate == null;
    }

}
