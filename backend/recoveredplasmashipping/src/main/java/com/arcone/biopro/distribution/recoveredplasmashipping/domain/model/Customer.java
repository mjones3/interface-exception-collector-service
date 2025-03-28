package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
public class Customer implements Validatable {
    private Long id;
    private String externalId;
    private String customerType;
    private String name;
    private String code;
    private String departmentCode;
    private String departmentName;
    private String foreignFlag;
    private String phoneNumber;
    private String contactName;
    private String state;
    private String postalCode;
    private String country;
    private String countryCode;
    private String city;
    private String district;
    private String addressLine1;
    private String addressLine2;
    private Boolean active;
    private ZonedDateTime createDate;
    private ZonedDateTime modificationDate;

    public Customer(Long id, String externalId, String customerType, String name, String code, String departmentCode, String departmentName, String foreignFlag, String phoneNumber
        , String contactName, String state, String postalCode, String country, String countryCode, String city, String district, String addressLine1, String addressLine2
        , Boolean active, ZonedDateTime createDate, ZonedDateTime modificationDate) {
        this.id = id;
        this.externalId = externalId;
        this.customerType = customerType;
        this.name = name;
        this.code = code;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.foreignFlag = foreignFlag;
        this.phoneNumber = phoneNumber;
        this.contactName = contactName;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.countryCode = countryCode;
        this.city = city;
        this.district = district;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.active = active;
        this.createDate = createDate;
        this.modificationDate = modificationDate;

        checkValid();
    }

    @Override
    public void checkValid() {

        if (this.code == null || this.code.isBlank()) {
            throw new IllegalArgumentException("Code cannot be null or blank");
        }
        if (this.name == null || this.name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (this.addressLine1 == null || this.addressLine1.isBlank()) {
            throw new IllegalArgumentException("Address Line1 cannot be null or blank");
        }
        if (this.postalCode == null || this.postalCode.isBlank()) {
            throw new IllegalArgumentException("Postal Code cannot be null or blank");
        }
        if (this.city == null || this.city.isBlank()) {
            throw new IllegalArgumentException("City cannot be null or blank");
        }
        if (this.state == null || this.state.isBlank()) {
            throw new IllegalArgumentException("State cannot be null or blank");
        }
        if (this.country == null || this.country.isBlank()) {
            throw new IllegalArgumentException("Country cannot be null or blank");
        }
        if (this.countryCode == null || this.countryCode.isBlank()) {
            throw new IllegalArgumentException("Country Code cannot be null or blank");
        }


    }
}
