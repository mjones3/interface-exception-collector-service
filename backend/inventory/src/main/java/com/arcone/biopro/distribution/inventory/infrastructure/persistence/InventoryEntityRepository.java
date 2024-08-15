package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.graphql.data.GraphQlRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@GraphQlRepository
public interface InventoryEntityRepository extends ReactiveCrudRepository<InventoryEntity, Long> {

    Mono<InventoryEntity> findByUnitNumberAndProductCode(String unitNumber, String productCode);

    Mono<Boolean> existsByLocationAndUnitNumberAndProductCode(String location, String unitNumber, String productCode);

    Flux<InventoryEntity> findAllByLocationAndProductFamilyAndAboRhInAndInventoryStatusOrderByExpirationDateAsc(String location, ProductFamily productFamily, List<AboRhType> aboRh, InventoryStatus inventoryStatus);

    Mono<Long> countByLocationAndProductFamilyAndAboRhInAndInventoryStatus(String location, ProductFamily productFamily, List<AboRhType> aboRh, InventoryStatus inventoryStatus);

    Flux<InventoryEntity> findAllByLocationAndProductFamilyAndAboRhInAndInventoryStatusAndExpirationDateBeforeOrderByExpirationDateAsc(String location, ProductFamily productFamily, List<AboRhType> aboRh, InventoryStatus inventoryStatus, LocalDateTime timeFrame);


}
