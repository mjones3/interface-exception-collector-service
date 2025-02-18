package com.arcone.biopro.distribution.shipping.domain.repository;

import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ShipmentItemRepository extends ReactiveCrudRepository<ShipmentItem, Long> {

    Flux<ShipmentItem> findAllByShipmentId(Long shipmentId);
}
