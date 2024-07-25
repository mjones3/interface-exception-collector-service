package com.arcone.biopro.distribution.shipping.domain.repository;

import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemShortDateProduct;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ShipmentItemShortDateProductRepository extends ReactiveCrudRepository<ShipmentItemShortDateProduct, Long> {

    Flux<ShipmentItemShortDateProduct> findAllByShipmentItemId(Long shipmentItemId);
}
