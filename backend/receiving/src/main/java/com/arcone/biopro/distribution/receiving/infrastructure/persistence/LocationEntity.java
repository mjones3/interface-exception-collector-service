package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.InsertOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@Builder(toBuilder = true)
@Table(name = "lk_location")
public class LocationEntity {

    @Id
    @Column("id")
    @InsertOnlyProperty
    private Long id;

    @Column("external_id")
    private String externalId;

    @Column("code")
    @NotNull
    private String code;

    @Column("name")
    @NotNull
    private String name;

    @Column("city")
    @NotNull
    private String city;

    @Column("state")
    @NotNull
    private String state;

    @Column("postal_code")
    @NotNull
    private String postalCode;

    @Column("address_line_1")
    @NotNull
    private String addressLine1;

    @Column("address_line_2")
    private String addressLine2;

    @NotNull
    @Column("active")
    private boolean active;

    @NotNull
    @Column("create_date")
    @CreatedDate
    @InsertOnlyProperty
    private ZonedDateTime createDate;

    @NotNull
    @Column("modification_date")
    @LastModifiedDate
    private ZonedDateTime modificationDate;

}
