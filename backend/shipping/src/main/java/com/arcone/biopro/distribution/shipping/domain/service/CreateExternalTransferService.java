package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.domain.model.CreateExternalTransferCommand;
import reactor.core.publisher.Mono;

public interface CreateExternalTransferService {

    Mono<RuleResponseDTO> createExternalTransfer(CreateExternalTransferCommand createExternalTransferCommand);

}
