package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.domain.model.PickList;
import reactor.core.publisher.Mono;

public interface PickListService {

    Mono<PickList> generatePickList(Long orderId);
}
