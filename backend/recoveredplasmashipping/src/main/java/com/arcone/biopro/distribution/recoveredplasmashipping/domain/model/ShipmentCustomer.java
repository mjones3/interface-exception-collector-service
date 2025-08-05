package com.arcone.biopro.distribution.recoveredplasmashipping.domain.model;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CustomerOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CustomerService;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.controller.error.DataNotFoundException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Getter
@EqualsAndHashCode
@ToString
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class ShipmentCustomer implements Validatable {

    private String customerCode;
    private String customerName;
    private String customerState;
    private String customerPostalCode;
    private String customerCountry;
    private String customerCountryCode;
    private String customerCity;
    private String customerDistrict;
    private String customerAddressLine1;
    private String customerAddressLine2;
    private String customerAddressContactName;
    private String customerAddressPhoneNumber;
    private String customerAddressDepartmentName;
    private CustomerService customerService;


    public static ShipmentCustomer fromCustomerCode(String customerCode, CustomerService customerService) {
        var customer = checkCustomer(customerCode, customerService);
        return ShipmentCustomer.builder()
            .customerCode(customerCode)
            .customerName(customer.name())
            .customerState(customer.state())
            .customerPostalCode(customer.postalCode())
            .customerCountry(customer.country())
            .customerCountryCode(customer.countryCode())
            .customerCity(customer.city())
            .customerDistrict(customer.district())
            .customerAddressLine1(customer.addressLine1())
            .customerAddressLine2(customer.addressLine2())
            .build();
    }

    public static ShipmentCustomer fromShipmentDetails(String customerCode, String customerName, String customerState, String customerPostalCode, String customerCountry, String customerCountryCode, String customerCity, String customerDistrict, String customerAddressLine1, String customerAddressLine2, String customerAddressContactName, String customerAddressPhoneNumber, String customerAddressDepartmentName) {

        var shipCustomer = ShipmentCustomer.builder()
            .customerCode(customerCode)
            .customerName(customerName)
            .customerState(customerState)
            .customerPostalCode(customerPostalCode)
            .customerCountry(customerCountry)
            .customerCountryCode(customerCountryCode)
            .customerCity(customerCity)
            .customerDistrict(customerDistrict)
            .customerAddressLine1(customerAddressLine1)
            .customerAddressLine2(customerAddressLine2)
            .customerAddressContactName(customerAddressContactName)
            .customerAddressPhoneNumber(customerAddressPhoneNumber)
            .customerAddressDepartmentName(customerAddressDepartmentName)
            .build();
        shipCustomer.checkValid();

        return shipCustomer;
    }

    @Override
    public void checkValid() {

        if (this.customerCode == null || this.customerCode.isBlank()) {
            throw new IllegalArgumentException("Customer code cannot be null or blank");
        }
    }

    private static CustomerOutput checkCustomer(String customerCode, CustomerService customerService) {
        if (customerService == null) {
            throw new IllegalArgumentException("CustomerService is required");
        }

        return customerService.findByCode(customerCode)
            .switchIfEmpty(Mono.error(()-> new IllegalArgumentException("Customer not found for code: " + customerCode)))
            .block();

    }

}
