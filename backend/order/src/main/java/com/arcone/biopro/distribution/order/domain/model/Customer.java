package com.arcone.biopro.distribution.order.domain.model;

import com.arcone.biopro.distribution.order.domain.model.vo.CustomerAddress;
import com.arcone.biopro.distribution.order.domain.model.vo.CustomerCode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class Customer implements Validatable {

    public static final String ADDRESS_TYPE_SHIPPING = "SHIPPING";
    public static final String ADDRESS_TYPE_BILLING = "BILLING";

    private final CustomerCode code;
    private final String externalId;
    private final String name;
    private final String departmentCode;
    private final String departmentName;
    private final String phoneNumber;
    private final List<CustomerAddress> addresses;
    private final boolean active;

    public Customer(
        CustomerCode code,
        String externalId,
        String name,
        String departmentCode,
        String departmentName,
        String phoneNumber,
        List<CustomerAddress> addresses,
        boolean active
    ) {
        this.code = code;
        this.externalId = externalId;
        this.name = name;
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.phoneNumber = phoneNumber;
        this.addresses = addresses;
        this.active = active;
        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.code == null) {
            throw new IllegalArgumentException("code cannot be null or blank");
        }
        if (this.externalId == null || this.externalId.isBlank()) {
            throw new IllegalArgumentException("externalId cannot be null or blank");
        }
        if (this.name == null || this.name.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or blank");
        }
        if (this.departmentCode == null || this.departmentCode.isBlank()) {
            throw new IllegalArgumentException("departmentCode cannot be null or blank");
        }
        if (this.departmentName == null || this.departmentName.isBlank()) {
            throw new IllegalArgumentException("departmentName cannot be null or blank");
        }
        if (this.phoneNumber == null || this.phoneNumber.isBlank()) {
            throw new IllegalArgumentException("phoneNumber cannot be null or blank");
        }
        if (this.addresses == null) {
            throw new IllegalArgumentException("addresses cannot be null");
        }
        this.checkHasAddressForType(ADDRESS_TYPE_SHIPPING);
        this.checkHasAddressForType(ADDRESS_TYPE_BILLING);
    }

    private void checkHasAddressForType(String type) {
        var hasAddressForType = this.addresses.stream()
            .anyMatch(address -> address.getAddressType().getValue().equals(type));

        if (!hasAddressForType) {
            throw new IllegalArgumentException("customer must have a " + type + " address");
        }
    }

}
