package com.arcone.biopro.distribution.irradiation.adapter.irradiation;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.CheckDigitResponseDTO;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.ConfigurationResponseDTO;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.mapper.ConfigurationDTOMapper;
import com.arcone.biopro.distribution.irradiation.application.dto.IrradiationInventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.usecase.CheckDigitUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateUnitNumberUseCase;

import com.arcone.biopro.distribution.irradiation.domain.repository.ConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class IrradiationResource {
    private final ValidateDeviceUseCase validateDeviceUseCase;
    private final ValidateUnitNumberUseCase validateUnitNumberUseCase;
    private final ConfigurationService configurationService;
    private final ConfigurationDTOMapper configurationDTOMapper;
    private final CheckDigitUseCase checkDigitUseCase;

    @QueryMapping
    public Mono<Boolean> validateDevice(@Argument String deviceId, @Argument String location) {
        return validateDeviceUseCase.execute(deviceId, location);
    }

    @QueryMapping
    public Flux<IrradiationInventoryOutput> validateUnit(@Argument String unitNumber, @Argument String location) {
        return validateUnitNumberUseCase.execute(unitNumber, location);
    }

    @QueryMapping
    public Flux<ConfigurationResponseDTO> readConfiguration(@Argument List<String> keys) {
        return configurationService.readConfiguration(keys).map(configurationDTOMapper::toResponseDTO);
    }

    @QueryMapping
    public Mono<CheckDigitResponseDTO> checkDigit(@Argument String unitNumber,
                                                  @Argument String checkDigit) {
        return checkDigitUseCase.checkDigit(unitNumber, checkDigit);
    }
}
