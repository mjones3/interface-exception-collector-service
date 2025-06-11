package com.arcone.biopro.distribution.receiving.adapter.in.web.dto;

import com.arcone.biopro.distribution.receiving.application.dto.AddImportItemCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.CreateImportCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.EnterShippingInformationCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateBarcodeCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateDeviceInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateTemperatureCommandInput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateTransitTimeCommandInput;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommandRequestDTOMapper {

    EnterShippingInformationCommandInput toCommandInput(EnterShippingInformationRequestDTO enterShippingInformationRequestDTO);
    ValidateDeviceInput toCommandInput(ValidateDeviceRequestDTO validateDeviceRequestDTO);
    ValidateTemperatureCommandInput toCommandInput(ValidateTemperatureRequestDTO validateTemperatureRequestDTO);
    ValidateTransitTimeCommandInput toCommandInput(ValidateTransitTimeRequestDTO validateTransitTimeRequestDTO);
    CreateImportCommandInput toCommandInput(CreateImportRequestDTO createImportRequestDTO);
    ValidateBarcodeCommandInput toCommandInput(ValidateBarcodeRequestDTO validateBarcodeRequestDTO);
    AddImportItemCommandInput toCommandInput(AddImportItemRequestDTO addImportItemRequestDTO);
}
