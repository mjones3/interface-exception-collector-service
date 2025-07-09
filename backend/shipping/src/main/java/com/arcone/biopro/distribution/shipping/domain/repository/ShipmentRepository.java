package com.arcone.biopro.distribution.shipping.domain.repository;

import com.arcone.biopro.distribution.shipping.domain.model.Shipment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ShipmentRepository extends ReactiveCrudRepository<Shipment, Long> {

    @Query("select * from bld_shipment where id = (select shipment_id from bld_shipment_item where id = :shipmentItemId)")
    Mono<Shipment> findShipmentByItemId(@Param("shipmentItemId") Long shipmentItemId);
}
