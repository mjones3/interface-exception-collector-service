package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.graphql.data.GraphQlRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@GraphQlRepository
public interface InventoryEntityRepository extends ReactiveCrudRepository<InventoryEntity, Long> {

    Mono<InventoryEntity> findByUnitNumberAndProductCodeAndInventoryStatus(String unitNumber, String productCode, InventoryStatus inventoryStatus);

    Mono<InventoryEntity> findByUnitNumberAndProductCode(String unitNumber, String productCode);

    Mono<InventoryEntity> findByUnitNumberAndProductCodeLike(String unitNumber, String productCodePattern);


    Mono<InventoryEntity> findByUnitNumberAndProductCodeAndLocation(String unitNumber, String productCode, String location);

    Mono<InventoryEntity> findByUnitNumberAndProductCodeLikeAndLocation(String unitNumber, String productCode, String location);


    Mono<Boolean> existsByLocationAndUnitNumberAndProductCode(String location, String unitNumber, String productCode);

    Flux<InventoryEntity> findAllByLocationAndProductFamilyAndAboRhInAndInventoryStatusOrderByExpirationDateAsc(String location, String productFamily, List<AboRhType> aboRh, InventoryStatus inventoryStatus);

    Mono<Long> countByLocationAndProductFamilyAndAboRhInAndInventoryStatusAndExpirationDateAfterAndIsLabeledTrue(String location, String productFamily, List<AboRhType> aboRh, InventoryStatus inventoryStatus, LocalDateTime dateTime);

    @Query("""
        SELECT COUNT(bld_inventory.id) FROM bld_inventory
        WHERE bld_inventory.location = :location
        AND bld_inventory.product_family = :productFamily
        AND (:aboRh IS NULL OR bld_inventory.abo_rh = ANY(CAST(:aboRh AS text[])))
        AND (:inventoryStatus IS NULL OR bld_inventory.status = :inventoryStatus)
        AND (:dateTime IS NULL OR bld_inventory.expiration_date > :dateTime)
        AND (:isLabeled IS NULL OR bld_inventory.is_labeled = :isLabeled)
        """)
    Mono<Long> countBy(String location, String productFamily, String[] aboRh, InventoryStatus inventoryStatus, LocalDateTime dateTime, boolean isLabeled);

    Flux<InventoryEntity> findByUnitNumber(String unitNumber);

    @Query("""
        SELECT * FROM bld_inventory
        WHERE bld_inventory.location = :location
          AND bld_inventory.product_family = :productFamily
          AND (:aboRh IS NULL OR abo_rh = ANY(CAST(:aboRh AS text[])))
          AND (:inventoryStatus IS NULL OR status = :inventoryStatus)
          AND (:finalDateTime IS NULL OR expiration_date <= :finalDateTime)
          AND (:startDateTime IS NULL OR expiration_date >= :startDateTime)
          AND (:isLabeled IS NULL OR is_labeled = :isLabeled)
        """)
    Flux<InventoryEntity> findBy(String location, String productFamily, String[] aboRh, InventoryStatus inventoryStatus, LocalDateTime startDateTime, LocalDateTime finalDateTime, boolean isLabeled);
}
