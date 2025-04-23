package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonItemOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonItemOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Inventory;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class CartonItemOutputMapperTest {

    private CartonItemOutputMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(CartonItemOutputMapper.class);
    }

    @Test
    void shouldMapCartonItemToCartonItemOutput() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        CartonItem cartonItem = Mockito.mock(CartonItem.class);

            Mockito.when(cartonItem.getUnitNumber()).thenReturn("UNIT123");
            Mockito.when(cartonItem.getProductCode()).thenReturn("PROD456");
            Mockito.when(cartonItem.getProductDescription()).thenReturn("Sample Product");
            Mockito.when(cartonItem.getProductType()).thenReturn("PLASMA");
            Mockito.when(cartonItem.getWeight()).thenReturn(250);
            Mockito.when(cartonItem.getAboRh()).thenReturn("A+");
            Mockito.when(cartonItem.getExpirationDate()).thenReturn(now.plusDays(30));
            Mockito.when(cartonItem.getCollectionDate()).thenReturn(ZonedDateTime.now().minusDays(1));
            Mockito.when(cartonItem.getCreateDate()).thenReturn(ZonedDateTime.now());
            Mockito.when(cartonItem.getModificationDate()).thenReturn(ZonedDateTime.now());


        // When
        CartonItemOutput result = mapper.toOutput(cartonItem);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.unitNumber()).isEqualTo(cartonItem.getUnitNumber());
        assertThat(result.productCode()).isEqualTo(cartonItem.getProductCode());
        assertThat(result.productDescription()).isEqualTo(cartonItem.getProductDescription());
        assertThat(result.productType()).isEqualTo(cartonItem.getProductType());
        assertThat(result.weight()).isEqualTo(cartonItem.getWeight());
        assertThat(result.aboRh()).isEqualTo(cartonItem.getAboRh());
        assertThat(result.expirationDate()).isEqualTo(cartonItem.getExpirationDate());
        assertThat(result.collectionDate()).isEqualTo(cartonItem.getCollectionDate());
        assertThat(result.createDate()).isEqualTo(cartonItem.getCreateDate());
        assertThat(result.modificationDate()).isEqualTo(cartonItem.getModificationDate());
    }

    @Test
    void shouldMapInventoryValidationToCartonItemOutput() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime nowZoned = ZonedDateTime.now();

        Inventory inventory = Mockito.mock(Inventory.class);
            Mockito.when(inventory.getUnitNumber()).thenReturn("UNIT789");
            Mockito.when(inventory.getProductCode()).thenReturn("PROD101");
            Mockito.when(inventory.getProductDescription()).thenReturn("Validated Product");
            Mockito.when(inventory.getProductFamily()).thenReturn("PLASMA");
            Mockito.when(inventory.getWeight()).thenReturn(300);
            Mockito.when(inventory.getAboRh()).thenReturn("B-");
            Mockito.when(inventory.getExpirationDate()).thenReturn(now.plusDays(60));
            Mockito.when(inventory.getCollectionDate()).thenReturn(nowZoned.minusDays(2));
            Mockito.when(inventory.getCreateDate()).thenReturn(nowZoned);
            Mockito.when(inventory.getModificationDate()).thenReturn(nowZoned);



        InventoryValidation inventoryValidation = new InventoryValidation(inventory, Collections.emptyList());

        // When
        CartonItemOutput result = mapper.toOutput(inventoryValidation);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.unitNumber()).isEqualTo(inventory.getUnitNumber());
        assertThat(result.productCode()).isEqualTo(inventory.getProductCode());
        assertThat(result.productDescription()).isEqualTo(inventory.getProductDescription());
        assertThat(result.productType()).isEqualTo(inventory.getProductFamily());
        assertThat(result.weight()).isEqualTo(inventory.getWeight());
        assertThat(result.aboRh()).isEqualTo(inventory.getAboRh());
        assertThat(result.expirationDate()).isEqualTo(inventory.getExpirationDate());
        assertThat(result.collectionDate()).isEqualTo(inventory.getCollectionDate());
        assertThat(result.createDate()).isEqualTo(inventory.getCreateDate());
        assertThat(result.modificationDate()).isEqualTo(inventory.getModificationDate());
    }

    @Test
    void shouldHandleNullCartonItem() {
        // When
        CartonItemOutput result = mapper.toOutput((CartonItem) null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldHandleNullInventoryValidation() {
        // When
        CartonItemOutput result = mapper.toOutput((InventoryValidation) null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldHandleInventoryValidationWithNullInventory() {
        // Given
        InventoryValidation inventoryValidation = Mockito.mock(InventoryValidation.class);

        // When
        CartonItemOutput result = mapper.toOutput(inventoryValidation);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.unitNumber()).isNull();
        assertThat(result.productCode()).isNull();
        assertThat(result.productDescription()).isNull();
        assertThat(result.productType()).isNull();
        assertThat(result.weight()).isNull();
        assertThat(result.aboRh()).isNull();
        assertThat(result.expirationDate()).isNull();
        assertThat(result.collectionDate()).isNull();
        assertThat(result.createDate()).isNull();
        assertThat(result.modificationDate()).isNull();
    }
}
