package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.CartonDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.VerifyCartonItemRequestDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.CommandRequestDTOMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.UseCaseResponseMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.VerifyCartonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class VerifyCartonController {

    private final CommandRequestDTOMapper commandRequestDTOMapper;
    private final UseCaseResponseMapper useCaseResponseMapper;
    private final VerifyCartonService verifyCartonService;


    @MutationMapping("verifyCarton")
    public Mono<UseCaseResponseDTO<CartonDTO>> verifyCarton(@Argument("verifyCartonItemRequest") VerifyCartonItemRequestDTO verifyCartonItemRequestDTO) {
        log.debug("Request to Verify Carton: {}", verifyCartonItemRequestDTO);
        return verifyCartonService.verifyCartonItem(commandRequestDTOMapper.toInputCommand(verifyCartonItemRequestDTO))
            .map(useCaseResponseMapper::toUseCaseVerifyCartonDTO);
    }

}
