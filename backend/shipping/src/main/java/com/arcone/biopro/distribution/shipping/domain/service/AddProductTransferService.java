package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.application.dto.AddProductTransferCommandDTO;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import reactor.core.publisher.Mono;

public interface AddProductTransferService {

    Mono<RuleResponseDTO> addProductTransfer(AddProductTransferCommandDTO addProductTransferCommandDTO);
}
