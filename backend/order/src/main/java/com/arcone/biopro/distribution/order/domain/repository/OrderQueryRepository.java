package com.arcone.biopro.distribution.order.domain.repository;

import com.arcone.biopro.distribution.order.domain.model.OrderQueryCommand;
import com.arcone.biopro.distribution.order.domain.model.OrderReport;
import reactor.core.publisher.Flux;

public interface OrderQueryRepository {
    Flux<OrderReport> searchOrders(OrderQueryCommand orderQueryCommand);
}
