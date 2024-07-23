package com.arcone.biopro.distribution.orderservice.verification.controllers;

import com.arcone.biopro.distribution.orderservice.verification.support.types.CustomerAddressType;
import com.arcone.biopro.distribution.orderservice.verification.support.types.CustomerType;

import java.util.List;
import java.util.Map;

public class CustomerController {

    public static CustomerType parseCustomerByCodeResponse(Map response){
        return CustomerType.builder()
            .externalId((String) response.get("externalId"))
            .name((String) response.get("name"))
            .code((String) response.get("code"))
            .departmentCode((String) response.get("departmentCode"))
            .departmentName((String) response.get("departmentName"))
            .phoneNumber((String) response.get("phoneNumber"))
            .active((String) response.get("active"))
            .addresses(parseCustomerAddresses((List<Map<String, ?>>) response.get("addresses")))
            .build();
    }

    private static List<CustomerAddressType> parseCustomerAddresses(List<Map<String, ?>> addressesMap) {
        return addressesMap.stream()
            .map(v -> CustomerAddressType.builder()
                .contactName((String) v.get("contactName"))
                .addressType((String) v.get("addressType"))
                .state((String) v.get("state"))
                .postalCode((String) v.get("postalCode"))
                .countryCode((String) v.get("countryCode"))
                .city((String) v.get("city"))
                .district((String) v.get("district"))
                .addressLine1((String) v.get("addressLine1"))
                .addressLine2((String) v.get("addressLine2"))
                .active((String) v.get("active"))
                .build()
            )
            .toList();
    }

}
