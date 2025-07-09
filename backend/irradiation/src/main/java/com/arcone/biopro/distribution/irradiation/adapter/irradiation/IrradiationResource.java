package com.arcone.biopro.distribution.irradiation.adapter.irradiation;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.ConfigurationResponseDTO;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.mapper.ConfigurationDTOMapper;
import com.arcone.biopro.distribution.irradiation.adapter.irradiation.dto.DeviceValidationResult;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateUnitNumberUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Inventory;

import com.arcone.biopro.distribution.irradiation.domain.service.ConfigurationService;
import lombok.RequiredArgsConstructor;
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

    @QueryMapping
    public Mono<DeviceValidationResult> validateDevice(@Argument String deviceId, @Argument String location) {
        return validateDeviceUseCase.execute(deviceId, location)
                .map(result -> new DeviceValidationResult(true, null))
                .onErrorResume(throwable -> {
                    String errorMessage = throwable.getMessage();
                    return Mono.just(new DeviceValidationResult(false, errorMessage));
                });
    }

    @QueryMapping
    public Flux<Inventory> validateUnit(@Argument String unitNumber, @Argument String location) {
        return validateUnitNumberUseCase.execute(unitNumber, location);
    }

    @QueryMapping
    public Flux<ConfigurationResponseDTO> readConfiguration(@Argument List<String> keys) {
        return configurationService.readConfiguration(keys).map(configurationDTOMapper::toResponseDTO);
    }
}
