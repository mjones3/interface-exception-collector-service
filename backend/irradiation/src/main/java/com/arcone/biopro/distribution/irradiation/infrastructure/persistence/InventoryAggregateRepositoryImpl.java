package com.arcone.biopro.distribution.irradiation.infrastructure.persistence;

import com.arcone.biopro.distribution.irradiation.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.irradiation.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.irradiation.domain.repository.InventoryAggregateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.graphql.data.GraphQlRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor
@GraphQlRepository
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryAggregateRepositoryImpl implements InventoryAggregateRepository {

    InventoryEntityRepository inventoryEntityRepository;

    InventoryEntityMapper mapper;

    ProductFamilyEntityRepository productFamilyEntityRepository;
    private final PropertyEntityRepository propertyEntityRepository;

    @Override
    public Flux<InventoryAggregate> findByUnitNumber(String unitNumber) {
        return inventoryEntityRepository.findByUnitNumber(unitNumber)
            .map(mapper::toAggregate)
            .flatMap(this::populateProperties);
    }

    @Override
    public Mono<InventoryAggregate> findByUnitNumberAndProductCode(String unitNumber, String productCode) {
        return inventoryEntityRepository.findByUnitNumberAndProductCodeLike(unitNumber, createProductCodePattern(productCode))
            .map(mapper::toDomain)
            .flatMap(inventory -> Mono.just(InventoryAggregate.builder().inventory(inventory).build()))
            .flatMap(this::populateProperties);
    }

    @Override
    public Mono<InventoryAggregate> saveInventory(InventoryAggregate inventoryAggregate) {
        return inventoryEntityRepository
            .save(mapper.toEntity(inventoryAggregate.getInventory()))
            .flatMap(inventoryEntity -> this.saveProperties(inventoryEntity, inventoryAggregate))
            .then(Mono.just(inventoryAggregate));
    }

    private Mono<InventoryAggregate> saveProperties(InventoryEntity inventoryEntity, InventoryAggregate inventoryAggregate) {
        if (Objects.isNull(inventoryAggregate.getProperties())) {
            return Mono.just(inventoryAggregate);
        }

        return propertyEntityRepository.deleteAll(propertyEntityRepository.findByInventoryId(inventoryEntity.getId()))
                .then(propertyEntityRepository.saveAll(inventoryAggregate.getProperties().stream()
                    .map(p -> mapper.toEntity(p, inventoryEntity))
                    .toList())
                    .then(Mono.just(inventoryAggregate)));
    }

    @Override
    public Flux<InventoryAggregate> findAllAvailableShortDate(String location, String productFamily, AboRhCriteria aboRh, String temperatureCategory) {
        return productFamilyEntityRepository.findByProductFamily(productFamily)
            .flatMapMany(pf -> inventoryEntityRepository.findBy(
                location,
                productFamily,
                aboRHArray(aboRh),
                InventoryStatus.AVAILABLE,
                temperatureCategory,
                getFinalDateTime(pf)))
            .map(mapper::toAggregate);
    }

    @Override
    public Mono<Long> countAllAvailable(String location, String productFamily, AboRhCriteria aboRh, String temperatureCategory) {
        return inventoryEntityRepository.countBy(location, productFamily, aboRHArray(aboRh), InventoryStatus.AVAILABLE, temperatureCategory);
    }

    private static String[] aboRHArray(AboRhCriteria aboRh) {
        return aboRh.getAboRhTypes()
            .stream()
            .map(AboRhType::name)
            .toArray(String[]::new);
    }

    @Override
    public Mono<InventoryAggregate> findByLocationAndUnitNumberAndProductCode(String location, String unitNumber, String productCode) {
        return inventoryEntityRepository.findByUnitNumberAndProductCodeLikeAndInventoryLocation(unitNumber, createProductCodePattern(productCode), location)
            .map(mapper::toDomain)
            .flatMap(inventory -> Mono.just(InventoryAggregate.builder().inventory(inventory).build()))
            .flatMap(this::populateProperties);
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

    private Mono<InventoryAggregate> populateProperties(InventoryAggregate inventoryAggregate) {
        return propertyEntityRepository.findByInventoryId(inventoryAggregate.getInventory().getId())
            .map(mapper::toDomain)
            .collectList()
            .map(inventoryAggregate::populateProperties);
    }

}
