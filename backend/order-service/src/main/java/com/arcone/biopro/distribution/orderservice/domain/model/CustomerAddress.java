package com.arcone.biopro.distribution.orderservice.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class CustomerAddress implements Validatable {

    private final String contactName;
    private final CustomerAddressType addressType;
    private final String state;
    private final String postalCode;
    private final String countryCode;
    private final String city;
    private final String district;
    private final String addressLine1;
    private final String addressLine2;
    private final boolean active;

    public CustomerAddress(
        String contactName,
        CustomerAddressType addressType,
        String state,
        String postalCode,
        String countryCode,
        String city,
        String district,
        String addressLine1,
        String addressLine2,
        boolean active
    ) {
        this.contactName = contactName;
        this.addressType = addressType;
        this.state = state;
        this.postalCode = postalCode;
        this.countryCode = countryCode;
        this.city = city;
        this.district = district;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.active = active;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.contactName == null || this.contactName.isBlank()) {
            throw new IllegalArgumentException("contactName cannot be null or blank");
        }
        if (this.addressType == null) {
            throw new IllegalArgumentException("addressType cannot be null");
        }
        if (this.state == null || this.state.isBlank()) {
            throw new IllegalArgumentException("state cannot be null or blank");
        }
        if (this.postalCode == null || this.postalCode.isBlank()) {
            throw new IllegalArgumentException("postalCode cannot be null or blank");
        }
        if (this.countryCode == null || this.countryCode.isBlank()) {
            throw new IllegalArgumentException("countryCode cannot be null or blank");
        }
        if (this.city == null || this.city.isBlank()) {
            throw new IllegalArgumentException("city cannot be null or blank");
        }
        if (this.district == null || this.district.isBlank()) {
            throw new IllegalArgumentException("district cannot be null or blank");
        }
        if (this.addressLine1 == null || this.addressLine1.isBlank()) {
            throw new IllegalArgumentException("addressLine1 cannot be null or blank");
        }
    }

}
