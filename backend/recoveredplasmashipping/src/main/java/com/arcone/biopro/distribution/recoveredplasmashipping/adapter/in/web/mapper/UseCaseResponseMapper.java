package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CartonDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CartonPackingSlipDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CustomerDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.LabelDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.PageDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RecoveredPlasmaShipmentReportDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.RecoveredPlasmaShipmentResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.ShippingSummaryReportDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UnacceptableUnitReportDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonPackingSlipOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CustomerOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.LabelOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PageOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ShippingSummaryReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UnacceptableUnitReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring" , uses = {UseCaseNotificationDtoMapper.class, CustomerDtoMapper.class , RecoveredPlasmaShipmentDtoMapper.class , CartonDtoMapper.class})
public interface UseCaseResponseMapper {
    UseCaseResponseDTO<CustomerDTO> toUseCaseResponseDTO(UseCaseOutput<CustomerOutput> useCaseOutput);
    UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO> toUseCaseRecoveredPlasmaShipmentResponseDTO(UseCaseOutput<RecoveredPlasmaShipmentOutput> useCaseOutput);
    UseCaseResponseDTO<PageDTO<RecoveredPlasmaShipmentReportDTO>> toUseCaseRecoveredPlasmaShipmentReportDTO(UseCaseOutput<PageOutput<RecoveredPlasmaShipmentReportOutput>> useCaseOutput);
    UseCaseResponseDTO<CartonDTO> toUseCaseCreateCartonDTO(UseCaseOutput<CartonOutput> useCaseOutput);
    UseCaseResponseDTO<CartonDTO> toUseCasePackCartonItemDTO(UseCaseOutput<CartonOutput> useCaseOutput);
    UseCaseResponseDTO<CartonDTO> toUseCaseVerifyCartonDTO(UseCaseOutput<CartonOutput> useCaseOutput);
    UseCaseResponseDTO<CartonPackingSlipDTO> toUseCaseGenerateCartonPackingSlipDTO(UseCaseOutput<CartonPackingSlipOutput> useCaseOutput);
    UseCaseResponseDTO<UnacceptableUnitReportDTO> toUseCaseUnacceptableUnitReportDTO(UseCaseOutput<UnacceptableUnitReportOutput> useCaseOutput);
    UseCaseResponseDTO<ShippingSummaryReportDTO> toUseCaseShippingSummaryReportDTO(UseCaseOutput<ShippingSummaryReportOutput> useCaseOutput);
    UseCaseResponseDTO<LabelDTO> toUseCaseLabelDto(UseCaseOutput<LabelOutput> useCaseOutput);




}
