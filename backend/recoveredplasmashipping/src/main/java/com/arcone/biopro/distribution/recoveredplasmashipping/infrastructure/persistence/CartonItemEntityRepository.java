package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CartonItemEntityRepository extends ReactiveCrudRepository<CartonItemEntity,Long> {

    Flux<CartonItemEntity> findAllByCartonIdOrderByCreateDateAsc(Long cartonId);
    Mono<Integer> countByUnitNumberAndProductCode(String unitNumber, String productCode);
    Mono<CartonItemEntity> findByCartonIdAndProductCodeAndUnitNumber(Long cartonId, String productCode, String unitNumber);
    Mono<Void> deleteAllByCartonId(Long cartonId);

    @Query("select * from bld_recovered_plasma_shipment_carton_item where carton_id in (select id from bld_recovered_plasma_shipment_carton where recovered_plasma_shipment_id = :shipmentId) ")
    Flux<CartonItemEntity> findAllByShipmentId(@Param("shipmentId") Long shipmentId);

    @Query("select * from bld_recovered_plasma_shipment_carton_item where unit_number = :unitNumber and product_code = :productCode and carton_id in (select id from bld_recovered_plasma_shipment_carton where recovered_plasma_shipment_id = :shipmentId) ")
    Mono<CartonItemEntity> findByShipmentIdAndProduct(@Param("shipmentId") Long shipmentId , @Param("unitNumber") String unitNumber , @Param("productCode") String productCode);

}
