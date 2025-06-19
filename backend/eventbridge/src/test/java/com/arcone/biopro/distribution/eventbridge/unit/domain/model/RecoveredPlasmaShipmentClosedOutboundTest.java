package com.arcone.biopro.distribution.eventbridge.unit.domain.model;

import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedCartonItemOutbound;
import com.arcone.biopro.distribution.eventbridge.domain.model.RecoveredPlasmaShipmentClosedOutbound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecoveredPlasmaShipmentClosedOutboundTest {

    @Mock
    private RecoveredPlasmaShipmentClosedCartonItemOutbound mockCartonItem;

    private LocalDate validShipmentDate;
    private ZonedDateTime validCloseDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validShipmentDate = LocalDate.now();
        validCloseDate = ZonedDateTime.now();
    }

    private RecoveredPlasmaShipmentClosedOutbound createValidOutbound() {
        return new RecoveredPlasmaShipmentClosedOutbound(
            "SHP123",
            "LSC456",
            "LCC789",
            "CUST001",
            validShipmentDate,
            validCloseDate,
            "SLC001"
        );
    }

    @Test
    @DisplayName("Should create valid object with all required fields")
    void shouldCreateValidObject() {
        // When
        RecoveredPlasmaShipmentClosedOutbound outbound = createValidOutbound();

        // Then
        assertNotNull(outbound);
        assertEquals("ARC", outbound.getBloodCenterName());
        assertEquals(validShipmentDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            outbound.getShipmentDateFormatted());
        assertEquals(validCloseDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            outbound.getCloseDateFormatted());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when shipmentNumber is null")
    void shouldThrowExceptionWhenShipmentNumberIsNull() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new RecoveredPlasmaShipmentClosedOutbound(
                null,
                "LSC456",
                "LCC789",
                "CUST001",
                validShipmentDate,
                validCloseDate,
                "SLC001"
            );
        });
        assertEquals("Shipment number is null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when locationShipmentCode is null")
    void shouldThrowExceptionWhenLocationShipmentCodeIsNull() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new RecoveredPlasmaShipmentClosedOutbound(
                "SHP123",
                null,
                "LCC789",
                "CUST001",
                validShipmentDate,
                validCloseDate,
                "SLC001"
            );
        });
        assertEquals("Location shipment code is null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when locationCartonCode is null")
    void shouldThrowExceptionWhenLocationCartonCodeIsNull() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new RecoveredPlasmaShipmentClosedOutbound(
                "SHP123",
                "LSC456",
                null,
                "CUST001",
                validShipmentDate,
                validCloseDate,
                "SLC001"
            );
        });
        assertEquals("Location carton code is null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when customerCode is null")
    void shouldThrowExceptionWhenCustomerCodeIsNull() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new RecoveredPlasmaShipmentClosedOutbound(
                "SHP123",
                "LSC456",
                "LCC789",
                null,
                validShipmentDate,
                validCloseDate,
                "SLC001"
            );
        });
        assertEquals("Customer code is null", exception.getMessage());
    }

    @Test
    @DisplayName("Should correctly add carton and calculate total products")
    void shouldAddCartonAndCalculateTotalProducts() {
        // Given
        RecoveredPlasmaShipmentClosedOutbound outbound = createValidOutbound();
        List<RecoveredPlasmaShipmentClosedCartonItemOutbound> packedProducts =
            new ArrayList<>();
        packedProducts.add(mockCartonItem);

        // When
        outbound.addCarton("CARTON1", 5, packedProducts);
        outbound.addCarton("CARTON2", 3, packedProducts);

        // Then
        assertEquals(8, outbound.getTotalShipmentProducts());
    }

    @Test
    @DisplayName("Should return 0 when no cartons are added")
    void shouldReturnZeroWhenNoCartonsAdded() {
        // Given
        RecoveredPlasmaShipmentClosedOutbound outbound = createValidOutbound();

        // When
        int totalProducts = outbound.getTotalShipmentProducts();

        // Then
        assertEquals(0, totalProducts);
    }

    @Test
    @DisplayName("Should handle blank strings as invalid input")
    void shouldHandleBlankStringsAsInvalid() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            new RecoveredPlasmaShipmentClosedOutbound(
                "  ",
                "LSC456",
                "LCC789",
                "CUST001",
                validShipmentDate,
                validCloseDate,
                "SLC001"
            );
        });
        assertEquals("Shipment number is null", exception.getMessage());
    }

    @Test
    @DisplayName("Should format dates correctly when closeDate is null")
    void shouldFormatDatesCorrectlyWhenCloseDateIsNull() {
        // Given & When
        RecoveredPlasmaShipmentClosedOutbound outbound = new RecoveredPlasmaShipmentClosedOutbound(
            "SHP123",
            "LSC456",
            "LCC789",
            "CUST001",
            validShipmentDate,
            null,
            "SLC001"
        );

        // Then
        assertNotNull(outbound.getShipmentDateFormatted());
        assertNull(outbound.getCloseDateFormatted());
    }
}

