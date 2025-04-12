package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CartonDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CreateCartonRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.CommandRequestDTOMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.UseCaseResponseMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CreateCartonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CartonController {

    private final CreateCartonService createCartonService;
    private final CommandRequestDTOMapper commandRequestDTOMapper;
    private final UseCaseResponseMapper useCaseResponseMapper;


    @MutationMapping("createCarton")
    public Mono<UseCaseResponseDTO<CartonDTO>> createCarton(@Argument("createCartonRequest") CreateCartonRequestDTO createCartonRequestDTO) {
        log.debug("Request to Create Carton: {}", createCartonRequestDTO);
        return createCartonService.createCarton(commandRequestDTOMapper.toInputCommand(createCartonRequestDTO))
            .map(useCaseResponseMapper::toUseCaseCreateCartonDTO);
    }

}
