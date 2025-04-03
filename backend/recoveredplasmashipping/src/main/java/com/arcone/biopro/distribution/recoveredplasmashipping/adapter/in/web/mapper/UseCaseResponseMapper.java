package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CustomerDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RecoveredPlasmaShipmentResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CustomerOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring" , uses = {UseCaseNotificationDtoMapper.class, CustomerDtoMapper.class , RecoveredPlasmaShipmentDtoMapper.class})
public interface UseCaseResponseMapper {
    UseCaseResponseDTO<CustomerDTO> toUseCaseResponseDTO(UseCaseOutput<CustomerOutput> useCaseOutput);
    UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO> toUseCaseRecoveredPlasmaShipmentResponseDTO(UseCaseOutput<RecoveredPlasmaShipmentOutput> useCaseOutput);


}
