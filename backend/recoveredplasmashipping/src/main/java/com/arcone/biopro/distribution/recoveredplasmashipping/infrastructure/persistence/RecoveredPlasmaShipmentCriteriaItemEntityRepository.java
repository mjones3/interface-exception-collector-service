package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RecoveredPlasmaShipmentCriteriaItemEntityRepository extends ReactiveCrudRepository<RecoveredPlasmaShipmentCriteriaItemEntity, Integer>{

    Flux<RecoveredPlasmaShipmentCriteriaItemEntity> findAllByCriteriaId(Integer criteriaId);

    static final String QUERY = "select cast(lrpsci.value as INTEGER) from bld_recovered_plasma_shipment_carton brpsc " +
        " inner join bld_recovered_plasma_shipment brps on brps.id = brpsc.recovered_plasma_shipment_id " +
        " inner join lk_recovered_plasma_shipment_criteria lrpsc on lrpsc.customer_code  = brps.customer_code and brps.product_type = lrpsc.product_type " +
        " inner join lk_recovered_plasma_shipment_criteria_item lrpsci on lrpsci.recovered_plasma_shipment_criteria_id  = lrpsc.id " +
        " where brpsc.id = :cartonId and lrpsci.type = :type ";

    @Query(QUERY)
    Mono<Integer> findByCartonIdAndType(@Param("cartonId") Long cartonId , @Param("type") String type);
}
