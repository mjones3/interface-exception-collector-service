package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RecoveredPlasmaProductTypeEntityRepository extends ReactiveCrudRepository<RecoveredPlasmaProductTypeEntity, Integer> {

    @Query("select type.* from lk_recovered_plasma_product_type type where type.product_type in (select product_type from lk_recovered_plasma_shipment_criteria where customer_code = :costumerCode and active = true ) and type.active = true order by type.order_number asc  ")
    Flux<RecoveredPlasmaProductTypeEntity> findAllByCostumer(@Param("costumerCode") String costumerCode);

    @Query("select type.* from lk_recovered_plasma_product_type type where type.id in (select product_type_id from lk_recovered_plasma_product_type_product_code where product_code = :productCode ) and type.active = true")
    Mono<RecoveredPlasmaProductTypeEntity> findByProductCode(@Param("productCode") String productCode);

}
