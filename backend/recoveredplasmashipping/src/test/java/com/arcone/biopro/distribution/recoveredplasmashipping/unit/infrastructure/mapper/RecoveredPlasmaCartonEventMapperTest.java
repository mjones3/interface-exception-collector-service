package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonItemPackedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonPackedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaCartonEventMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecoveredPlasmaCartonEventMapperTest {

    private RecoveredPlasmaCartonEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(RecoveredPlasmaCartonEventMapper.class);
    }

    @Test
    @DisplayName("Should map Carton to RecoveredPlasmaCartonPackedOutputDTO successfully")
    void shouldMapCartonToPackedEventDTO() {
        // Given
        Carton carton = createSampleCarton();

        // When
        RecoveredPlasmaCartonPackedOutputDTO result = mapper.modelToPackedEventDTO(carton,"LOCATION_CODE");

        // Then
        assertNotNull(result);
        assertEquals(carton.getStatus(), result.status());
        assertEquals(carton.getCloseEmployeeId(), result.closeEmployeeId());
        assertEquals(carton.getCloseDate(), result.closeDate());
        assertEquals(carton.getCartonNumber(), result.cartonNumber());
        assertEquals(carton.getCartonSequence(), result.cartonSequence());
        assertEquals(carton.getTotalProducts(), result.totalProducts());
        assertEquals(carton.getTotalWeight(), result.totalWeight());
        assertEquals(carton.getTotalVolume(), result.totalVolume());
        assertEquals("PRODUCT_TYPE", result.productType());
        assertEquals("LOCATION_CODE", result.locationCode());
        assertEquals(carton.getTotalProducts(), result.packedProducts().size());

    }

    @Test
    @DisplayName("Should return null when mapping null Carton")
    void shouldReturnNullWhenMappingNullCarton() {
        // When
        RecoveredPlasmaCartonPackedOutputDTO result = mapper.modelToPackedEventDTO(null,null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should map CartonItem to RecoveredPlasmaCartonItemPackedOutputDTO successfully")
    void shouldMapCartonItemToItemPackedEventDTO() {
        // Given
        CartonItem cartonItem = createSampleCartonItem();

        // When


        RecoveredPlasmaCartonItemPackedOutputDTO result = mapper.modelToItemPackedEventDTO(cartonItem);

        // Then
        assertNotNull(result);
        assertEquals(cartonItem.getUnitNumber(), result.unitNumber());
        assertEquals(cartonItem.getProductCode(), result.productCode());
        assertEquals(cartonItem.getPackedByEmployeeId(), result.packedByEmployeeId());
        assertEquals(cartonItem.getStatus(), result.status());
        assertEquals(cartonItem.getCreateDate(), result.packedDate());

    }

    @Test
    @DisplayName("Should return null when mapping null CartonItem")
    void shouldReturnNullWhenMappingNullCartonItem() {
        // When
        RecoveredPlasmaCartonItemPackedOutputDTO result = mapper.modelToItemPackedEventDTO((CartonItem) null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should map List of CartonItems to List of RecoveredPlasmaCartonItemPackedOutputDTO successfully")
    void shouldMapCartonItemListToItemPackedEventDTOList() {
        // Given
        List<CartonItem> cartonItems = Arrays.asList(
            createSampleCartonItem(),
            createSampleCartonItem()
        );

        // When
        List<RecoveredPlasmaCartonItemPackedOutputDTO> result = mapper.modelToItemPackedEventDTO(cartonItems);

        // Then
        assertNotNull(result);
        assertEquals(cartonItems.size(), result.size());

        for (int i = 0; i < cartonItems.size(); i++) {
            CartonItem cartonItem = cartonItems.get(i);
            RecoveredPlasmaCartonItemPackedOutputDTO dto = result.get(i);

            assertEquals(cartonItem.getUnitNumber(), dto.unitNumber());
            assertEquals(cartonItem.getProductCode(), dto.productCode());
            assertEquals(cartonItem.getPackedByEmployeeId(), dto.packedByEmployeeId());
            assertEquals(cartonItem.getStatus(), dto.status());
            assertEquals(cartonItem.getCreateDate(), dto.packedDate());
        }
    }

    @Test
    @DisplayName("Should return empty list when mapping null List of CartonItems")
    void shouldReturnEmptyListWhenMappingNullCartonItemList() {
        // When
        List<RecoveredPlasmaCartonItemPackedOutputDTO> result = mapper.modelToItemPackedEventDTO((List<CartonItem>) null);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("Should return empty list when mapping empty List of CartonItems")
    void shouldReturnEmptyListWhenMappingEmptyCartonItemList() {
        // When
        List<RecoveredPlasmaCartonItemPackedOutputDTO> result = mapper.modelToItemPackedEventDTO(List.of());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private Carton createSampleCarton() {

        List<CartonItem> cartonItems = Arrays.asList(
            createSampleCartonItem(),
            createSampleCartonItem()
        );

        return Carton.fromRepository(1L,"number",1L,1,"employee-id","close-employee-id"
            , ZonedDateTime.now(),ZonedDateTime.now(),ZonedDateTime.now(),"PACKED", BigDecimal.ZERO,BigDecimal.ZERO, cartonItems,2 ,2 );


    }

    private CartonItem createSampleCartonItem() {
        return CartonItem.fromRepository(1L, 1L, "UNIT_NUMBER", "PRODUCT_CODE", "DESCRIPTION",
            "PRODUCT_TYPE", 160, 10, "EMPLOYEE_ID", "AP", "PACKED",
            LocalDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now() , "verify-employee", ZonedDateTime.now());
    }
}
