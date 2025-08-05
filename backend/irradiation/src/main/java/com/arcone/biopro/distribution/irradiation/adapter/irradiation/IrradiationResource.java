package com.arcone.biopro.distribution.irradiation.adapter.irradiation;

import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.CheckDigitResponseDTO;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.dto.ConfigurationResponseDTO;
import com.arcone.biopro.distribution.irradiation.adapter.in.web.mapper.ConfigurationDTOMapper;
import com.arcone.biopro.distribution.irradiation.application.dto.IrradiationInventoryOutput;
import com.arcone.biopro.distribution.irradiation.application.dto.BatchProductDTO;
import com.arcone.biopro.distribution.irradiation.application.usecase.CheckDigitUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateLotNumberUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateUnitNumberUseCase;
import com.arcone.biopro.distribution.irradiation.application.usecase.ValidateDeviceOnCloseBatchUseCase;

import com.arcone.biopro.distribution.irradiation.domain.repository.ConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class IrradiationResource {
    private final ValidateDeviceUseCase validateDeviceUseCase;
    private final ValidateUnitNumberUseCase validateUnitNumberUseCase;
    private final ConfigurationService configurationService;
    private final ConfigurationDTOMapper configurationDTOMapper;
    private final CheckDigitUseCase checkDigitUseCase;
    private final ValidateLotNumberUseCase validateLotNumberUseCase;
    private final ValidateDeviceOnCloseBatchUseCase validateDeviceOnCloseBatchUseCase;

    @QueryMapping
    public Mono<Boolean> validateDevice(@Argument String deviceId, @Argument String location) {
        return validateDeviceUseCase.execute(deviceId, location);
    }

    @QueryMapping
    public Flux<IrradiationInventoryOutput> validateUnit(@Argument String unitNumber, @Argument String location) {
        log.debug("Validating unit {} and location {}", unitNumber, location);
        return validateUnitNumberUseCase.execute(unitNumber, location);
    }

    @QueryMapping
    public Flux<ConfigurationResponseDTO> readConfiguration(@Argument List<String> keys) {
        return configurationService.readConfiguration(keys).map(configurationDTOMapper::toResponseDTO);
    }

    @QueryMapping
    public Mono<Boolean> validateLotNumber(@Argument String lotNumber, @Argument String type) {
        log.info("*** ENTERING validateLotNumber method with lotNumber: {} and type: {}", lotNumber, type);

        return validateLotNumberUseCase.execute(lotNumber, type)
            .doOnNext(result -> log.info("*** validateLotNumber result: {}", result))
            .onErrorResume(error -> {
                log.error("*** Error validating lot number: {}", error.getMessage());
                return Mono.just(false);
            });
    }

    @QueryMapping
    public Mono<CheckDigitResponseDTO> checkDigit(@Argument String unitNumber,
                                                  @Argument String checkDigit) {
        return checkDigitUseCase.checkDigit(unitNumber, checkDigit);
    }

    @QueryMapping
    public Flux<BatchProductDTO> validateDeviceOnCloseBatch(@Argument String deviceId, @Argument String location) {
        return validateDeviceOnCloseBatchUseCase.execute(deviceId, location);
    }
}
