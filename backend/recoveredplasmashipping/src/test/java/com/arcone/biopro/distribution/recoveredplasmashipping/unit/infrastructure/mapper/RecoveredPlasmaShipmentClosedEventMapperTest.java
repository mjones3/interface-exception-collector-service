package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonClosedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaShipmentClosedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentClosedEventMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonItemEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class RecoveredPlasmaShipmentClosedEventMapperTest {

    private RecoveredPlasmaShipmentClosedEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(RecoveredPlasmaShipmentClosedEventMapper.class);
    }


    @Test
    void modelToClosedEventDTO_ShouldMapAllFields_WhenGivenValidInput() {
        // Given

        RecoveredPlasmaShipmentEntity recoveredPlasmaShipment = createShipmentTestEntity();
        var cartonList = List.of(createCartonClosedOutputDto());

        // When
        RecoveredPlasmaShipmentClosedOutputDTO result = mapper.entityToCloseEventDTO(recoveredPlasmaShipment, cartonList);

        // Then
        assertNotNull(result);
        assertEquals("CUST123", result.customerCode());
        assertEquals("Test Customer", result.customerName());
        assertEquals("TX", result.customerState());
        assertEquals("12345", result.customerPostalCode());
        assertEquals("United States", result.customerCountry());
        assertEquals("US", result.customerCountryCode());
        assertEquals("Test City", result.customerCity());
        assertEquals("Test District", result.customerDistrict());
        assertEquals("123 Test St", result.customerAddressLine1());
        assertEquals("Suite 100", result.customerAddressLine2());
        assertEquals("John Doe", result.customerAddressContactName());
        assertEquals("123-456-7890", result.customerAddressPhoneNumber());
        assertEquals("Test Department", result.customerAddressDepartmentName());
        assertEquals(LocalDate.now(), result.shipmentDate());
        assertEquals(new BigDecimal(10.5), result.cartonTareWeight());
        assertEquals("LOC123", result.locationCode());
        assertEquals("PLASMA", result.productType());
        assertEquals("SHP001", result.shipmentNumber());
        assertEquals("TRN123", result.transportationReferenceNumber());
        assertEquals("CLOSED", result.status());
        assertEquals(1, result.totalCartons());
        assertEquals("closed-emp-id", result.closedEmployeeId());
        assertNotNull(result.closeDate());

        // CARTON DETAILS
        assertEquals(1, result.cartonList().size());
        assertEquals("CARTON_NUMBER", result.cartonList().getFirst().cartonNumber());
        assertEquals(2, result.cartonList().getFirst().cartonSequence());
        assertEquals("close-emp-id", result.cartonList().getFirst().closeEmployeeId());
        assertNotNull(result.cartonList().getFirst().closeDate());
        assertEquals(10, result.cartonList().getFirst().totalProducts());
        assertEquals("CLOSED", result.cartonList().getFirst().status());
        assertEquals("LOC123", result.cartonList().getFirst().locationCode());
        assertEquals("PLASMA", result.cartonList().getFirst().productType());

    }


    @Test
    void entityToCartonClosedEventDto() {

        var carton = createCartonClosedModel();
        var itemList = createCartonItemEntityList();

        RecoveredPlasmaShipment shipment = Mockito.mock(RecoveredPlasmaShipment.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(shipment.getLocationCode()).thenReturn("LOC123");
        Mockito.when(shipment.getProductType()).thenReturn("PLASMA");

        var result = mapper.cartonModelToEventDTO(carton, shipment, itemList);

        // CARTON DETAILS
        assertNotNull(result);
        assertEquals("CARTON_NUMBER", result.cartonNumber());
        assertEquals(2, result.cartonSequence());
        assertEquals("close-emp-id", result.closeEmployeeId());
        assertNotNull(result.closeDate());
        assertEquals(1, result.totalProducts());
        assertEquals("CLOSED", result.status());
        assertEquals("LOC123", result.locationCode());
        assertEquals("PLASMA", result.productType());

        // Carton Item Details

        assertEquals(1, result.packedProducts().size());
        var cartonResult = result.packedProducts().getFirst();

        assertNotNull(cartonResult);
        assertEquals("PRODUCT_CODE", cartonResult.productCode());
        assertEquals("UNIT_NUMBER", cartonResult.unitNumber());
        assertEquals("PRODUCT_TYPE", cartonResult.productType());
        assertEquals("ABPN", cartonResult.aboRh());
        assertEquals(10, cartonResult.volume());
        assertEquals(15, cartonResult.weight());
        assertNotNull(cartonResult.donationDate());

    }


    @Test
    void modelToEventDTO_ShouldHandleNullValues() {
        // Given
        RecoveredPlasmaShipmentEntity recoveredPlasmaShipment =  Mockito.mock(RecoveredPlasmaShipmentEntity.class);
        var list = List.of(Mockito.mock(RecoveredPlasmaCartonClosedOutputDTO.class));

        // When
        RecoveredPlasmaShipmentClosedOutputDTO result = mapper.entityToCloseEventDTO(recoveredPlasmaShipment,list);

        // Then
        assertNotNull(result);
        assertNull(result.customerCode());
        assertNull(result.customerName());
        assertNull(result.customerState());
        assertNull(result.customerPostalCode());
        assertNull(result.customerCountry());
        assertNull(result.customerCountryCode());
        assertNull(result.customerCity());
        assertNull(result.customerDistrict());
        assertNull(result.customerAddressLine1());
        assertNull(result.customerAddressLine2());
        assertNull(result.customerAddressContactName());
        assertNull(result.customerAddressPhoneNumber());
        assertNull(result.customerAddressDepartmentName());
    }

    private RecoveredPlasmaShipmentEntity createShipmentTestEntity() {

        return RecoveredPlasmaShipmentEntity
            .builder()
            .locationCode("LOC123")
            .productType("PLASMA")
            .shipmentDate(LocalDate.now())
            .shipmentNumber("SHP001")
            .status("CLOSED")
            .cartonTareWeight(new BigDecimal(10.5))
            .transportationReferenceNumber("TRN123")
            .closeEmployeeId("closed-emp-id")
            .customerCode("CUST123")
            .customerName("Test Customer")
            .customerState("TX")
            .customerPostalCode("12345")
            .customerCountry("United States")
            .customerCountryCode("US")
            .customerCity("Test City")
            .customerDistrict("Test District")
            .customerAddressLine1("123 Test St")
            .customerAddressLine2("Suite 100")
            .customerAddressContactName("John Doe")
            .customerAddressPhoneNumber("123-456-7890")
            .customerAddressDepartmentName("Test Department")
            .closeDate(ZonedDateTime.now())
            .build();
    }

    private CartonEntity createCartonClosedModel() {
        return CartonEntity
            .builder()
            .id(1L)
            .shipmentId(1L)
            .cartonSequenceNumber(2)
            .cartonNumber("CARTON_NUMBER")
            .closeEmployeeId("close-emp-id")
            .closeDate(ZonedDateTime.now())
            .status("CLOSED")
            .build();
    }

    private RecoveredPlasmaCartonClosedOutputDTO createCartonClosedOutputDto() {

        return RecoveredPlasmaCartonClosedOutputDTO
            .builder()
            .cartonNumber("CARTON_NUMBER")
            .locationCode("LOC123")
            .productType("PLASMA")
            .cartonSequence(2)
            .closeDate(ZonedDateTime.now())
            .closeEmployeeId("close-emp-id")
            .status("CLOSED")
            .totalProducts(10)
            .build();
    }

    private List<CartonItemEntity> createCartonItemEntityList() {

        return List.of(CartonItemEntity
            .builder()
            .unitNumber("UNIT_NUMBER")
            .productType("PRODUCT_TYPE")
            .productCode("PRODUCT_CODE")
            .aboRh("ABPN")
            .volume(10)
            .weight(15)
            .collectionDate(ZonedDateTime.now())
            .build());
    }

}
