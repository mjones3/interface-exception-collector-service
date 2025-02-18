package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
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

    Flux<InventoryEntity> findAllByLocationAndProductFamilyAndAboRhInAndInventoryStatusAndIsLabeledTrueAndExpirationDateBetweenOrderByExpirationDateAsc(String location, String productFamily, List<AboRhType> aboRh, InventoryStatus inventoryStatus, LocalDateTime initialDate, LocalDateTime finalDate);

    Mono<InventoryEntity> findByUnitNumber(String unitNumber);
}
