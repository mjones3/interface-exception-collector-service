package com.arcone.biopro.distribution.receiving.adapter.in.web.controller;

import com.arcone.biopro.distribution.receiving.adapter.in.web.mapper.CommandRequestDTOMapper;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.DeviceDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.ValidateDeviceRequestDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.mapper.UseCaseResponseMapper;
import com.arcone.biopro.distribution.receiving.domain.service.ValidateDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DeviceController {

    private final UseCaseResponseMapper useCaseResponseMapper;
    private final ValidateDeviceService validateDeviceService;
    private final CommandRequestDTOMapper commandRequestDTOMapper;

    @QueryMapping("validateDevice")
    public Mono<UseCaseResponseDTO<DeviceDTO>> validateDevice(@Argument("validateDeviceRequest") ValidateDeviceRequestDTO validateDeviceRequest) {
        log.debug("Request to validate device : {}", validateDeviceRequest);
        return validateDeviceService.validateDevice(commandRequestDTOMapper.toCommandInput(validateDeviceRequest))
            .map(useCaseResponseMapper::toDeviceValidationUseCaseResponse);
    }
}
