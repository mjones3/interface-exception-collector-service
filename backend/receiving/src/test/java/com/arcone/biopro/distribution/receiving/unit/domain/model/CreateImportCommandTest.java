package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.CreateImportCommand;
import com.arcone.biopro.distribution.receiving.domain.model.Device;
import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import com.arcone.biopro.distribution.receiving.domain.repository.DeviceRepository;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateImportCommandTest {

    @Mock
    private ProductConsequenceRepository productConsequenceRepository;

    @Mock
    private DeviceRepository deviceRepository;

    private LocalDateTime now;
    private String validTemperatureCategory;
    private String validLocationCode;
    private String validEmployeeId;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        validTemperatureCategory = "FROZEN";
        validLocationCode = "LOC123";
        validEmployeeId = "EMP123";
    }

    @Test
    void constructor_ValidParameters_CreatesInstance() {
        // Arrange
        mockNoRequirements();

        // Act
        CreateImportCommand command = new CreateImportCommand(
            validTemperatureCategory,
            now,
            "UTC",
            now.plusHours(2),
            "UTC",
            BigDecimal.valueOf(20.5),
            "THERM123",
            validLocationCode,
            "Test comment",
            validEmployeeId,
            productConsequenceRepository,
            deviceRepository
        );

        // Assert
        assertNotNull(command);
    }

    @Test
    void constructor_NullTemperatureCategory_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new CreateImportCommand(
                null,
                now,
                "UTC",
                now.plusHours(2),
                "UTC",
                BigDecimal.valueOf(20.5),
                "THERM123",
                validLocationCode,
                "Test comment",
                validEmployeeId,
                productConsequenceRepository,
                deviceRepository
            )
        );
        assertEquals("Temperature category is required", exception.getMessage());
    }

    @Test
    void constructor_BlankTemperatureCategory_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new CreateImportCommand(
                "   ",
                now,
                "UTC",
                now.plusHours(2),
                "UTC",
                BigDecimal.valueOf(20.5),
                "THERM123",
                validLocationCode,
                "Test comment",
                validEmployeeId,
                productConsequenceRepository,
                deviceRepository
            )
        );
        assertEquals("Temperature category is required", exception.getMessage());
    }

    @Test
    void constructor_NullEmployeeId_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new CreateImportCommand(
                validTemperatureCategory,
                now,
                "UTC",
                now.plusHours(2),
                "UTC",
                BigDecimal.valueOf(20.5),
                "THERM123",
                validLocationCode,
                "Test comment",
                null,
                productConsequenceRepository,
                deviceRepository
            )
        );
        assertEquals("Employee id is required", exception.getMessage());
    }

    @Test
    void constructor_CommentsTooLong_ThrowsException() {
        String longComment = "a".repeat(251);
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new CreateImportCommand(
                validTemperatureCategory,
                now,
                "UTC",
                now.plusHours(2),
                "UTC",
                BigDecimal.valueOf(20.5),
                "THERM123",
                validLocationCode,
                longComment,
                validEmployeeId,
                productConsequenceRepository,
                deviceRepository
            )
        );
        assertEquals("Comments length must be less than 250 characters", exception.getMessage());
    }

    @Test
    void constructor_TransitTimeRequired_ValidData_CreatesInstance() {
        // Arrange
        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty("ROOM_TEMPERATURE", "TRANSIT_TIME"))
            .thenReturn(Flux.just(consequence));

        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty("ROOM_TEMPERATURE", "TEMPERATURE"))
            .thenReturn(Flux.empty());

        // Act
        CreateImportCommand command = new CreateImportCommand(
            "ROOM_TEMPERATURE",
            now,
            "UTC",
            now.plusHours(2),
            "UTC",
            null,
            null,
            validLocationCode,
            "Test comment",
            validEmployeeId,
            productConsequenceRepository,
            deviceRepository
        );

        // Assert
        assertNotNull(command);
    }

    @Test
    void constructor_TransitTimeRequired_MissingStartDateTime_ThrowsException() {
        // Arrange
        mockTransitTimeRequired();

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new CreateImportCommand(
                validTemperatureCategory,
                null,
                "UTC",
                now.plusHours(2),
                "UTC",
                null,
                null,
                validLocationCode,
                "Test comment",
                validEmployeeId,
                productConsequenceRepository,
                deviceRepository
            )
        );
        assertEquals("Transit start date time is required", exception.getMessage());
    }

    @Test
    void constructor_TemperatureRequired_ValidData_CreatesInstance() {
        // Arrange
        mockTemperatureRequired();
        mockValidDevice();

        // Act
        CreateImportCommand command = new CreateImportCommand(
            validTemperatureCategory,
            now,
            "UTC",
            now.plusHours(2),
            "UTC",
            BigDecimal.valueOf(20.5),
            "THERM123",
            validLocationCode,
            "Test comment",
            validEmployeeId,
            productConsequenceRepository,
            deviceRepository
        );

        // Assert
        assertNotNull(command);
    }

    @Test
    void constructor_TemperatureRequired_InvalidThermometer_ThrowsException() {
        // Arrange
        mockTemperatureRequired();
        when(deviceRepository.findFirstByBloodCenterIdAndLocationAndActiveIsTrue(any(), any()))
            .thenReturn(Mono.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            new CreateImportCommand(
                validTemperatureCategory,
                now,
                "UTC",
                now.plusHours(2),
                "UTC",
                BigDecimal.valueOf(20.5),
                "INVALID_THERM",
                validLocationCode,
                "Test comment",
                validEmployeeId,
                productConsequenceRepository,
                deviceRepository
            )
        );
        assertEquals("Thermometer code is not valid", exception.getMessage());
    }

    private void mockNoRequirements() {
        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(), any()))
            .thenReturn(Flux.empty());
    }

    private void mockTransitTimeRequired() {
        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(), any()))
            .thenReturn(Flux.just(consequence));
    }

    private void mockTemperatureRequired() {
        ProductConsequence consequence = Mockito.mock(ProductConsequence.class);
        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(), any()))
            .thenReturn(Flux.just(consequence));
    }

    private void mockValidDevice() {
        Device device = Mockito.mock(Device.class);
        when(deviceRepository.findFirstByBloodCenterIdAndLocationAndActiveIsTrue(any(), any()))
            .thenReturn(Mono.just(device));
    }
}

