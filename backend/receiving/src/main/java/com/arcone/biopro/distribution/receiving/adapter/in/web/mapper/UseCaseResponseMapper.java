package com.arcone.biopro.distribution.receiving.adapter.in.web.mapper;

import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.DeviceDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.ImportDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.ShippingInformationDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.ValidationResultDTO;
import com.arcone.biopro.distribution.receiving.application.dto.DeviceOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ImportOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ShippingInformationOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidationResultOutput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring" , uses = {UseCaseNotificationDtoMapper.class})
public interface UseCaseResponseMapper {

    UseCaseResponseDTO<ShippingInformationDTO> toUseCaseResponse(UseCaseOutput<ShippingInformationOutput> useCaseOutput);

    UseCaseResponseDTO<DeviceDTO> toDeviceValidationUseCaseResponse(UseCaseOutput<DeviceOutput> useCaseOutput);

    UseCaseResponseDTO<ValidationResultDTO> toValidateUseCaseResponse(UseCaseOutput<ValidationResultOutput> useCaseOutput);

    UseCaseResponseDTO<ImportDTO> toCreateImportUseCaseResponse(UseCaseOutput<ImportOutput> useCaseOutput);

    UseCaseResponseDTO<Void> toVoidUseCaseResponse(UseCaseOutput<Void> useCaseOutput);
}
