package com.arcone.biopro.distribution.shippingservice.domain.repository;

import com.arcone.biopro.distribution.shippingservice.domain.model.OrderItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {

    Flux<OrderItem> findAllByOrderId(Long orderId);

}
