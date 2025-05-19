package com.arcone.biopro.distribution.recoveredplasmashipping.unit.domain.model.vo;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.UnacceptableUnitReportItem;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnacceptableUnitReportItemTest {

    @Test
    void shouldCreateValidUnacceptableUnitReportItem() {
        // Given
        Long shipmentId = 1L;
        String cartonNumber = "CTN001";
        Integer cartonSequenceNumber = 1;
        String unitNumber = "UNIT001";
        String productCode = "PROD001";
        String failureReason = "Quality Check Failed";
        ZonedDateTime createDate = ZonedDateTime.now();

        // When
        UnacceptableUnitReportItem reportItem = new UnacceptableUnitReportItem(
            shipmentId,
            cartonNumber,
            cartonSequenceNumber,
            unitNumber,
            productCode,
            failureReason,
            createDate
        );

        // Then
        assertNotNull(reportItem);
        assertEquals(shipmentId, reportItem.getShipmentId());
        assertEquals(cartonNumber, reportItem.getCartonNumber());
        assertEquals(cartonSequenceNumber, reportItem.getCartonSequenceNumber());
        assertEquals(unitNumber, reportItem.getUnitNumber());
        assertEquals(productCode, reportItem.getProductCode());
        assertEquals(failureReason, reportItem.getFailureReason());
        assertEquals(createDate, reportItem.getCreateDate());
    }

    @Test
    void shouldThrowExceptionWhenCartonNumberIsNull() {
        // Given
        Long shipmentId = 1L;
        String cartonNumber = null;
        Integer cartonSequenceNumber = 1;
        String unitNumber = "UNIT001";
        String productCode = "PROD001";
        String failureReason = "Quality Check Failed";
        ZonedDateTime createDate = ZonedDateTime.now();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new UnacceptableUnitReportItem(
                shipmentId,
                cartonNumber,
                cartonSequenceNumber,
                unitNumber,
                productCode,
                failureReason,
                createDate
            )
        );
        assertEquals("Carton Number is null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCartonNumberIsBlank() {
        // Given
        Long shipmentId = 1L;
        String cartonNumber = "   ";
        Integer cartonSequenceNumber = 1;
        String unitNumber = "UNIT001";
        String productCode = "PROD001";
        String failureReason = "Quality Check Failed";
        ZonedDateTime createDate = ZonedDateTime.now();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new UnacceptableUnitReportItem(
                shipmentId,
                cartonNumber,
                cartonSequenceNumber,
                unitNumber,
                productCode,
                failureReason,
                createDate
            )
        );
        assertEquals("Carton Number is null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenCartonSequenceNumberIsNull() {
        // Given
        Long shipmentId = 1L;
        String cartonNumber = "CTN001";
        Integer cartonSequenceNumber = null;
        String unitNumber = "UNIT001";
        String productCode = "PROD001";
        String failureReason = "Quality Check Failed";
        ZonedDateTime createDate = ZonedDateTime.now();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new UnacceptableUnitReportItem(
                shipmentId,
                cartonNumber,
                cartonSequenceNumber,
                unitNumber,
                productCode,
                failureReason,
                createDate
            )
        );
        assertEquals("Carton Sequence Number is null", exception.getMessage());
    }

    @Test
    void shouldTestEqualsAndHashCode() {
        // Given
        ZonedDateTime now = ZonedDateTime.now();
        UnacceptableUnitReportItem item1 = new UnacceptableUnitReportItem(
            1L, "CTN001", 1, "UNIT001", "PROD001", "Reason", now
        );
        UnacceptableUnitReportItem item2 = new UnacceptableUnitReportItem(
            1L, "CTN001", 1, "UNIT001", "PROD001", "Reason", now
        );
        UnacceptableUnitReportItem item3 = new UnacceptableUnitReportItem(
            2L, "CTN002", 2, "UNIT002", "PROD002", "Reason", now
        );

        // Then
        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
        assertEquals(item1.hashCode(), item2.hashCode());
        assertNotEquals(item1.hashCode(), item3.hashCode());
    }

    @Test
    void shouldTestToString() {
        // Given
        ZonedDateTime now = ZonedDateTime.now();
        UnacceptableUnitReportItem item = new UnacceptableUnitReportItem(
            1L, "CTN001", 1, "UNIT001", "PROD001", "Reason", now
        );

        // When
        String toString = item.toString();

        // Then
        assertTrue(toString.contains("shipmentId=1"));
        assertTrue(toString.contains("cartonNumber=CTN001"));
        assertTrue(toString.contains("cartonSequenceNumber=1"));
        assertTrue(toString.contains("unitNumber=UNIT001"));
        assertTrue(toString.contains("productCode=PROD001"));
        assertTrue(toString.contains("failureReason=Reason"));
    }
}

