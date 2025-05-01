package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.CartonItemEntityMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonItemEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

class CartonItemEntityMapperTest {

    private final CartonItemEntityMapper mapper = Mappers.getMapper(CartonItemEntityMapper.class);

    @Test
    void shouldMapCartonItemToEntity() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime nowZ = ZonedDateTime.now();
        CartonItem cartonItem = CartonItem.fromRepository(
            1L,                    // id
            1L,          // cartonId
            "UNIT456",            // unitNumber
            "PROD789",            // productCode
            "Test Product",       // productDescription
            "TYPE_A",             // productType
            500,                // volume
            250,                // weight
            "EMP123",             // packedByEmployeeId
            "A+",                 // aboRh
            "ACTIVE",             // status
            now.plusDays(30),     // expirationDate
            nowZ.minusDays(1),     // collectionDate
            nowZ,                  // createDate
            nowZ                   // modificationDate
            ,"verify-employee", ZonedDateTime.now()
        );

        // When
        CartonItemEntity entity = mapper.toEntity(cartonItem);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(cartonItem.getId());
        assertThat(entity.getCartonId()).isEqualTo(cartonItem.getCartonId());
        assertThat(entity.getUnitNumber()).isEqualTo(cartonItem.getUnitNumber());
        assertThat(entity.getProductCode()).isEqualTo(cartonItem.getProductCode());
        assertThat(entity.getProductDescription()).isEqualTo(cartonItem.getProductDescription());
        assertThat(entity.getProductType()).isEqualTo(cartonItem.getProductType());
        assertThat(entity.getVolume()).isEqualTo(cartonItem.getVolume());
        assertThat(entity.getWeight()).isEqualTo(cartonItem.getWeight());
        assertThat(entity.getPackedByEmployeeId()).isEqualTo(cartonItem.getPackedByEmployeeId());
        assertThat(entity.getAboRh()).isEqualTo(cartonItem.getAboRh());
        assertThat(entity.getStatus()).isEqualTo(cartonItem.getStatus());
        assertThat(entity.getExpirationDate()).isEqualTo(cartonItem.getExpirationDate());
        assertThat(entity.getCollectionDate()).isEqualTo(cartonItem.getCollectionDate());
        assertThat(entity.getCreateDate()).isEqualTo(cartonItem.getCreateDate());
        assertThat(entity.getModificationDate()).isEqualTo(cartonItem.getModificationDate());
        assertThat(entity.getVerificationDate()).isEqualTo(cartonItem.getVerifyDate());
        assertThat(entity.getVerifiedByEmployeeId()).isEqualTo(cartonItem.getVerifiedByEmployeeId());
    }

    @Test
    void shouldMapEntityToCartonItem() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime nowZ = ZonedDateTime.now();
        CartonItemEntity entity = CartonItemEntity.builder().build();
        entity.setId(1L);
        entity.setCartonId(1L);
        entity.setUnitNumber("UNIT456");
        entity.setProductCode("PROD789");
        entity.setProductDescription("Test Product");
        entity.setProductType("TYPE_A");
        entity.setVolume(500);
        entity.setWeight(250);
        entity.setPackedByEmployeeId("EMP123");
        entity.setAboRh("A+");
        entity.setStatus("ACTIVE");
        entity.setExpirationDate(now.plusDays(30));
        entity.setCollectionDate(nowZ.minusDays(1));
        entity.setCreateDate(nowZ);
        entity.setModificationDate(nowZ);

        // When
        CartonItem cartonItem = mapper.entityToModel(entity);

        // Then
        assertThat(cartonItem).isNotNull();
        assertThat(cartonItem.getId()).isEqualTo(entity.getId());
        assertThat(cartonItem.getCartonId()).isEqualTo(entity.getCartonId());
        assertThat(cartonItem.getUnitNumber()).isEqualTo(entity.getUnitNumber());
        assertThat(cartonItem.getProductCode()).isEqualTo(entity.getProductCode());
        assertThat(cartonItem.getProductDescription()).isEqualTo(entity.getProductDescription());
        assertThat(cartonItem.getProductType()).isEqualTo(entity.getProductType());
        assertThat(cartonItem.getVolume()).isEqualTo(entity.getVolume());
        assertThat(cartonItem.getWeight()).isEqualTo(entity.getWeight());
        assertThat(cartonItem.getPackedByEmployeeId()).isEqualTo(entity.getPackedByEmployeeId());
        assertThat(cartonItem.getAboRh()).isEqualTo(entity.getAboRh());
        assertThat(cartonItem.getStatus()).isEqualTo(entity.getStatus());
        assertThat(cartonItem.getExpirationDate()).isEqualTo(entity.getExpirationDate());
        assertThat(cartonItem.getCollectionDate()).isEqualTo(entity.getCollectionDate());
        assertThat(cartonItem.getCreateDate()).isEqualTo(entity.getCreateDate());
        assertThat(cartonItem.getModificationDate()).isEqualTo(entity.getModificationDate());
    }

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        // When
        CartonItem result = mapper.entityToModel(null);

        // Then
        assertThat(result).isNull();
    }
}

