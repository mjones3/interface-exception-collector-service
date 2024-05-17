package com.arcone.biopro.distribution.shippingservice.domain.repository;

import com.arcone.biopro.distribution.shippingservice.domain.model.ShipmentItem;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ShipmentItemRepository extends ReactiveCrudRepository<ShipmentItem, Long> {
}
