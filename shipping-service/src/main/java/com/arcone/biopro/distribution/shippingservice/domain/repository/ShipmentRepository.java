package com.arcone.biopro.distribution.shippingservice.domain.repository;

import com.arcone.biopro.distribution.shippingservice.domain.model.Shipment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ShipmentRepository extends ReactiveCrudRepository<Shipment, Long> {
}
