package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

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
@Table(name = "lk_customer")
public class CustomerEntity {

    @Id
    @InsertOnlyProperty
    private Long id;

    @Column("external_id")
    @NotNull
    private String externalId;

    @NotNull
    @Column("customer_type")
    private String customerType;

    @NotNull
    @Column("name")
    private String name;

    @NotNull
    @Column("code")
    private String code;

    @Column("department_code")
    private String departmentCode;

    @Column("department_name")
    private String departmentName;

    @Column("foreign_flag")
    private String foreignFlag;

    @Column("phone_number")
    private String phoneNumber;

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
