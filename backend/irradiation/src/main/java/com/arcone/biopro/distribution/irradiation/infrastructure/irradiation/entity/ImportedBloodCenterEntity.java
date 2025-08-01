package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
@Table("bld_imported_blood_center")
public class ImportedBloodCenterEntity implements Serializable, Persistable<Long> {

    @Id
    private Long id;

    @Column("product_id")
    private Long productId;

    @Column("name")
    private String name;

    @Column("address")
    private String address;

    @Column("registration_number")
    private String registrationNumber;

    @Column("license_number")
    private String licenseNumber;

    @CreatedDate
    @Column("create_date")
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;

    @Override
    public boolean isNew() {
        return id == null;
    }
}
