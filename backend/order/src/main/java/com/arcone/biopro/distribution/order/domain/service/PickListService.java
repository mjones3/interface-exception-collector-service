package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.application.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.order.domain.model.PickList;
import reactor.core.publisher.Mono;

public interface PickListService {

    Mono<UseCaseResponseDTO<PickList>> generatePickList(final Long orderId, final boolean skipInventoryUnavailable);
}
