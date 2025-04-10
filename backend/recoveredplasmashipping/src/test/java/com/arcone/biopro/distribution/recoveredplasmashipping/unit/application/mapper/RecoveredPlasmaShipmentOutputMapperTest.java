package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShipmentCustomer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;




class RecoveredPlasmaShipmentOutputMapperTest {


    private RecoveredPlasmaShipmentOutputMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(RecoveredPlasmaShipmentOutputMapper.class);
    }

    @Test
    void shouldMapRecoveredPlasmaShipmentToOutput() {
        // Given
        ShipmentCustomer shipmentCustomer = ShipmentCustomer.fromShipmentDetails(
            "CODE123",
            "Customer Name",
            "CA",
            "12345",
            "United States",
            "US",
            "San Francisco",
            "District",
            "123 Main St",
            "Suite 100",
            "John Doe",
            "123-456-7890",
            "Shipping Dept");

        RecoveredPlasmaShipment shipment = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(shipment.getStatus()).thenReturn("OPEN");
        Mockito.when(shipment.isCanAddCartons()).thenReturn(true);
        Mockito.when(shipment.getShipmentCustomer()).thenReturn(shipmentCustomer);

        // When
        RecoveredPlasmaShipmentOutput output = mapper.toRecoveredPlasmaShipmentOutput(shipment);

        // Then
        assertNotNull(output);
        assertEquals("OPEN", output.status());
        assertTrue(output.canAddCartons());
        assertEquals("CODE123", output.customerCode());
        assertEquals("Customer Name", output.customerName());
        assertEquals("CA", output.customerState());
        assertEquals("12345", output.customerPostalCode());
        assertEquals("United States", output.customerCountry());
        assertEquals("US", output.customerCountryCode());
        assertEquals("San Francisco", output.customerCity());
        assertEquals("District", output.customerDistrict());
        assertEquals("123 Main St", output.customerAddressLine1());
        assertEquals("Suite 100", output.customerAddressLine2());
        assertEquals("John Doe", output.customerAddressContactName());
        assertEquals("123-456-7890", output.customerAddressPhoneNumber());
        assertEquals("Shipping Dept", output.customerAddressDepartmentName());
    }

}

