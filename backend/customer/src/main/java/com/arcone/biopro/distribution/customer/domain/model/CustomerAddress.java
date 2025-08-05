package com.arcone.biopro.distribution.customer.domain.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Data
@Table("customer.bld_customer_address")
public class CustomerAddress {
    @Id
    private Long id;

    @Column("customer_id")
    private Long customerId;

    @Column("contact_name")
    private String contactName;

    @Column("address_type")
    private String addressType;

    @Column("address_line1")
    private String addressLine1;

    @Column("address_line2")
    private String addressLine2;

    private String city;
    private String state;

    @Column("postal_code")
    private String postalCode;

    private String district;
    private String country;

    @Column("country_code")
    private String countryCode;

    private String active;

    @Column("create_date")
    private ZonedDateTime createDate;

    @Column("modification_date")
    private ZonedDateTime modificationDate;

    @Column("delete_date")
    private ZonedDateTime deleteDate;
}
