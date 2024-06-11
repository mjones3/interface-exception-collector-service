package com.arcone.biopro.distribution.shippingservice.domain.repository;

import com.arcone.biopro.distribution.shippingservice.domain.model.ShipmentItemPacked;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ShipmentItemPackedRepository extends ReactiveCrudRepository<ShipmentItemPacked, Long> {

    Flux<ShipmentItemPacked> findAllByShipmentItemId(Long shipmentItemId);
}
