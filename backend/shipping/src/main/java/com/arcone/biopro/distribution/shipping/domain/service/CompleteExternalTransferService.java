package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.application.dto.CompleteExternalTransferCommandDTO;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import reactor.core.publisher.Mono;

public interface CompleteExternalTransferService {

    Mono<RuleResponseDTO> completeExternalTransfer(CompleteExternalTransferCommandDTO completeExternalTransferCommandDTO);
}
