package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.graphql.data.GraphQlRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@GraphQlRepository
public interface InventoryEntityRepository extends ReactiveCrudRepository<InventoryEntity, Long> {

    Mono<InventoryEntity> findByUnitNumberAndProductCodeAndInventoryStatus(String unitNumber, String productCode, InventoryStatus inventoryStatus);

    Mono<InventoryEntity> findByUnitNumberAndProductCode(String unitNumber, String productCode);

    Mono<InventoryEntity> findByUnitNumberAndProductCodeLike(String unitNumber, String productCodePattern);

    Mono<InventoryEntity> findByUnitNumberAndProductCodeLikeAndLocation(String unitNumber, String productCode, String location);

    @Query("""
        SELECT COUNT(bld_inventory.id) FROM bld_inventory
        WHERE bld_inventory.location = :location
        AND bld_inventory.product_family = :productFamily
        AND bld_inventory.abo_rh = ANY(CAST(:aboRh AS text[]))
        AND bld_inventory.status = :inventoryStatus
        AND (:temperatureCategory IS NULL OR bld_inventory.temperature_category = :temperatureCategory)
        AND bld_inventory.expiration_date > :startDateTime
        AND bld_inventory.is_labeled = true
        AND bld_inventory.unsuitable_reason is null
        AND (bld_inventory.quarantines is null OR jsonb_array_length(bld_inventory.quarantines) = 0)
        """)
    Mono<Long> countBy(
        @Param("location") String location,
        @Param("productFamily") String productFamily,
        @Param("aboRh") String[] aboRh,
        @Param("inventoryStatus") InventoryStatus inventoryStatus,
        @Param("temperatureCategory") String temperatureCategory,
        @Param("startDateTime") LocalDateTime startDateTime
    );

    Flux<InventoryEntity> findByUnitNumber(String unitNumber);

    @Query("""
        SELECT * FROM bld_inventory
        WHERE location = :location
          AND bld_inventory.product_family = :productFamily
          AND bld_inventory.abo_rh = ANY(CAST(:aboRh AS text[]))
          AND bld_inventory.status = :inventoryStatus
          AND (:temperatureCategory IS NULL OR bld_inventory.temperature_category = :temperatureCategory)
          AND bld_inventory.expiration_date >= :startDateTime
          AND bld_inventory.expiration_date <= :finalDateTime
          AND bld_inventory.is_labeled = true
          AND bld_inventory.unsuitable_reason is null
          AND (bld_inventory.quarantines is null OR jsonb_array_length(bld_inventory.quarantines) = 0)
        ORDER BY expiration_date ASC
        """)
    Flux<InventoryEntity> findBy(
        @Param("location") String location,
        @Param("productFamily") String productFamily,
        @Param("aboRh") String[] aboRh,
        @Param("inventoryStatus") InventoryStatus inventoryStatus,
        @Param("temperatureCategory") String temperatureCategory,
        @Param("startDateTime") LocalDateTime startDateTime,
        @Param("finalDateTime") LocalDateTime finalDateTime
    );
}
