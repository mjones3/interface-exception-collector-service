package com.arcone.biopro.distribution.receiving.adapter.in.web.controller;

import com.arcone.biopro.distribution.receiving.adapter.in.web.mapper.CommandRequestDTOMapper;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.EnterShippingInformationRequestDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.ShippingInformationDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.receiving.adapter.in.web.mapper.UseCaseResponseMapper;
import com.arcone.biopro.distribution.receiving.domain.service.ShippingInformationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ShippingInformationController {
    private final UseCaseResponseMapper useCaseResponseMapper;
    private final ShippingInformationService shippingInformationService;
    private final CommandRequestDTOMapper commandRequestDTOMapper;

    @QueryMapping("enterShippingInformation")
    public Mono<UseCaseResponseDTO<ShippingInformationDTO>> enterShippingInformation(@Argument("enterShippingInformationRequest") EnterShippingInformationRequestDTO enterShippingInformationRequest) {
        log.debug("Request to enter shipping information : {}", enterShippingInformationRequest);
        return shippingInformationService.enterShippingInformation(commandRequestDTOMapper.toCommandInput(enterShippingInformationRequest))
            .map(useCaseResponseMapper::toUseCaseResponse);
    }
}
