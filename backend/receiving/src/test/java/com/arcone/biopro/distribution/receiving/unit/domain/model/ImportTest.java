package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.CreateImportCommand;
import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import com.arcone.biopro.distribution.receiving.domain.model.TemperatureValidator;
import com.arcone.biopro.distribution.receiving.domain.model.TransitTimeValidator;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ValidationResult;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportTest {

    @Mock
    private ProductConsequenceRepository productConsequenceRepository;

    @Mock
    private CreateImportCommand createImportCommand;

    private MockedStatic<TransitTimeValidator> mockedStaticTransitValidator;

    private MockedStatic<TemperatureValidator> temperatureValidatorMockedStatic;

    private LocalDateTime now;
    private ZonedDateTime zonedNow;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        zonedNow = ZonedDateTime.now();
        mockedStaticTransitValidator = Mockito.mockStatic(TransitTimeValidator.class);
        temperatureValidatorMockedStatic = Mockito.mockStatic(TemperatureValidator.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedStaticTransitValidator != null) {
            mockedStaticTransitValidator.close();
        }
        if (temperatureValidatorMockedStatic != null) {
            temperatureValidatorMockedStatic.close();
        }
    }

    @Test
    void create_ValidCommand_CreatesImport() {

        ValidationResult validationResult = ValidationResult.builder()
            .valid(false)
            .result("2")
            .build();

        mockedStaticTransitValidator.when(() -> TransitTimeValidator.validateTransitTime(any(), any())).thenReturn(validationResult);

        temperatureValidatorMockedStatic.when(() -> TemperatureValidator.validateTemperature(any(), any())).thenReturn(validationResult);

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        ProductConsequence consequence2 = Mockito.mock(ProductConsequence.class);

        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(), any()))
            .thenReturn(Flux.just(consequence, consequence2));

        // Arrange
        setupValidCreateImportCommand();

        // Arrange
        setupValidCreateImportCommand();

        // Act
        Import result = Import.create(createImportCommand, productConsequenceRepository);

        // Assert
        assertNotNull(result);
        assertEquals("FROZEN", result.getTemperatureCategory());
        assertEquals("PENDING", result.getStatus());
        assertEquals("2", result.getTotalTransitTime());
        assertEquals("UNACCEPTABLE", result.getTransitTimeResult());
        assertEquals("20.5", result.getTemperature().toString());
        assertEquals("THERM123", result.getThermometerCode());
        assertEquals("UNACCEPTABLE", result.getTemperatureResult());
        assertEquals("LOC123", result.getLocationCode());
        assertEquals("Test comment", result.getComments());
        assertEquals("EMP123", result.getEmployeeId());
        assertNotNull(result.getCreateDate());
        assertNotNull(result.getModificationDate());


    }

    @Test
    void create_NullCommand_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            Import.create(null, productConsequenceRepository)
        );
        assertEquals("CreateImportCommand is required", exception.getMessage());
    }

    @Test
    void fromRepository_ValidData_CreatesImport() {
        // Act
        Import result = Import.fromRepository(
            1L,
            "FROZEN",
            now,
            "UTC",
            now.plusHours(2),
            "UTC",
            "2",
            "ACCEPTABLE",
            BigDecimal.valueOf(20.5),
            "THERM123",
            "ACCEPTABLE",
            "LOC123",
            "Test comment",
            "PENDING",
            "EMP123",
            zonedNow,
            zonedNow
        );

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("FROZEN", result.getTemperatureCategory());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void checkValid_ValidData_NoException() {
        // Arrange
        CreateImportCommand command = Mockito.mock(CreateImportCommand.class);
        when(command.getTemperatureCategory()).thenReturn("FROZEN");
        when(command.getLocationCode()).thenReturn("LOC123");
        when(command.getEmployeeId()).thenReturn("EMP123");
        when(command.getTemperatureCategory()).thenReturn("FROZEN");

        Import validImport = Import.create(command, productConsequenceRepository);

        // Act & Assert
        assertDoesNotThrow(validImport::checkValid);

    }

    @Test
    void checkValid_NullTemperatureCategory_ThrowsException() {
        // Arrange
        CreateImportCommand command = Mockito.mock(CreateImportCommand.class);
    }

    @Test
    void checkValid_NullLocationCode_ThrowsException() {
        // Arrange
        CreateImportCommand command = Mockito.mock(CreateImportCommand.class);
        when(command.getTemperatureCategory()).thenReturn("FROZEN");
        when(command.getEmployeeId()).thenReturn("EMP123");

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> Import.create(command, productConsequenceRepository));
        assertEquals("Location code is required", exception.getMessage());


    }

    @Test
    void checkValid_CommentsLengthExceeded_ThrowsException() {
        // Arrange
        CreateImportCommand command = Mockito.mock(CreateImportCommand.class);
        when(command.getTemperatureCategory()).thenReturn("FROZEN");
        when(command.getLocationCode()).thenReturn("LOC123");
        when(command.getEmployeeId()).thenReturn("EMP123");
        when(command.getComments()).thenReturn("a".repeat(251));


        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> Import.create(command, productConsequenceRepository));
        assertEquals("Comments length must be less than 250 characters", exception.getMessage());


    }

    @Test
    void checkValid_TransitTimeValidation_ThrowsException() {
        // Arrange
        CreateImportCommand command = Mockito.mock(CreateImportCommand.class);
        when(command.getTemperatureCategory()).thenReturn("FROZEN");
        when(command.getLocationCode()).thenReturn("LOC123");
        when(command.getEmployeeId()).thenReturn("EMP123");
        when(command.getTransitStartDateTime()).thenReturn(now);
        when(command.getTransitStartTimeZone()).thenReturn("UTC");
        when(command.getTransitEndDateTime()).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> Import.create(command, productConsequenceRepository));
        assertEquals("End date time cannot be null", exception.getMessage());

    }

    @Test
    void isQuarantined_UnacceptableTemperature_ReturnsTrue() {

        // Arrange
        setupValidCreateImportCommand();

        ValidationResult validationResult = ValidationResult.builder()
            .valid(true)
            .result("2")
            .build();

        ValidationResult validationResultTemperature = ValidationResult.builder()
            .valid(false)
            .result("2")
            .build();

        mockedStaticTransitValidator.when(() -> TransitTimeValidator.validateTransitTime(any(), any())).thenReturn(validationResult);
        temperatureValidatorMockedStatic.when(() -> TemperatureValidator.validateTemperature(any(), any())).thenReturn(validationResultTemperature);

        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        ProductConsequence consequence2 = Mockito.mock(ProductConsequence.class);

        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(), any()))
            .thenReturn(Flux.just(consequence, consequence2));

        Import quarantinedImport = Import.create(createImportCommand, productConsequenceRepository);

        // Act & Assert
        assertTrue(quarantinedImport.isQuarantined());


    }

    @Test
    void isQuarantined_UnacceptableTransitTime_ReturnsTrue() {
        // Arrange

        setupValidCreateImportCommand();
        ValidationResult validationResult = ValidationResult.builder()
            .valid(false)
            .result("2")
            .build();

        mockedStaticTransitValidator.when(() -> TransitTimeValidator.validateTransitTime(any(), any())).thenReturn(validationResult);
        temperatureValidatorMockedStatic.when(() -> TemperatureValidator.validateTemperature(any(), any())).thenReturn(validationResult);


        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        ProductConsequence consequence2 = Mockito.mock(ProductConsequence.class);

        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(), any()))
            .thenReturn(Flux.just(consequence, consequence2));


        Import quarantinedImport = Import.create(createImportCommand, productConsequenceRepository);

        // Act & Assert
        assertTrue(quarantinedImport.isQuarantined());

    }

    @Test
    void isQuarantined_AllAcceptable_ReturnsFalse() {

        setupValidCreateImportCommand();
        ValidationResult validationResult = ValidationResult.builder()
            .valid(true)
            .result("2")
            .build();

        mockedStaticTransitValidator.when(() -> TransitTimeValidator.validateTransitTime(any(), any())).thenReturn(validationResult);
        temperatureValidatorMockedStatic.when(() -> TemperatureValidator.validateTemperature(any(), any())).thenReturn(validationResult);


        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        ProductConsequence consequence2 = Mockito.mock(ProductConsequence.class);

        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(), any()))
            .thenReturn(Flux.just(consequence, consequence2));


        Import acceptableImport = Import.create(createImportCommand, productConsequenceRepository);

        // Act & Assert
        assertFalse(acceptableImport.isQuarantined());


    }

    @Test
    void checkValid_ThrowsException_When_DeviceNull() {

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> Import.fromRepository(
                1L,
                "FROZEN",
                now,
                "UTC",
                now.plusHours(2),
                "UTC",
                "2",
                "ACCEPTABLE",
                BigDecimal.valueOf(20.5),
                null,
                "ACCEPTABLE",
                "LOC123",
                "Test comment",
                "PENDING",
                "EMP123",
                zonedNow,
                zonedNow
            ));
        assertEquals("Thermometer code is required", exception.getMessage());

    }

    @Test
    void checkValid_ThrowsException_When_TemperatureResultNull() {

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> Import.fromRepository(
                1L,
                "FROZEN",
                now,
                "UTC",
                now.plusHours(2),
                "UTC",
                "2",
                "ACCEPTABLE",
                BigDecimal.valueOf(20.5),
                "THERM-2",
                null,
                "LOC123",
                "Test comment",
                "PENDING",
                "EMP123",
                zonedNow,
                zonedNow
            ));
        assertEquals("Temperature result is required", exception.getMessage());

    }

    @Test
    void checkValid_ThrowsException_When_TransiTimeNull() {

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> Import.fromRepository(
                1L,
                "FROZEN",
                now,
                null,
                now.plusHours(2),
                "UTC",
                "2",
                "ACCEPTABLE",
                null,
                null,
                null,
                "LOC123",
                "Test comment",
                "PENDING",
                "EMP123",
                zonedNow,
                zonedNow
            ));
        assertEquals("Transit start time zone is required", exception.getMessage());

    }

    @Test
    void checkValid_ThrowsException_When_TransitEndTimeNull() {

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> Import.fromRepository(
                1L,
                "FROZEN",
                now,
                "ET",
                null,
                "UTC",
                "2",
                "ACCEPTABLE",
                null,
                null,
                null,
                "LOC123",
                "Test comment",
                "PENDING",
                "EMP123",
                zonedNow,
                zonedNow
            ));
        assertEquals("Transit end date time is required", exception.getMessage());

    }

    @Test
    void checkValid_ThrowsException_When_TransitResultNull() {

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> Import.fromRepository(
                1L,
                "FROZEN",
                now,
                "ET",
                LocalDateTime.now(),
                "UTC",
                "2",
                null,
                null,
                null,
                null,
                "LOC123",
                "Test comment",
                "PENDING",
                "EMP123",
                zonedNow,
                zonedNow
            ));
        assertEquals("Transit time result is required", exception.getMessage());

    }

    private void setupValidCreateImportCommand() {
        when(createImportCommand.getTemperatureCategory()).thenReturn("FROZEN");
        when(createImportCommand.getTransitStartDateTime()).thenReturn(now);
        when(createImportCommand.getTransitStartTimeZone()).thenReturn("UTC");
        when(createImportCommand.getTransitEndDateTime()).thenReturn(now.plusHours(2));
        when(createImportCommand.getTransitEndTimeZone()).thenReturn("UTC");
        when(createImportCommand.getThermometerCode()).thenReturn("THERM123");
        when(createImportCommand.getTemperature()).thenReturn(BigDecimal.valueOf(20.5));
        when(createImportCommand.getLocationCode()).thenReturn("LOC123");
        when(createImportCommand.getComments()).thenReturn("Test comment");
        when(createImportCommand.getEmployeeId()).thenReturn("EMP123");
    }
}

import com.arcone.biopro.distribution.receiving.domain.model.ValidateTemperatureCommand;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
    void validateTemperature_WhenCommandIsNull_ThrowsException() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> Import.validateTemperature(null, productConsequenceRepository)
        );
        assertEquals("Temperature Information is required", exception.getMessage());
    void validateTemperature_WhenRepositoryIsNull_ThrowsException() {
        ValidateTemperatureCommand command = new ValidateTemperatureCommand(new BigDecimal("-18.0"),"FROZEN");
