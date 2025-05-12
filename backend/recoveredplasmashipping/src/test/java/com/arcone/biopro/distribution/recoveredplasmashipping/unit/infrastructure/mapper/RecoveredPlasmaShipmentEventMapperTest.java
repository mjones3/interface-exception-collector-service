package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShipmentCustomer;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaShipmentClosedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaShipmentCreatedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentEventMapper;
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
class RecoveredPlasmaShipmentEventMapperTest {


    private RecoveredPlasmaShipmentEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(RecoveredPlasmaShipmentEventMapper.class);
    }

    @Test
    void modelToEventDTO_ShouldMapAllFields_WhenGivenValidInput() {
        // Given

        RecoveredPlasmaShipment recoveredPlasmaShipment = createTestModel(false);

        // When
        RecoveredPlasmaShipmentCreatedOutputDTO result = mapper.modelToEventDTO(recoveredPlasmaShipment);

        // Then
        assertNotNull(result);
        assertEquals("CUST123", result.customerCode());
        assertEquals("Test Customer", result.customerName());
        assertEquals("TX", result.customerState());
        assertEquals("12345", result.customerPostalCode());
        assertEquals("United States", result.customerCountry());
        assertEquals("US",result.customerCountryCode());
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
        assertEquals("EMP123", result.createEmployeeId());
        assertEquals("TRN123", result.transportationReferenceNumber());
        assertEquals("ACTIVE", result.status());


    }

    @Test
    void modelToClosedEventDTO_ShouldMapAllFields_WhenGivenValidInput() {
        // Given

        RecoveredPlasmaShipment recoveredPlasmaShipment = createTestModel(true);

        // When
        RecoveredPlasmaShipmentClosedOutputDTO result = mapper.modelToCloseEventDTO(recoveredPlasmaShipment);

        // Then
        assertNotNull(result);
        assertEquals("CUST123", result.customerCode());
        assertEquals("Test Customer", result.customerName());
        assertEquals("TX", result.customerState());
        assertEquals("12345", result.customerPostalCode());
        assertEquals("United States", result.customerCountry());
        assertEquals("US",result.customerCountryCode());
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
        assertEquals("EMP123", result.createEmployeeId());
        assertEquals("TRN123", result.transportationReferenceNumber());
        assertEquals("ACTIVE", result.status());
        assertEquals(10, result.totalCartons());
        assertEquals("close-emp-id", result.closedEmployeeId());
        assertNotNull(result.closeDate());

        assertEquals(1, result.cartonList().size());
        assertEquals("CARTON_NUMBER", result.cartonList().getFirst().cartonNumber());
        assertEquals(1, result.cartonList().getFirst().cartonSequence());
        assertEquals("close-emp-id", result.cartonList().getFirst().closeEmployeeId());
        assertNotNull(result.cartonList().getFirst().closeDate());
        assertEquals(10, result.cartonList().getFirst().totalProducts());
        assertEquals(BigDecimal.ONE, result.cartonList().getFirst().totalVolume());
        assertEquals(BigDecimal.TEN, result.cartonList().getFirst().totalWeight());
        assertEquals("CLOSED", result.cartonList().getFirst().status());
        assertNotNull(result.closeDate());


    }


    @Test
    void modelToEventDTO_ShouldHandleNullValues() {
        // Given
        RecoveredPlasmaShipment recoveredPlasmaShipment =  Mockito.mock(RecoveredPlasmaShipment.class);

        // When
        RecoveredPlasmaShipmentCreatedOutputDTO result = mapper.modelToEventDTO(recoveredPlasmaShipment);

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

    private RecoveredPlasmaShipment createTestModel(boolean includeCarton) {
        RecoveredPlasmaShipment model = Mockito.mock(RecoveredPlasmaShipment.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(model.getLocationCode()).thenReturn("LOC123");
        Mockito.when(model.getProductType()).thenReturn("PLASMA");
        Mockito.when(model.getShipmentNumber()).thenReturn("SHP001");
        Mockito.when(model.getStatus()).thenReturn("ACTIVE");
        Mockito.when(model.getCreateEmployeeId()).thenReturn("EMP123");
        Mockito.when(model.getTransportationReferenceNumber()).thenReturn("TRN123");
        Mockito.when(model.getShipmentDate()).thenReturn(LocalDate.now());
        Mockito.when(model.getCartonTareWeight()).thenReturn(new BigDecimal(10.5));

        if(includeCarton){
            Mockito.when(model.getCloseEmployeeId()).thenReturn("close-emp-id");
            Mockito.when(model.getTotalCartons()).thenReturn(10);
            var listCarton = createCartonClosedModel();

            Mockito.when(model.getCartonList()).thenReturn(listCarton);
        }

        ShipmentCustomer customer = Mockito.mock(ShipmentCustomer.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(customer.getCustomerCode()).thenReturn("CUST123");
        Mockito.when(customer.getCustomerName()).thenReturn("Test Customer");
        Mockito.when(customer.getCustomerState()).thenReturn("TX");
        Mockito.when(customer.getCustomerPostalCode()).thenReturn("12345");
        Mockito.when(customer.getCustomerCountry()).thenReturn("United States");
        Mockito.when(customer.getCustomerCountryCode()).thenReturn("US");
        Mockito.when(customer.getCustomerCity()).thenReturn("Test City");
        Mockito.when(customer.getCustomerDistrict()).thenReturn("Test District");
        Mockito.when(customer.getCustomerAddressLine1()).thenReturn("123 Test St");
        Mockito.when(customer.getCustomerAddressLine2()).thenReturn("Suite 100");
        Mockito.when(customer.getCustomerAddressContactName()).thenReturn("John Doe");
        Mockito.when(customer.getCustomerAddressPhoneNumber()).thenReturn("123-456-7890");
        Mockito.when(customer.getCustomerAddressDepartmentName()).thenReturn("Test Department");
        Mockito.when(model.getShipmentCustomer()).thenReturn(customer);



        return model;
    }

    private List<Carton> createCartonClosedModel(){
        Carton cartonModel = Mockito.mock(Carton.class,Mockito.RETURNS_DEEP_STUBS);

       /*
        String productType,
        ,*/

        Mockito.when(cartonModel.getCartonNumber()).thenReturn("CARTON_NUMBER");
        Mockito.when(cartonModel.getCartonSequence()).thenReturn(1);
        Mockito.when(cartonModel.getCloseEmployeeId()).thenReturn("close-emp-id");
        Mockito.when(cartonModel.getCloseDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(cartonModel.getTotalProducts()).thenReturn(10);
        Mockito.when(cartonModel.getTotalVolume()).thenReturn(BigDecimal.ONE);
        Mockito.when(cartonModel.getTotalWeight()).thenReturn(BigDecimal.TEN);
        Mockito.when(cartonModel.getStatus()).thenReturn("CLOSED");
        return  List.of(cartonModel);
    }
}

