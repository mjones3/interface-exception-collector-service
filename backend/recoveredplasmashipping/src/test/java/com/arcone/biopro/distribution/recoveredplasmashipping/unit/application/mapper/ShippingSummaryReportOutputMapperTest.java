package com.arcone.biopro.distribution.recoveredplasmashipping.unit.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ShippingSummaryCartonItemOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ShippingSummaryReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.ShippingSummaryReportOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.ShippingSummaryReport;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShipTo;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShippingSummaryCartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShippingSummaryShipFrom;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.ShippingSummaryShipmentDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

class ShippingSummaryReportOutputMapperTest {


    private ShippingSummaryReportOutputMapper mapper;
    private ShippingSummaryReport shippingSummaryReport;
    private ShippingSummaryShipmentDetail shipmentDetail;
    private ShipTo shipTo;
    private ShippingSummaryShipFrom shipFrom;



    @BeforeEach
    void setUp() {


        mapper = Mappers.getMapper(ShippingSummaryReportOutputMapper.class);

        // Initialize shipment detail
        shipmentDetail = Mockito.mock(ShippingSummaryShipmentDetail.class);
        Mockito.when(shipmentDetail.getShipmentNumber()).thenReturn("SHP123");
        Mockito.when(shipmentDetail.getProductType()).thenReturn("PLASMA");
        Mockito.when(shipmentDetail.getProductCode()).thenReturn("PRD456");
        Mockito.when(shipmentDetail.getTotalNumberOfCartons()).thenReturn(10);
        Mockito.when(shipmentDetail.getTotalNumberOfProducts()).thenReturn(100);
        Mockito.when(shipmentDetail.getTransportationReferenceNumber()).thenReturn("TRN789");
        Mockito.when(shipmentDetail.isDisplayTransportationNumber()).thenReturn(true);

        // Initialize shipTo
        shipTo = Mockito.mock(ShipTo.class);
        Mockito.when(shipTo.getFormattedAddress()).thenReturn("123 Receiver St, City, State");
        Mockito.when(shipTo.getCustomerName()).thenReturn("Receiver Name");

        // Initialize shipFrom
        shipFrom = Mockito.mock(ShippingSummaryShipFrom.class);
        Mockito.when(shipFrom.getBloodCenterName()).thenReturn("Blood Center ABC");
        Mockito.when(shipFrom.getLocationAddress()).thenReturn("456 Sender St, City, State");

        // Initialize shipping summary report
        shippingSummaryReport = Mockito.mock(ShippingSummaryReport.class);
        Mockito.when(shippingSummaryReport.getShipmentDetail()).thenReturn(shipmentDetail);
        Mockito.when(shippingSummaryReport.getShipTo()).thenReturn(shipTo);
        Mockito.when(shippingSummaryReport.getShipFrom()).thenReturn(shipFrom);
    }

    @Test
    @DisplayName("Should map ShippingSummaryReport to ShippingSummaryReportOutput correctly")
    void shouldMapShippingSummaryReportToOutputCorrectly() {
        // Arrange
        ShippingSummaryReportOutput expectedOutput = ShippingSummaryReportOutput.builder()
        .shipmentDetailShipmentNumber("SHP123")
        .shipmentDetailProductType("PLASMA")
        .shipmentDetailProductCode("PRD456")
        .shipmentDetailTotalNumberOfCartons(10)
        .shipmentDetailTotalNumberOfProducts(100)
        .shipmentDetailTransportationReferenceNumber("TRN789")
        .shipmentDetailDisplayTransportationNumber(true)
        .shipToAddress("123 Receiver St, City, State")
        .shipToCustomerName("Receiver Name")
        .shipFromBloodCenterName("Blood Center ABC")
        .shipFromLocationAddress("456 Sender St, City, State").build();

        // Act
        ShippingSummaryReportOutput actualOutput = mapper.toOutput(shippingSummaryReport);

        // Assert
        assertNotNull(actualOutput);
        assertEquals(expectedOutput.shipmentDetailShipmentNumber(), actualOutput.shipmentDetailShipmentNumber());
        assertEquals(expectedOutput.shipmentDetailProductType(), actualOutput.shipmentDetailProductType());
        assertEquals(expectedOutput.shipmentDetailProductCode(), actualOutput.shipmentDetailProductCode());
        assertEquals(expectedOutput.shipmentDetailTotalNumberOfCartons(), actualOutput.shipmentDetailTotalNumberOfCartons());
        assertEquals(expectedOutput.shipmentDetailTotalNumberOfProducts(), actualOutput.shipmentDetailTotalNumberOfProducts());
        assertEquals(expectedOutput.shipmentDetailTransportationReferenceNumber(), actualOutput.shipmentDetailTransportationReferenceNumber());
        assertEquals(expectedOutput.shipmentDetailDisplayTransportationNumber(), actualOutput.shipmentDetailDisplayTransportationNumber());
        assertEquals(expectedOutput.shipToAddress(), actualOutput.shipToAddress());
        assertEquals(expectedOutput.shipToCustomerName(), actualOutput.shipToCustomerName());
        assertEquals(expectedOutput.shipFromBloodCenterName(), actualOutput.shipFromBloodCenterName());
        assertEquals(expectedOutput.shipFromLocationAddress(), actualOutput.shipFromLocationAddress());
    }

    @Test
    @DisplayName("Should map ShippingSummaryCartonItemOutput to ShippingSummaryCartonItem correctly")
    void shouldMapCartonItemOutputToCartonItemCorrectly() {
        // Arrange

        ShippingSummaryCartonItem cartonItem = Mockito.mock(ShippingSummaryCartonItem.class, RETURNS_DEEP_STUBS);
        when(cartonItem.getCartonNumber()).thenReturn("CTN123");
        when(cartonItem.getProductCode()).thenReturn("PRD456");
        when(cartonItem.getProductDescription()).thenReturn("Product Description");

        // Act
        ShippingSummaryCartonItemOutput actualCartonItem = mapper.toOutput(cartonItem);

        // Assert
        assertNotNull(actualCartonItem);
        assertEquals(cartonItem.getCartonNumber(), actualCartonItem.cartonNumber());
        assertEquals(cartonItem.getProductCode(), actualCartonItem.productCode());
        assertEquals(cartonItem.getProductDescription(), actualCartonItem.productDescription());
        assertEquals(cartonItem.getTotalProducts(), actualCartonItem.totalProducts());
    }

    @Test
    @DisplayName("Should handle null input for ShippingSummaryReport")
    void shouldHandleNullShippingSummaryReport() {

        // Act
        ShippingSummaryReportOutput output = mapper.toOutput((ShippingSummaryReport) null);

        // Assert
        assertNull(output);
    }

    @Test
    @DisplayName("Should handle partial data in ShippingSummaryReport")
    void shouldHandlePartialData() {
        // Arrange
        ShippingSummaryReport partialReport = Mockito.mock(ShippingSummaryReport.class);
        ShippingSummaryShipmentDetail partialDetail = Mockito.mock(ShippingSummaryShipmentDetail.class);
        when(partialDetail.getShipmentNumber()).thenReturn("SHP123");
        when(partialReport.getShipmentDetail()).thenReturn(partialDetail);

        // Act
        ShippingSummaryReportOutput actualOutput = mapper.toOutput(partialReport);

        // Assert
        assertNotNull(actualOutput);
        assertEquals("SHP123", actualOutput.shipmentDetailShipmentNumber());
        assertNull(actualOutput.shipToAddress());
        assertNull(actualOutput.shipFromBloodCenterName());
    }
}
