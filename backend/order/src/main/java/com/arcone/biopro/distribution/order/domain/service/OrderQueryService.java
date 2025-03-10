package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.domain.model.OrderQueryCommand;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import com.arcone.biopro.distribution.order.domain.model.Page;
import reactor.core.publisher.Mono;

public interface OrderQueryService {

    Mono<Page<OrderReport>> search(OrderQueryCommand orderQueryCommand);

}
