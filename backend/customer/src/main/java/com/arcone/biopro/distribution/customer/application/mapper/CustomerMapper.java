package com.arcone.biopro.distribution.customer.application.mapper;

import com.arcone.biopro.distribution.customer.application.dto.CustomerDto;
import com.arcone.biopro.distribution.customer.application.dto.CustomerAddressDto;
import com.arcone.biopro.distribution.customer.domain.model.Customer;
import com.arcone.biopro.distribution.customer.domain.model.CustomerAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "type", target = "customerType")
    @Mapping(source = "status", target = "active")
    Customer toEntity(CustomerDto dto);

    CustomerDto toDto(Customer entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "countryCode", ignore = true)
    @Mapping(target = "active", constant = "Y")
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "deleteDate", ignore = true)
    CustomerAddress toEntity(CustomerAddressDto dto);

    CustomerAddressDto toDto(CustomerAddress entity);
}
