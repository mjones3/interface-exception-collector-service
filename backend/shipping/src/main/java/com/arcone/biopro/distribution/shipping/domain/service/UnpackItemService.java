package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.UnpackItemsRequest;
import reactor.core.publisher.Mono;

public interface UnpackItemService {

    Mono<RuleResponseDTO> unpackItems(UnpackItemsRequest unpackItemsRequest);
}
