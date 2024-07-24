package com.arcone.biopro.distribution.orderservice.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

import static com.arcone.biopro.distribution.orderservice.domain.model.Customer.ADDRESS_TYPE_BILLING;
import static com.arcone.biopro.distribution.orderservice.domain.model.Customer.ADDRESS_TYPE_SHIPPING;

@Getter
@EqualsAndHashCode
@ToString
public class CustomerAddressType implements Validatable {

    private static final Set<String> ALLOWED_ADDRESS_TYPES = Set.of(ADDRESS_TYPE_SHIPPING, ADDRESS_TYPE_BILLING);

    private final String value;

    public CustomerAddressType(String value) {
        this.value = value;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.value == null || this.value.isBlank()) {
            throw new IllegalArgumentException("value cannot be null or blank");
        }
        if (!ALLOWED_ADDRESS_TYPES.contains(this.value)) {
            throw new IllegalArgumentException("value for address type is invalid: allowed types are " + ALLOWED_ADDRESS_TYPES);
        }
    }

}
