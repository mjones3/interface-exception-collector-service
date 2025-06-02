package com.arcone.biopro.distribution.receiving.adapter.in.web.mapper;

import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.ShippingInformationDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.receiving.application.dto.ShippingInformationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring" , uses = {UseCaseNotificationDtoMapper.class})
public interface UseCaseResponseMapper {

    UseCaseResponseDTO<ShippingInformationDTO> toUseCaseResponse(UseCaseOutput<ShippingInformationOutput> useCaseOutput);
}
