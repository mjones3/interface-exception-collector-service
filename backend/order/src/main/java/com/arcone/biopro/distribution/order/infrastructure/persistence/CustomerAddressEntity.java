package com.arcone.biopro.distribution.order.infrastructure.persistence;

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
@Table(name = "lk_customer_address")
public class CustomerAddressEntity {

    @Id
    @InsertOnlyProperty
    private Long id;

    @Column("customer_id")
    @NotNull
    private Long customerId;

    @NotNull
    @Column("address_type")
    private String addressType;

    @Column("contact_name")
    private String contactName;

    @Column("state")
    private String state;

    @Column("postal_code")
    private String postalCode;

    @Column("country")
    private String country;

    @Column("country_code")
    private String countryCode;

    @Column("city")
    private String city;

    @Column("district")
    private String district;

    @Column("address_line1")
    private String addressLine1;

    @Column("address_line2")
    private String addressLine2;

    @NotNull
    @Column("active")
    private Boolean active;

    @Column("delete_date")
    private ZonedDateTime deleteDate;

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
