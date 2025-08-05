package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CustomerDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CustomerOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerDtoMapper {
    CustomerDTO toCustomerDto(CustomerOutput customerOutput);
}
