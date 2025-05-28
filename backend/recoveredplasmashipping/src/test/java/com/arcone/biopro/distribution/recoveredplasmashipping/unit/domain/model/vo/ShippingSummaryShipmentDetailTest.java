package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ProductType;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShippingSummaryCartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShippingSummaryShipmentDetail;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
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
    private ShippingSummaryCartonItem mockCartonItem;

    private LocalDate shipmentDate;

    void setUp() {
        shipmentDate = LocalDate.now();

        when(mockShipment.getShipmentNumber()).thenReturn("SHP001");
        when(mockShipment.getProductType()).thenReturn("TYPE1");
        when(mockShipment.getTransportationReferenceNumber()).thenReturn("TRN001");

        when(mockProductType.getProductTypeDescription()).thenReturn("Product Type 1");
        when(mockRepository.findBYProductType(anyString())).thenReturn(Mono.just(mockProductType));

        when(mockCartonItem.getTotalProducts()).thenReturn(5);
        when(mockCartonItem.getProductCode()).thenReturn("PC001");
    }

    @Test
    void shouldCreateValidShippingSummaryWithDisplayTransportation() {
        setUp();
        List<ShippingSummaryCartonItem> cartons = Collections.singletonList(mockCartonItem);

        ShippingSummaryShipmentDetail summary = new ShippingSummaryShipmentDetail(
            mockShipment,
            cartons,
            "Y",
            mockRepository
        );

        assertEquals("SHP001", summary.getShipmentNumber());
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
        List<ShippingSummaryCartonItem> cartons = Collections.singletonList(mockCartonItem);

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


        when(mockShipment.getShipmentNumber()).thenReturn("SHP001");
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
        ShippingSummaryCartonItem mockCartonItem2 = mock(ShippingSummaryCartonItem.class);
        when(mockCartonItem2.getProductCode()).thenReturn("PC002");

        List<ShippingSummaryCartonItem> cartons = List.of(mockCartonItem,mockCartonItem2);

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

        when(mockShipment.getShipmentNumber()).thenReturn("SHP001");
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

    @Test
    void shouldHandleSingleProductCode() {
        setUp();
        when(mockCartonItem.getProductCode()).thenReturn("PC001");
        List<ShippingSummaryCartonItem> cartons = List.of(mockCartonItem);

        ShippingSummaryShipmentDetail summary = new ShippingSummaryShipmentDetail(
            mockShipment,
            cartons,
            "N",
            mockRepository
        );

        assertEquals("PC001", summary.getProductCode());
    }
}

