package com.arcone.biopro.distribution.order.domain.repository;

import com.arcone.biopro.distribution.order.domain.model.OrderShipment;
import reactor.core.publisher.Mono;

public interface OrderShipmentRepository {

    Mono<OrderShipment> insert(final OrderShipment orderShipment);
}
