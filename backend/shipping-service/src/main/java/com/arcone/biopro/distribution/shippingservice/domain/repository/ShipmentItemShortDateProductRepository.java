package com.arcone.biopro.distribution.shippingservice.domain.repository;

import com.arcone.biopro.distribution.shippingservice.domain.model.ShipmentItemShortDateProduct;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ShipmentItemShortDateProductRepository extends ReactiveCrudRepository<ShipmentItemShortDateProduct, Long> {

    Flux<ShipmentItemShortDateProduct> findAllByShipmentItemId(Long shipmentItemId);
}
