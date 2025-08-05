package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.application.dto.GetUnlabeledPackedItemsRequest;
import com.arcone.biopro.distribution.shipping.application.dto.GetUnlabeledProductsRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import reactor.core.publisher.Mono;

public interface GetUnlabeledPackedItemsService {
    Mono<RuleResponseDTO> getUnlabeledPackedItems(GetUnlabeledPackedItemsRequest getUnlabeledPackedItemsRequest);
}
