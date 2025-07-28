package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.ValidateTransitTimeCommand;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidateTransitTimeCommandTest {

    private final LocalDateTime now = LocalDateTime.now();
    private final String validTimeZone = "UTC";
    private final String validTemperatureCategory = "FROZEN";

    @Test
    void constructor_ValidParameters_CreatesInstance() {

        var now = LocalDateTime.now();
        ValidateTransitTimeCommand command = new ValidateTransitTimeCommand(
            validTemperatureCategory,
            now.minusHours(1),
            validTimeZone,
            now,
            validTimeZone
        );

        assertNotNull(command);
        assertEquals(validTemperatureCategory, command.getTemperatureCategory());
        assertEquals(now.minusHours(1), command.getStartDateTime());
        assertEquals(validTimeZone, command.getStartTimeZone());
        assertEquals(now, command.getEndDateTime());
        assertEquals(validTimeZone, command.getEndTimeZone());
        assertEquals(ZoneId.of(validTimeZone), command.getStartZoneId());
        assertEquals(ZoneId.of(validTimeZone), command.getEndZoneId());
    }

    @Test
    void constructor_NullTemperatureCategory_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new ValidateTransitTimeCommand(
                null,
                now,
                validTimeZone,
                now.plusHours(2),
                validTimeZone
            )
        );
        assertEquals("Temperature category cannot be null or blank", exception.getMessage());
    }

    @Test
    void constructor_BlankTemperatureCategory_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new ValidateTransitTimeCommand(
                "   ",
                now,
                validTimeZone,
                now.plusHours(2),
                validTimeZone
            )
        );
        assertEquals("Temperature category cannot be null or blank", exception.getMessage());
    }

    @Test
    void constructor_NullStartDateTime_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new ValidateTransitTimeCommand(
                validTemperatureCategory,
                null,
                validTimeZone,
                now.plusHours(2),
                validTimeZone
            )
        );
        assertEquals("Start date time cannot be null", exception.getMessage());
    }

    @Test
    void constructor_NullStartTimeZone_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new ValidateTransitTimeCommand(
                validTemperatureCategory,
                now,
                null,
                now.plusHours(2),
                validTimeZone
            )
        );
        assertEquals("Start time zone cannot be null or blank", exception.getMessage());
    }

    @Test
    void constructor_BlankStartTimeZone_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new ValidateTransitTimeCommand(
                validTemperatureCategory,
                now,
                "  ",
                now.plusHours(2),
                validTimeZone
            )
        );
        assertEquals("Start time zone cannot be null or blank", exception.getMessage());
    }

    @Test
    void constructor_InvalidStartTimeZone_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new ValidateTransitTimeCommand(
                validTemperatureCategory,
                now,
                "INVALID_TIMEZONE",
                now.plusHours(2),
                validTimeZone
            )
        );
        assertEquals("Invalid start time zone", exception.getMessage());
    }

    @Test
    void constructor_NullEndDateTime_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new ValidateTransitTimeCommand(
                validTemperatureCategory,
                now,
                validTimeZone,
                null,
                validTimeZone
            )
        );
        assertEquals("End date time cannot be null", exception.getMessage());
    }

    @Test
    void constructor_NullEndTimeZone_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new ValidateTransitTimeCommand(
                validTemperatureCategory,
                now,
                validTimeZone,
                now.plusHours(2),
                null
            )
        );
        assertEquals("End time zone cannot be null or blank", exception.getMessage());
    }

    @Test
    void constructor_BlankEndTimeZone_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new ValidateTransitTimeCommand(
                validTemperatureCategory,
                now,
                validTimeZone,
                now.plusHours(2),
                "  "
            )
        );
        assertEquals("End time zone cannot be null or blank", exception.getMessage());
    }

    @Test
    void constructor_InvalidEndTimeZone_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new ValidateTransitTimeCommand(
                validTemperatureCategory,
                now,
                validTimeZone,
                now.plusHours(2),
                "INVALID_TIMEZONE"
            )
        );
        assertEquals("Invalid end time zone", exception.getMessage());
    }

    @Test
    void constructor_DifferentValidTimeZones_CreatesInstance() {
        var now = LocalDateTime.now();
        ValidateTransitTimeCommand command = new ValidateTransitTimeCommand(
            validTemperatureCategory,
            now.minusHours(1),
            "America/New_York",
            now,
            "America/Los_Angeles"
        );

        assertNotNull(command);
        assertEquals(ZoneId.of("America/New_York"), command.getStartZoneId());
        assertEquals(ZoneId.of("America/Los_Angeles"), command.getEndZoneId());
    }

    @Test
    void constructor_EndDateFuture_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new ValidateTransitTimeCommand(
                validTemperatureCategory,
                now,
                validTimeZone,
                now.plusHours(2),
                "America/New_York"
            )
        );
        assertEquals("End date date cannot be in the future", exception.getMessage());
    }

    @Test
    void constructor_StartDateGreatherThanEndDate_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new ValidateTransitTimeCommand(
                validTemperatureCategory,
                now.plusHours(1),
                validTimeZone,
                now,
                "America/New_York"
            )
        );
        assertEquals("Start date date cannot be after end date date", exception.getMessage());
    }

    @Test
    void constructor_EndDateLessThanStartDate_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new ValidateTransitTimeCommand(
                validTemperatureCategory,
                now.minusDays(1),
                validTimeZone,
                now.minusDays(2),
                "America/New_York"
            )
        );
        assertEquals("Start date date cannot be after end date date", exception.getMessage());
    }
}

