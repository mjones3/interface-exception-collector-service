package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CartonDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CloseCartonRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CreateCartonRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.CommandRequestDTOMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.UseCaseResponseMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CartonService;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CloseCartonService;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CreateCartonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CartonController {

    private final CreateCartonService createCartonService;
    private final CommandRequestDTOMapper commandRequestDTOMapper;
    private final UseCaseResponseMapper useCaseResponseMapper;
    private final CartonService cartonService;
    private final CloseCartonService closeCartonService;


    @MutationMapping("createCarton")
    public Mono<UseCaseResponseDTO<CartonDTO>> createCarton(@Argument("createCartonRequest") CreateCartonRequestDTO createCartonRequestDTO) {
        log.debug("Request to Create Carton: {}", createCartonRequestDTO);
        return createCartonService.createCarton(commandRequestDTOMapper.toInputCommand(createCartonRequestDTO))
            .map(useCaseResponseMapper::toUseCaseCreateCartonDTO);
    }

    @QueryMapping("findCartonById")
    public Mono<UseCaseResponseDTO<CartonDTO>> findCartonById(@Argument("cartonId") Long cartonId) {
        log.debug("Request to find carton by ID : {}", cartonId);
        return cartonService.findOneById(cartonId)
            .map(useCaseResponseMapper::toUseCaseCreateCartonDTO);
    }

    @MutationMapping("closeCarton")
    public Mono<UseCaseResponseDTO<CartonDTO>> closeCarton(@Argument("closeCartonRequest") CloseCartonRequestDTO closeCartonRequestDTO) {
        log.debug("Request to Close Carton: {}", closeCartonRequestDTO);
        return closeCartonService.closeCarton(commandRequestDTOMapper.toInputCommand(closeCartonRequestDTO))
            .map(useCaseResponseMapper::toUseCaseCreateCartonDTO);
    }
}
