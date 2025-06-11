package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProductEntityRepository extends ReactiveCrudRepository<ProductEntity,Long> {

    @Query("select p.* from lk_product p where p.active = true and p.product_code in (select product_code from lk_product_family_product l " +
        " inner join lk_product_family lpf on lpf.id = l.product_family_id " +
        " where l.product_code = :productCode " +
        " and lpf.temperature_category = :temperatureCategory)")
    Mono<ProductEntity> findByProductCodeAndTemperatureCategory(@Param("productCode") String productCode , @Param("temperatureCategory") String temperatureCategory);

    Mono<ProductEntity> findFirstByProductCodeAndActiveIsTrue(String productCode);
}
