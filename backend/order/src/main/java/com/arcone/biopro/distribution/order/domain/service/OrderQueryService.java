package com.arcone.biopro.distribution.order.domain.service;

import com.arcone.biopro.distribution.order.domain.model.OrderQueryCommand;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import reactor.core.publisher.Flux;

public interface OrderQueryService {

    Flux<OrderReport> searchOrders(OrderQueryCommand orderQueryCommand);
}
