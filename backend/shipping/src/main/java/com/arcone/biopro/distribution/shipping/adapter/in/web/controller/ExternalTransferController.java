package com.arcone.biopro.distribution.shipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.CreateExternalTransferRequestDTO;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.mapper.ExternalTransferDomainMapper;
import com.arcone.biopro.distribution.shipping.application.usecase.CreateExternalTransferUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ExternalTransferController {

    private final CreateExternalTransferUseCase createExternalTransferUseCase;
    private final ExternalTransferDomainMapper externalTransferDomainMapper;

    @MutationMapping("createExternalTransfer")
    public Mono<RuleResponseDTO> createExternalTransfer(@Argument("createExternalTransferRequest") CreateExternalTransferRequestDTO createExternalTransferRequest) {
        log.debug("Request to create a external transfer {}", createExternalTransferRequest);
        return createExternalTransferUseCase.createExternalTransfer(externalTransferDomainMapper.toCommand(createExternalTransferRequest));
    }
}
