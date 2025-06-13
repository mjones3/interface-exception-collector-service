package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import com.arcone.biopro.distribution.receiving.application.dto.EnterShippingInformationCommandInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommandRequestDTOMapper {

    EnterShippingInformationCommandInput toCommandInput(EnterShippingInformationRequestDTO enterShippingInformationRequestDTO);
}
