package com.arcone.biopro.distribution.orderservice.application.mapper;

import com.arcone.biopro.distribution.orderservice.domain.model.Customer;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.CustomerAddress;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.CustomerAddressType;
import com.arcone.biopro.distribution.orderservice.domain.model.vo.CustomerCode;
import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerAddressDTO;
import com.arcone.biopro.distribution.orderservice.infrastructure.service.dto.CustomerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerMapper {

    public CustomerDTO mapToDTO(final Customer customer) {
        return CustomerDTO.builder()
            .code(customer.getCode().getValue())
            .externalId(customer.getExternalId())
            .name(customer.getName())
            .departmentCode(customer.getDepartmentCode())
            .departmentName(customer.getDepartmentName())
            .phoneNumber(customer.getPhoneNumber())
            .addresses(
                customer.getAddresses().stream()
                    .map(this::mapToDTO)
                    .toList()
            )
            .active(customer.isActive() ? "Y" : "N")
            .build();
    }

    public CustomerAddressDTO mapToDTO(CustomerAddress customerAddress) {
        return CustomerAddressDTO.builder()
            .contactName(customerAddress.getContactName())
            .addressType(customerAddress.getAddressType().getValue())
            .state(customerAddress.getState())
            .postalCode(customerAddress.getPostalCode())
            .countryCode(customerAddress.getCountryCode())
            .city(customerAddress.getCity())
            .district(customerAddress.getDistrict())
            .addressLine1(customerAddress.getAddressLine1())
            .addressLine2(customerAddress.getAddressLine2())
            .active(customerAddress.isActive() ? "Y" : "N")
            .build();
    }

    public Customer mapToDomain(final CustomerDTO customerDTO) {
        return new Customer(
            new CustomerCode(customerDTO.code()),
            customerDTO.externalId(),
            customerDTO.name(),
            customerDTO.departmentCode(),
            customerDTO.departmentName(),
            customerDTO.phoneNumber(),
            customerDTO.addresses().stream()
                .map(this::mapToDomain)
                .toList(),
            "Y".equals(customerDTO.active())
        );
    }

    public CustomerAddress mapToDomain(CustomerAddressDTO customerAddressDTO) {
        return new CustomerAddress(
            customerAddressDTO.contactName(),
            new CustomerAddressType(customerAddressDTO.addressType()),
            customerAddressDTO.state(),
            customerAddressDTO.postalCode(),
            customerAddressDTO.countryCode(),
            customerAddressDTO.city(),
            customerAddressDTO.district(),
            customerAddressDTO.addressLine1(),
            customerAddressDTO.addressLine2(),
            "Y".equals(customerAddressDTO.active())
        );
    }

}
