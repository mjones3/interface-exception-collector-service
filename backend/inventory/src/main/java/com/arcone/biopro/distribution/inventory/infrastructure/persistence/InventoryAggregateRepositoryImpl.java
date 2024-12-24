package com.arcone.biopro.distribution.inventory.infrastructure.persistence;

import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.graphql.data.GraphQlRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@GraphQlRepository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryAggregateRepositoryImpl implements InventoryAggregateRepository {

    InventoryEntityRepository inventoryEntityRepository;

    InventoryEntityMapper inventoryEntityMapper;

    ProductFamilyEntityRepository productFamilyEntityRepository;

    @Override
    public Mono<InventoryAggregate> findByUnitNumberAndProductCode(String unitNumber, String productCode) {
        return inventoryEntityRepository.findByUnitNumberAndProductCodeLike(unitNumber, createProductCodePattern(productCode))
            .map(inventoryEntityMapper::toDomain)
            .flatMap(inventory -> Mono.just(InventoryAggregate.builder().inventory(inventory).build()));
    }

    @Override
    public Mono<InventoryAggregate> saveInventory(InventoryAggregate inventoryAggregate) {
        return inventoryEntityRepository
            .save(inventoryEntityMapper.toEntity(inventoryAggregate.getInventory()))
            .then(Mono.just(inventoryAggregate));
    }

    @Override
    public Mono<Boolean> existsByLocationAndUnitNumberAndProductCode(String location, String unitNumber, String productCode) {
        return inventoryEntityRepository.existsByLocationAndUnitNumberAndProductCode(location, unitNumber, productCode);
    }

    @Override
    public Flux<InventoryAggregate> findAllAvailable(String location, String productFamily, AboRhCriteria aboRh) {
        return inventoryEntityRepository.findAllByLocationAndProductFamilyAndAboRhInAndInventoryStatusOrderByExpirationDateAsc(location, productFamily, aboRh.getAboRhTypes(), InventoryStatus.AVAILABLE)
            .map(inventoryEntityMapper::toAggregate);
    }

    @Override
    public Flux<InventoryAggregate> findAllAvailableShortDate(String location, String productFamily, AboRhCriteria aboRh) {
        return productFamilyEntityRepository.findByProductFamily(productFamily)
            .flatMapMany(pf -> inventoryEntityRepository.findAllByLocationAndProductFamilyAndAboRhInAndInventoryStatusAndIsLabeledTrueAndExpirationDateBetweenOrderByExpirationDateAsc(location, productFamily, aboRh.getAboRhTypes(), InventoryStatus.AVAILABLE, LocalDateTime.now(), getFinalDateTime(pf)))
            .map(inventoryEntityMapper::toAggregate);
    }

    @Override
    public Mono<Long> countAllAvailable(String location, String productFamily, AboRhCriteria aboRh) {
        return inventoryEntityRepository.countByLocationAndProductFamilyAndAboRhInAndInventoryStatusAndExpirationDateAfterAndIsLabeledTrue(location, productFamily, aboRh.getAboRhTypes(), InventoryStatus.AVAILABLE, LocalDateTime.now());
    }

    @Override
    public Mono<InventoryAggregate> findByLocationAndUnitNumberAndProductCode(String location, String unitNumber, String productCode) {
        return inventoryEntityRepository.findByUnitNumberAndProductCodeLikeAndLocation(unitNumber, createProductCodePattern(productCode), location)
            .map(inventoryEntityMapper::toDomain)
            .flatMap(inventory -> Mono.just(InventoryAggregate.builder().inventory(inventory).build()));
    }

    private String createProductCodePattern(String productCode) {
        if (productCode != null && productCode.length() == 7) {
            return productCode.replaceAll("^(E\\d{4})([A-Z0-9]{2})$", "$1%$2");
        }

        return productCode;
    }

    private LocalDateTime getFinalDateTime(ProductFamilyEntity productFamily) {
        return LocalDateTime.now().plusDays(productFamily.getTimeFrame());
    }

}
