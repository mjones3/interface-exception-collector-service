package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShipmentCustomer;
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

        RecoveredPlasmaShipment recoveredPlasmaShipment = createTestModel();

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

    private RecoveredPlasmaShipment createTestModel() {
        RecoveredPlasmaShipment model = Mockito.mock(RecoveredPlasmaShipment.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(model.getLocationCode()).thenReturn("LOC123");
        Mockito.when(model.getProductType()).thenReturn("PLASMA");
        Mockito.when(model.getShipmentNumber()).thenReturn("SHP001");
        Mockito.when(model.getStatus()).thenReturn("ACTIVE");
        Mockito.when(model.getCreateEmployeeId()).thenReturn("EMP123");
        Mockito.when(model.getTransportationReferenceNumber()).thenReturn("TRN123");
        Mockito.when(model.getScheduleDate()).thenReturn(LocalDate.now());
        Mockito.when(model.getShipmentDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(model.getCartonTareWeight()).thenReturn(new BigDecimal(10.5));

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
}

