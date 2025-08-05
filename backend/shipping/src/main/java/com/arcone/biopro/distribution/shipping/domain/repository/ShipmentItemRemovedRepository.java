package com.arcone.biopro.distribution.shipping.domain.repository;

import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemRemoved;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ShipmentItemRemovedRepository extends ReactiveCrudRepository<ShipmentItemRemoved, Long> {

    Flux<ShipmentItemRemoved> findAllByShipmentId(Long shipmentId);

}


