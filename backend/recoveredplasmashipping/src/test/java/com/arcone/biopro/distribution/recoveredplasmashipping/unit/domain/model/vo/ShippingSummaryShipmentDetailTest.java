package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ProductType;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShippingSummaryShipmentDetail;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingSummaryShipmentDetailTest {

    @Mock
    private RecoveredPlasmaShipment mockShipment;

    @Mock
    private RecoveredPlasmaShipmentCriteriaRepository mockRepository;

    @Mock
    private ProductType mockProductType;

    @Mock
    private Carton mockCarton;

    @Mock
    private CartonItem mockCartonItem;

    private LocalDate shipmentDate;

    void setUp() {
        shipmentDate = LocalDate.now();

        when(mockShipment.getShipmentNumber()).thenReturn("SHP001");
        when(mockShipment.getShipmentDate()).thenReturn(shipmentDate);
        when(mockShipment.getProductType()).thenReturn("TYPE1");
        when(mockShipment.getTransportationReferenceNumber()).thenReturn("TRN001");

        when(mockProductType.getProductTypeDescription()).thenReturn("Product Type 1");
        when(mockRepository.findBYProductType(anyString())).thenReturn(Mono.just(mockProductType));

        when(mockCarton.getTotalProducts()).thenReturn(5);
        when(mockCarton.getProducts()).thenReturn(Collections.singletonList(mockCartonItem));
        when(mockCartonItem.getProductCode()).thenReturn("PC001");
    }

    @Test
    void shouldCreateValidShippingSummaryWithDisplayTransportation() {
        setUp();
        List<Carton> cartons = Collections.singletonList(mockCarton);

        ShippingSummaryShipmentDetail summary = new ShippingSummaryShipmentDetail(
            mockShipment,
            cartons,
            "Y",
            mockRepository
        );

        assertEquals("SHP001", summary.getShipmentNumber());
        assertEquals(shipmentDate, summary.getShipmentDate());
        assertEquals("Product Type 1", summary.getProductType());
        assertEquals("PC001", summary.getProductCode());
        assertEquals(1, summary.getTotalNumberOfCartons());
        assertEquals(5, summary.getTotalNumberOfProducts());
        assertEquals("TRN001", summary.getTransportationReferenceNumber());
        assertTrue(summary.isDisplayTransportationNumber());
    }

    @Test
    void shouldCreateValidShippingSummaryWithoutDisplayTransportation() {
        setUp();
        List<Carton> cartons = Collections.singletonList(mockCarton);

        ShippingSummaryShipmentDetail summary = new ShippingSummaryShipmentDetail(
            mockShipment,
            cartons,
            "N",
            mockRepository
        );

        assertFalse(summary.isDisplayTransportationNumber());
    }

    @Test
    void shouldHandleEmptyCartonList() {
        shipmentDate = LocalDate.now();

        when(mockShipment.getShipmentNumber()).thenReturn("SHP001");
        when(mockShipment.getShipmentDate()).thenReturn(shipmentDate);
        when(mockShipment.getProductType()).thenReturn("TYPE1");

        when(mockProductType.getProductTypeDescription()).thenReturn("Product Type 1");
        when(mockRepository.findBYProductType(anyString())).thenReturn(Mono.just(mockProductType));

        ShippingSummaryShipmentDetail summary = new ShippingSummaryShipmentDetail(
            mockShipment,
            Collections.emptyList(),
            "N",
            mockRepository
        );

        assertEquals(0, summary.getTotalNumberOfCartons());
        assertEquals(0, summary.getTotalNumberOfProducts());
        assertEquals("", summary.getProductCode());
    }

    @Test
    void shouldHandleMultipleProductCodes() {
        setUp();
        when(mockCartonItem.getProductCode()).thenReturn("PC001");
        CartonItem mockCartonItem2 = mock(CartonItem.class);
        when(mockCartonItem2.getProductCode()).thenReturn("PC002");

        when(mockCarton.getProducts()).thenReturn(Arrays.asList(mockCartonItem, mockCartonItem2));
        List<Carton> cartons = Collections.singletonList(mockCarton);

        ShippingSummaryShipmentDetail summary = new ShippingSummaryShipmentDetail(
            mockShipment,
            cartons,
            "N",
            mockRepository
        );

        assertEquals("PC001, PC002", summary.getProductCode());
    }

    @Test
    void shouldThrowExceptionWhenShipmentIsNull() {

        assertThrows(IllegalArgumentException.class, () ->
            new ShippingSummaryShipmentDetail(
                null,
                Collections.emptyList(),
                "N",
                null
            )
        );
    }

    @Test
    void shouldThrowExceptionWhenProductTypeNotFound() {
        shipmentDate = LocalDate.now();

        when(mockShipment.getShipmentNumber()).thenReturn("SHP001");
        when(mockShipment.getShipmentDate()).thenReturn(shipmentDate);
        when(mockShipment.getProductType()).thenReturn("TYPE1");

        when(mockRepository.findBYProductType(anyString())).thenReturn(Mono.empty());
        assertThrows(IllegalArgumentException.class, () ->
            new ShippingSummaryShipmentDetail(
                mockShipment,
                Collections.emptyList(),
                "N",
                mockRepository
            )
        );
    }
}

