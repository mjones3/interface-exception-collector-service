package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.exception.TypeNotConfiguredException;
import com.arcone.biopro.distribution.receiving.domain.model.AddImportItemCommand;
import com.arcone.biopro.distribution.receiving.domain.model.vo.AboRh;
import com.arcone.biopro.distribution.receiving.domain.model.vo.LicenseStatus;
import com.arcone.biopro.distribution.receiving.domain.model.vo.VisualInspection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class AddImportItemCommandTest {

    private Long validImportId;
    private String validUnitNumber;
    private String validProductCode;
    private String validAboRh;
    private LocalDateTime validExpirationDate;
    private String validVisualInspection;
    private String validLicenseStatus;
    private String validEmployeeId;

    @BeforeEach
    void setUp() {
        validImportId = 1L;
        validUnitNumber = "UNIT123";
        validProductCode = "PROD123";
        validAboRh = "AP";
        validExpirationDate = LocalDateTime.now().plusDays(30);
        validVisualInspection = "SATISFACTORY";
        validLicenseStatus = "LICENSED";
        validEmployeeId = "EMP123";
    }

    @Test
    void constructor_WithValidParameters_ShouldCreateCommand() {
        // Act
        AddImportItemCommand command = new AddImportItemCommand(
            validImportId,
            validUnitNumber,
            validProductCode,
            validAboRh,
            validExpirationDate,
            validVisualInspection,
            validLicenseStatus,
            validEmployeeId
        );

        // Assert
        assertNotNull(command);
        assertEquals(validImportId, command.getImportId());
        assertEquals(validUnitNumber, command.getUnitNumber());
        assertEquals(validProductCode, command.getProductCode());
        assertEquals(AboRh.getInstance(validAboRh), command.getAboRh());
        assertEquals(validExpirationDate, command.getExpirationDate());
        assertEquals(VisualInspection.getInstance(validVisualInspection), command.getVisualInspection());
        assertEquals(LicenseStatus.getInstance(validLicenseStatus), command.getLicenseStatus());
        assertEquals(validEmployeeId, command.getEmployeeId());
    }

    @Test
    void constructor_WithNullImportId_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new AddImportItemCommand(
                null,
                validUnitNumber,
                validProductCode,
                validAboRh,
                validExpirationDate,
                validVisualInspection,
                validLicenseStatus,
                validEmployeeId
            )
        );
        assertEquals("Import Id is required", exception.getMessage());
    }

    @Test
    void constructor_WithNullVisualInspection_ShouldThrowException() {
        // Act & Assert
        TypeNotConfiguredException exception = assertThrows(TypeNotConfiguredException.class,
            () -> new AddImportItemCommand(
                validImportId,
                validUnitNumber,
                validProductCode,
                validAboRh,
                validExpirationDate,
                "",
                validLicenseStatus,
                validEmployeeId
            )
        );
        assertEquals("Visual Inspection Not Configured", exception.getMessage());
    }

    @Test
    void constructor_WithNullLicenseStatus_ShouldThrowException() {
        // Act & Assert
        TypeNotConfiguredException exception = assertThrows(TypeNotConfiguredException.class,
            () -> new AddImportItemCommand(
                validImportId,
                validUnitNumber,
                validProductCode,
                validAboRh,
                validExpirationDate,
                validVisualInspection,
                "",
                validEmployeeId
            )
        );
        assertEquals("License Status Not Configured", exception.getMessage());
    }

    @Test
    void constructor_WithNullUnitNumber_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new AddImportItemCommand(
                validImportId,
                null,
                validProductCode,
                validAboRh,
                validExpirationDate,
                validVisualInspection,
                validLicenseStatus,
                validEmployeeId
            )
        );
        assertEquals("Unit Number is required", exception.getMessage());
    }

    @Test
    void constructor_WithBlankUnitNumber_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new AddImportItemCommand(
                validImportId,
                "  ",
                validProductCode,
                validAboRh,
                validExpirationDate,
                validVisualInspection,
                validLicenseStatus,
                validEmployeeId
            )
        );
        assertEquals("Unit Number is required", exception.getMessage());
    }

    @Test
    void constructor_WithNullProductCode_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new AddImportItemCommand(
                validImportId,
                validUnitNumber,
                null,
                validAboRh,
                validExpirationDate,
                validVisualInspection,
                validLicenseStatus,
                validEmployeeId
            )
        );
        assertEquals("Product Code is required", exception.getMessage());
    }

    @Test
    void constructor_WithBlankProductCode_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new AddImportItemCommand(
                validImportId,
                validUnitNumber,
                "",
                validAboRh,
                validExpirationDate,
                validVisualInspection,
                validLicenseStatus,
                validEmployeeId
            )
        );
        assertEquals("Product Code is required", exception.getMessage());
    }

    @Test
    void constructor_WithInvalidAboRh_ShouldThrowException() {
        // Act & Assert
        TypeNotConfiguredException exception = assertThrows(TypeNotConfiguredException.class,
            () -> new AddImportItemCommand(
                validImportId,
                validUnitNumber,
                validProductCode,
                "",
                validExpirationDate,
                validVisualInspection,
                validLicenseStatus,
                validEmployeeId
            )
        );
        assertEquals("ABO/RH is Invalid", exception.getMessage());
    }

    @Test
    void constructor_WithNullExpirationDate_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new AddImportItemCommand(
                validImportId,
                validUnitNumber,
                validProductCode,
                validAboRh,
                null,
                validVisualInspection,
                validLicenseStatus,
                validEmployeeId
            )
        );
        assertEquals("Expiration Date is required", exception.getMessage());
    }

    @Test
    void constructor_WithInvalidVisualInspectionValue_ShouldThrowException() {
        // Act & Assert
        assertThrows(TypeNotConfiguredException.class,
            () -> new AddImportItemCommand(
                validImportId,
                validUnitNumber,
                validProductCode,
                validAboRh,
                validExpirationDate,
                "INVALID_VALUE",
                validLicenseStatus,
                validEmployeeId
            )
        );
    }

    @Test
    void constructor_WithInvalidLicenseStatusValue_ShouldThrowException() {
        // Act & Assert
        assertThrows(TypeNotConfiguredException.class,
            () -> new AddImportItemCommand(
                validImportId,
                validUnitNumber,
                validProductCode,
                validAboRh,
                validExpirationDate,
                validVisualInspection,
                "INVALID_VALUE",
                validEmployeeId
            )
        );
    }
}

