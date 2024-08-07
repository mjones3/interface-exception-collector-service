package com.arcone.biopro.distribution.shipping.domain.repository;

import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShipmentItemPackedRepository extends ReactiveCrudRepository<ShipmentItemPacked, Long> {

    Flux<ShipmentItemPacked> findAllByShipmentItemId(Long shipmentItemId);
    Mono<Integer> countAllByShipmentItemId(Long shipmentItemId);

    Mono<Integer> countAllByUnitNumberAndProductCode(String unitNumber, String productCode);
    @Query("select * from bld_shipment_item_packed where shipment_item_id in (select id from bld_shipment_item bsi where shipment_id  = :shipmentId)")
    Flux<ShipmentItemPacked> listAllByShipmentId(@Param("shipmentId") Long shipmentId);
}


