package com.arcone.biopro.distribution.receiving.adapter.in.web.controller;

import com.arcone.biopro.distribution.receiving.adapter.in.web.mapper.CommandRequestDTOMapper;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.ValidateTemperatureRequestDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.ValidateTransitTimeRequestDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.ValidationResultDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.mapper.UseCaseResponseMapper;
import com.arcone.biopro.distribution.receiving.domain.service.ValidateTemperatureService;
import com.arcone.biopro.distribution.receiving.domain.service.ValidateTransitTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProductConsequenceController {
    private final UseCaseResponseMapper useCaseResponseMapper;
    private final ValidateTemperatureService validateTemperatureService;
    private final CommandRequestDTOMapper commandRequestDTOMapper;
    private final ValidateTransitTimeService validateTransitTimeService;


    @QueryMapping("validateTemperature")
    public Mono<UseCaseResponseDTO<ValidationResultDTO>> validateTemperature(@Argument("validateTemperatureRequest") ValidateTemperatureRequestDTO validateTemperatureRequest) {
        log.debug("Request to validate temperature : {}", validateTemperatureRequest);
        return validateTemperatureService.validateTemperature(commandRequestDTOMapper.toCommandInput(validateTemperatureRequest))
            .map(useCaseResponseMapper::toValidateUseCaseResponse);
    }

    @QueryMapping("validateTransitTime")
    public Mono<UseCaseResponseDTO<ValidationResultDTO>> validateTransitTime(@Argument("validateTransitTimeRequest") ValidateTransitTimeRequestDTO validateTransitTimeRequest) {
        log.debug("Request to validate transit time: {}", validateTransitTimeRequest);
        return validateTransitTimeService.validateTransitTime(commandRequestDTOMapper.toCommandInput(validateTransitTimeRequest))
            .map(useCaseResponseMapper::toValidateUseCaseResponse);
    }
}
