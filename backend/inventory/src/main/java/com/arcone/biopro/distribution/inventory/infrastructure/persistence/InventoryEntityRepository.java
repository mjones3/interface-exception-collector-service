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

    Mono<InventoryEntity> findByUnitNumberAndProductCodeLikeAndInventoryLocation(String unitNumber, String productCode, String inventoryLocation);

    @Query("""
        SELECT COUNT(i.id) FROM bld_inventory AS i
        LEFT JOIN bld_inventory_property AS p ON
            p.inventory_id = i.id
            AND p.key = 'TIMEZONE_RELEVANT'
            AND p.value = 'Y'
        WHERE i.inventory_location = :location
        AND i.product_family = :productFamily
        AND i.abo_rh = ANY(CAST(:aboRh AS text[]))
        AND i.status = :inventoryStatus
        AND (:temperatureCategory IS NULL OR i.temperature_category = :temperatureCategory)
        AND ((p.id is null and Date(i.expiration_date) >= Date(now() AT TIME ZONE COALESCE(i.collection_timezone, 'UTC')))
            OR (p.id is not null and i.expiration_date >= (now() AT TIME ZONE COALESCE(i.expiration_timezone, i.collection_timezone, 'UTC')))
        )
        AND i.is_labeled = true
        AND i.unsuitable_reason is null
        AND (i.quarantines is null OR jsonb_array_length(i.quarantines) = 0)
        """)
    Mono<Long> countBy(
        @Param("inventoryLocation") String location,
        @Param("productFamily") String productFamily,
        @Param("aboRh") String[] aboRh,
        @Param("inventoryStatus") InventoryStatus inventoryStatus,
        @Param("temperatureCategory") String temperatureCategory);

    Flux<InventoryEntity> findByUnitNumber(String unitNumber);

    @Query("""
        SELECT * FROM bld_inventory AS i
        LEFT JOIN bld_inventory_property AS p ON
            p.inventory_id = i.id
            AND p.key = 'TIMEZONE_RELEVANT'
            AND p.value = 'Y'
        WHERE i.inventory_location = :location
          AND i.product_family = :productFamily
          AND i.abo_rh = ANY(CAST(:aboRh AS text[]))
          AND i.status = :inventoryStatus
          AND (:temperatureCategory IS NULL OR i.temperature_category = :temperatureCategory)
          AND ((p.id is null and Date(i.expiration_date) >= Date(now() AT TIME ZONE COALESCE(i.collection_timezone, 'UTC')))
            OR (p.id is not null and i.expiration_date >= (now() AT TIME ZONE COALESCE(i.expiration_timezone, i.collection_timezone, 'UTC')))
          )
          AND DATE(i.expiration_date) <= DATE(:finalDateTime)
          AND i.is_labeled = true
          AND i.unsuitable_reason is null
          AND (i.quarantines is null OR jsonb_array_length(i.quarantines) = 0)
        ORDER BY i.expiration_date ASC
        """)
    Flux<InventoryEntity> findBy(
        @Param("inventoryLocation") String location,
        @Param("productFamily") String productFamily,
        @Param("aboRh") String[] aboRh,
        @Param("inventoryStatus") InventoryStatus inventoryStatus,
        @Param("temperatureCategory") String temperatureCategory,
        @Param("finalDateTime") LocalDateTime finalDateTime
    );
}
