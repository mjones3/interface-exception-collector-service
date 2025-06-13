package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import com.arcone.biopro.distribution.receiving.domain.model.TransitTimeValidator;
import com.arcone.biopro.distribution.receiving.domain.model.ValidateTransitTimeCommand;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransitTimeValidatorTest {

    private ValidateTransitTimeCommand command;
    private List<ProductConsequence> productConsequenceList;

    @BeforeEach
    void setUp() {
        // Initialize base command //LocalDateTime startDateTime, String startTimeZone, LocalDateTime endDateTime, String endTimeZone
        command = new ValidateTransitTimeCommand("ROOM_TEMPERATURE",LocalDateTime.now(),"UTC",LocalDateTime.now().plusHours(5),"UTC");

        // Initialize product consequences
        productConsequenceList = new ArrayList<>();
    }

    @Test
    void validateTransitTime_NullCommand_ThrowsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            TransitTimeValidator.validateTransitTime(null, productConsequenceList)
        );
        assertEquals("Transit Time Information is required", exception.getMessage());
    }

    @Test
    void validateTransitTime_NullProductConsequenceList_ThrowsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            TransitTimeValidator.validateTransitTime(command, null)
        );
        assertEquals("ProductConsequenceList is required", exception.getMessage());
    }

    @Test
    void validateTransitTime_EmptyProductConsequenceList_ThrowsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            TransitTimeValidator.validateTransitTime(command, new ArrayList<>())
        );
        assertEquals("ProductConsequenceList is required", exception.getMessage());
    }

    @Test
    void validateTransitTime_NoMatchingConsequence_ThrowsIllegalArgumentException() {
        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn("false");
        Mockito.when(consequence.isAcceptable()).thenReturn(true);

        productConsequenceList.add(consequence);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            TransitTimeValidator.validateTransitTime(command, productConsequenceList)
        );
        assertEquals("Product Consequence not found.", exception.getMessage());
    }

    @Test
    void validateTransitTime_AcceptableTransitTime_ReturnsValidResult() {
        // Setup consequence for 5 hours transit time
        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn("TRANSIT_TIME >= 0 && TRANSIT_TIME <= (24 * 60)");
        Mockito.when(consequence.isAcceptable()).thenReturn(true);

        productConsequenceList.add(consequence);

        ValidationResult result = TransitTimeValidator.validateTransitTime(command, productConsequenceList);

        assertTrue(result.valid());
        assertEquals("PT5H", result.result());
        assertEquals("5h 0m", result.resultDescription());
    }

    @Test
    void validateTransitTime_AcceptableTransitTime_ReturnsValidResultWithMinutes() {
        // Setup consequence for 5 hours transit time
        command = new ValidateTransitTimeCommand("ROOM_TEMPERATURE",LocalDateTime.now(),"UTC",LocalDateTime.now().plusHours(5).plusMinutes(30),"UTC");


        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn("TRANSIT_TIME >= 0 && TRANSIT_TIME <= (24 * 60)");
        Mockito.when(consequence.isAcceptable()).thenReturn(true);

        productConsequenceList.add(consequence);

        ValidationResult result = TransitTimeValidator.validateTransitTime(command, productConsequenceList);

        assertTrue(result.valid());
        assertEquals("PT5H30M", result.result());
        assertEquals("5h 30m", result.resultDescription());
    }

    @Test
    void validateTransitTime_UnacceptableTransitTime_ReturnsInvalidResult() {
        // Setup command with 25 hours transit time
        command = new ValidateTransitTimeCommand("ROOM_TEMPERATURE",LocalDateTime.now(),"UTC",LocalDateTime.now().plusHours(25),"UTC");

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn("TRANSIT_TIME >= 0 && TRANSIT_TIME <= (24 * 60)");
        Mockito.when(consequence.isAcceptable()).thenReturn(true);

        ProductConsequence consequence2 = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence2.getResultValue()).thenReturn("TRANSIT_TIME > (24 * 60)");
        Mockito.when(consequence2.isAcceptable()).thenReturn(false);

        productConsequenceList.add(consequence);
        productConsequenceList.add(consequence2);

        ValidationResult result = TransitTimeValidator.validateTransitTime(command, productConsequenceList);

        assertFalse(result.valid());
        assertEquals("Total Transit Time does not meet thresholds. All products will be quarantined.", result.message());
    }

    @Test
    void validateTransitTime_UnacceptableTransitTime_ReturnsInvalidResultHoursWithMinutes() {
        // Setup command with 25 hours transit time
        var now = LocalDateTime.now();
        command = new ValidateTransitTimeCommand("ROOM_TEMPERATURE",now,"UTC",now.plusDays(1).plusMinutes(5),"UTC");

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn("TRANSIT_TIME >= 0 && TRANSIT_TIME <= (24 * 60)");
        Mockito.when(consequence.isAcceptable()).thenReturn(true);

        ProductConsequence consequence2 = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence2.getResultValue()).thenReturn("TRANSIT_TIME > (24 * 60)");
        Mockito.when(consequence2.isAcceptable()).thenReturn(false);

        productConsequenceList.add(consequence);
        productConsequenceList.add(consequence2);

        ValidationResult result = TransitTimeValidator.validateTransitTime(command, productConsequenceList);

        assertFalse(result.valid());
        assertEquals("Total Transit Time does not meet thresholds. All products will be quarantined.", result.message());
    }

    @Test
    void validateTransitTime_DifferentTimeZones_CalculatesCorrectTransitTime() {

        command = new ValidateTransitTimeCommand("ROOM_TEMPERATURE",LocalDateTime.now(),"America/New_York",LocalDateTime.now().plusHours(5),"America/Los_Angeles");

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn("TRANSIT_TIME >= 0 && TRANSIT_TIME <= (24 * 60)");
        Mockito.when(consequence.isAcceptable()).thenReturn(true);

        productConsequenceList.add(consequence);

        ValidationResult result = TransitTimeValidator.validateTransitTime(command, productConsequenceList);

        assertTrue(result.valid());
        assertEquals("PT8H", result.result());
        assertEquals("8h 0m", result.resultDescription());
    }

    @Test
    void validateTransitTime_SameTimeZones_CalculatesCorrectTransitTimeInMinutes() {

        command = new ValidateTransitTimeCommand("ROOM_TEMPERATURE",LocalDateTime.now(),"America/New_York",LocalDateTime.now().plusMinutes(30),"America/New_York");

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn("TRANSIT_TIME >= 0 && TRANSIT_TIME <= (24 * 60)");
        Mockito.when(consequence.isAcceptable()).thenReturn(true);

        productConsequenceList.add(consequence);

        ValidationResult result = TransitTimeValidator.validateTransitTime(command, productConsequenceList);

        assertTrue(result.valid());
        assertEquals("PT30M", result.result());
        assertEquals("0h 30m", result.resultDescription());
    }

    @Test
    void validateTransitTime_SameTimeZones_StartDateLessThanEndDateTimeInHours() {

        var now = LocalDateTime.now();
        command = new ValidateTransitTimeCommand("ROOM_TEMPERATURE",now,"America/New_York",now.minusHours(2),"America/New_York");

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn("TRANSIT_TIME > (24 * 60) || TRANSIT_TIME < 0");
        Mockito.when(consequence.isAcceptable()).thenReturn(false);

        productConsequenceList.add(consequence);

        ValidationResult result = TransitTimeValidator.validateTransitTime(command, productConsequenceList);

        assertFalse(result.valid());
        assertEquals("PT-2H", result.result());
        assertEquals("-2h 0m", result.resultDescription());
    }

    @Test
    void validateTransitTime_SameTimeZones_StartDateLessThanEndDateTimeInMinutes() {

        var now = LocalDateTime.now();
        command = new ValidateTransitTimeCommand("ROOM_TEMPERATURE",now,"America/New_York",now.minusMinutes(30),"America/New_York");

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequence.getResultValue()).thenReturn("TRANSIT_TIME > (24 * 60) || TRANSIT_TIME < 0");
        Mockito.when(consequence.isAcceptable()).thenReturn(false);

        productConsequenceList.add(consequence);

        ValidationResult result = TransitTimeValidator.validateTransitTime(command, productConsequenceList);

        assertFalse(result.valid());
        assertEquals("PT-30M", result.result());
        assertEquals("0h -30m", result.resultDescription());
    }

}
