package com.arcone.biopro.distribution.shipping.infrastructure.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.graphql.data.GraphQlRepository;
import reactor.core.publisher.Mono;

@GraphQlRepository
public interface ProductLocationHistoryEntityRepository extends ReactiveCrudRepository<ProductLocationHistoryEntity, Long> {

    @Query("select * FROM bld_product_location_history WHERE unit_number = :unitNumber AND product_code = :productCode order by create_date DESC limit 1")
    Mono<ProductLocationHistoryEntity> findLastHistoryByProduct(@Param("unitNumber") String unitNumber , @Param("productCode") String productCode);

}
