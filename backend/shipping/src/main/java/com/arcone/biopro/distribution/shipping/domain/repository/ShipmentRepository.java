package com.arcone.biopro.distribution.shipping.domain.repository;

import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ShipmentRepository extends ReactiveCrudRepository<Shipment, Long> {
}
