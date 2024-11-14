package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.application.dto.CompleteShipmentRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import reactor.core.publisher.Mono;

public interface CompleteShipmentService {

    Mono<RuleResponseDTO> completeShipment(CompleteShipmentRequest request);
}
