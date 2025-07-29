package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.AddImportItemCommand;
import com.arcone.biopro.distribution.receiving.domain.model.CreateImportCommand;
import com.arcone.biopro.distribution.receiving.domain.model.FinNumber;
import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.domain.model.ImportItem;
import com.arcone.biopro.distribution.receiving.domain.model.Product;
import com.arcone.biopro.distribution.receiving.domain.model.ProductConsequence;
import com.arcone.biopro.distribution.receiving.domain.model.TemperatureValidator;
import com.arcone.biopro.distribution.receiving.domain.model.TransitTimeValidator;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ValidationResult;
import com.arcone.biopro.distribution.receiving.domain.repository.ProductConsequenceRepository;
import com.arcone.biopro.distribution.receiving.domain.service.ConfigurationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportTest {

    @Mock
    private ProductConsequenceRepository productConsequenceRepository;

    @Mock
    private CreateImportCommand createImportCommand;

    private MockedStatic<TransitTimeValidator> mockedStaticTransitValidator;

    private MockedStatic<TemperatureValidator> temperatureValidatorMockedStatic;

    private MockedStatic<ImportItem> importItemMockedStatic;

    private LocalDateTime yesterday;
    private ZonedDateTime zonedNow;

    private ConfigurationService configurationService;

    @BeforeEach
    void setUp() {
        yesterday = LocalDateTime.now().minusDays(1);
        zonedNow = ZonedDateTime.now();
        mockedStaticTransitValidator = Mockito.mockStatic(TransitTimeValidator.class);
        temperatureValidatorMockedStatic = Mockito.mockStatic(TemperatureValidator.class);
        configurationService = Mockito.mock(ConfigurationService.class);
        importItemMockedStatic = Mockito.mockStatic(ImportItem.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedStaticTransitValidator != null) {
            mockedStaticTransitValidator.close();
        }
        if (temperatureValidatorMockedStatic != null) {
            temperatureValidatorMockedStatic.close();
        }
        if (importItemMockedStatic != null){
            importItemMockedStatic.close();
        }
    }

    @Test
    void create_ValidCommand_CreatesImport() {

        ValidationResult validationResult = ValidationResult.builder()
            .valid(false)
            .result("PT2H")
            .resultDescription("2")
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
        assertEquals("20.50", result.getTemperature().toString());
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
            yesterday,
            "UTC",
            yesterday.plusHours(2),
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
            zonedNow,null,10
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
        when(command.getTransitStartDateTime()).thenReturn(yesterday);
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
            .result("PT2H")
            .resultDescription("2")
            .build();

        ValidationResult validationResultTemperature = ValidationResult.builder()
            .valid(false)
            .result("PT2H")
            .resultDescription("2")
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
            .result("PT2H")
            .resultDescription("2")
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
            .result("PT2H")
            .resultDescription("2")
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
                yesterday,
                "UTC",
                yesterday.plusHours(2),
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
                zonedNow,null,10
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
                yesterday,
                "UTC",
                yesterday.plusHours(2),
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
                zonedNow,null,10
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
                yesterday,
                null,
                yesterday.plusHours(2),
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
                zonedNow,null,10
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
                yesterday,
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
                zonedNow,null,10
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
                yesterday,
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
                zonedNow,null,10
            ));
        assertEquals("Transit time result is required", exception.getMessage());

    }

    private void setupValidCreateImportCommand() {
        when(createImportCommand.getTemperatureCategory()).thenReturn("FROZEN");
        when(createImportCommand.getTransitStartDateTime()).thenReturn(yesterday);
        when(createImportCommand.getTransitStartTimeZone()).thenReturn("UTC");
        when(createImportCommand.getTransitEndDateTime()).thenReturn(yesterday.plusHours(2));
        when(createImportCommand.getTransitEndTimeZone()).thenReturn("UTC");
        when(createImportCommand.getThermometerCode()).thenReturn("THERM123");
        when(createImportCommand.getTemperature()).thenReturn(BigDecimal.valueOf(20.5));
        when(createImportCommand.getLocationCode()).thenReturn("LOC123");
        when(createImportCommand.getComments()).thenReturn("Test comment");
        when(createImportCommand.getEmployeeId()).thenReturn("EMP123");
    }


    @Test
    void createImportItem_ValidData_CreatesItem() {

        when(configurationService.findByCodeAndTemperatureCategory(anyString(),anyString())).thenReturn(Mono.just(Mockito.mock(Product.class)));

        var finNumber = Mockito.mock(FinNumber.class);
        when(configurationService.findByFinNumber(anyString())).thenReturn(Mono.just(finNumber));

        Import importObj = Import.fromRepository(
            1L,
            "FROZEN",
            yesterday,
            "UTC",
            yesterday.plusHours(2),
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
            zonedNow,null,10
        );

        // Arrange
        Long importId = 2L;
        String unitNumber = "UNIT123";
        String productCode = "PROD123";
        String aboRh = "AP";
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(30);

        AddImportItemCommand command = new AddImportItemCommand(
            importId,
            unitNumber,
            productCode,
            aboRh,
            expirationDate,
            "SATISFACTORY",
            "LICENSED",
            "validEmployeeId"
        );

        ImportItem item = Mockito.mock(ImportItem.class);


        importItemMockedStatic.when(() -> ImportItem.create(any(), any(),Mockito.argThat(List::isEmpty))).thenReturn(item);

        // Act
        ImportItem result = importObj.createImportItem(command,configurationService,productConsequenceRepository);

        // Assert
        assertNotNull(result);

    }

    @Test
    void shouldNotCreateImportItem_WhenInvalidFinNumber() {

        when(configurationService.findByFinNumber(anyString())).thenReturn(Mono.empty());

        Import importObj = Import.fromRepository(1L,"FROZEN", yesterday,"UTC", yesterday.plusHours(2),"UTC",
            "2","ACCEPTABLE", BigDecimal.valueOf(20.5),            "THERM123",
            "ACCEPTABLE",
            "LOC123",
            "Test comment",
            "PENDING",
            "EMP123",
            zonedNow,
            zonedNow,null,10
        );

        // Arrange
        Long importId = 2L;
        String unitNumber = "UNIT123";
        String productCode = "PROD123";
        String aboRh = "AP";
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(30);

        AddImportItemCommand command = new AddImportItemCommand(
            importId,
            unitNumber,
            productCode,
            aboRh,
            expirationDate,
            "SATISFACTORY",
            "LICENSED",
            "validEmployeeId"
        );

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> importObj.createImportItem(command,configurationService,productConsequenceRepository));
        assertEquals("This FIN is not registered in the system", exception.getMessage());

    }

    @Test
    void shouldNotCreateImportItem_WhenInvalidProduct() {

        when(configurationService.findByCodeAndTemperatureCategory(anyString(),anyString())).thenReturn(Mono.empty());

        var finNumber = Mockito.mock(FinNumber.class);
        when(configurationService.findByFinNumber(anyString())).thenReturn(Mono.just(finNumber));



        Import importObj = Import.fromRepository(1L,"FROZEN", yesterday,"UTC", yesterday.plusHours(2),"UTC",
            "2","ACCEPTABLE", BigDecimal.valueOf(20.5),            "THERM123",
            "ACCEPTABLE",
            "LOC123",
            "Test comment",
            "PENDING",
            "EMP123",
            zonedNow,
            zonedNow,null,10
        );

        // Arrange
        Long importId = 2L;
        String unitNumber = "UNIT123";
        String productCode = "PROD123";
        String aboRh = "AP";
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(30);

        AddImportItemCommand command = new AddImportItemCommand(
            importId,
            unitNumber,
            productCode,
            aboRh,
            expirationDate,
            "SATISFACTORY",
            "LICENSED",
            "validEmployeeId"
        );

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> importObj.createImportItem(command,configurationService,productConsequenceRepository));
        assertEquals("Product type does not match", exception.getMessage());

    }

    @Test
    void createImportItem_ValidData_CreatesItemWithValidQuarantines() {

        when(configurationService.findByCodeAndTemperatureCategory(anyString(),anyString())).thenReturn(Mono.just(Mockito.mock(Product.class)));

        var finNumber = Mockito.mock(FinNumber.class);
        when(configurationService.findByFinNumber(anyString())).thenReturn(Mono.just(finNumber));

        ProductConsequence consequenceVisualInspection = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequenceVisualInspection.isAcceptable()).thenReturn(false);
        Mockito.when(consequenceVisualInspection.getConsequenceType()).thenReturn("VISUAL_INSPECTION_TYPE");
        Mockito.when(consequenceVisualInspection.getConsequenceReason()).thenReturn("VISUAL_INSPECTION_REASON");

        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(), Mockito.eq("VISUAL_INSPECTION"))).thenReturn(Flux.just(consequenceVisualInspection));


        ProductConsequence consequenceTemperature = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequenceTemperature.isAcceptable()).thenReturn(false);
        Mockito.when(consequenceTemperature.getConsequenceType()).thenReturn("TEMPERATURE_TYPE");
        Mockito.when(consequenceTemperature.getConsequenceReason()).thenReturn("TEMPERATURE_REASON");

        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(), Mockito.eq("TEMPERATURE"))).thenReturn(Flux.just(consequenceTemperature));

        ProductConsequence consequenceTransitTime = Mockito.mock(ProductConsequence.class);
        Mockito.when(consequenceTransitTime.isAcceptable()).thenReturn(false);
        Mockito.when(consequenceTransitTime.getConsequenceType()).thenReturn("TRANSIT_TIME_TYPE");
        Mockito.when(consequenceTransitTime.getConsequenceReason()).thenReturn("TRANSIT_TIME_REASON");

        when(productConsequenceRepository.findAllByProductCategoryAndResultProperty(any(), Mockito.eq("TRANSIT_TIME"))).thenReturn(Flux.just(consequenceTransitTime));

        Import importObj = Import.fromRepository(
            1L,
            "FROZEN",
            yesterday,
            "UTC",
            yesterday.plusHours(2),
            "UTC",
            "2",
            "UNACCEPTABLE",
            BigDecimal.valueOf(20.5),
            "THERM123",
            "UNACCEPTABLE",
            "LOC123",
            "Test comment",
            "PENDING",
            "EMP123",
            zonedNow,
            zonedNow,null,10
        );

        // Arrange
        Long importId = 2L;
        String unitNumber = "UNIT123";
        String productCode = "PROD123";
        String aboRh = "AP";
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(30);

        AddImportItemCommand command = new AddImportItemCommand(
            importId,
            unitNumber,
            productCode,
            aboRh,
            expirationDate,
            "UNSATISFACTORY",
            "LICENSED",
            "validEmployeeId"
        );

        ImportItem item = Mockito.mock(ImportItem.class);

        importItemMockedStatic.when(() -> ImportItem.create(any(AddImportItemCommand.class),any(ConfigurationService.class),Mockito.argThat(arg -> arg.stream()
            .anyMatch(itemArg -> itemArg.consequenceType().equals("TEMPERATURE_TYPE"))
            && arg.stream()
            .anyMatch(itemArg -> itemArg.consequenceType().equals("TRANSIT_TIME_TYPE"))
            && arg.stream()
            .anyMatch(itemArg -> itemArg.consequenceType().equals("VISUAL_INSPECTION_TYPE"))
        ))).thenReturn(item);

        // Act
        ImportItem result = importObj.createImportItem(command,configurationService,productConsequenceRepository);

        // Assert
        assertNotNull(result);

    }

    @Test
    void shouldNotCreateImportItem_WhenCommandIsNull() {

        Import importObj = Import.fromRepository(1L,"FROZEN", yesterday,"UTC", yesterday.plusHours(2),"UTC",
            "2","ACCEPTABLE", BigDecimal.valueOf(20.5),            "THERM123",
            "ACCEPTABLE",
            "LOC123",
            "Test comment",
            "PENDING",
            "EMP123",
            zonedNow,
            zonedNow,null,10
        );

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> importObj.createImportItem(null,configurationService,productConsequenceRepository));
        assertEquals("AddImportItemCommand is required", exception.getMessage());

    }

    @Test
    void shouldNotCreateImportItem_WhenConfigServiceIsNull() {

        Import importObj = Import.fromRepository(1L,"FROZEN", yesterday,"UTC", yesterday.plusHours(2),"UTC",
            "2","ACCEPTABLE", BigDecimal.valueOf(20.5),            "THERM123",
            "ACCEPTABLE",
            "LOC123",
            "Test comment",
            "PENDING",
            "EMP123",
            zonedNow,
            zonedNow,null,10
        );

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> importObj.createImportItem(Mockito.mock(AddImportItemCommand.class),null,productConsequenceRepository));
        assertEquals("Configuration Service is required", exception.getMessage());

    }

    @Test
    void shouldNotCreateImportItem_WhenMaxNumberOfProductsReached() {

        var item = Mockito.mock(ImportItem.class);

        Import importObj = Import.fromRepository(1L,"FROZEN", yesterday,"UTC", yesterday.plusHours(2),"UTC",
            "2","ACCEPTABLE", BigDecimal.valueOf(20.5),            "THERM123",
            "ACCEPTABLE",
            "LOC123",
            "Test comment",
            "PENDING",
            "EMP123",
            zonedNow,
            zonedNow,List.of(item),1
        );

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> importObj.createImportItem(Mockito.mock(AddImportItemCommand.class),configurationService,productConsequenceRepository));
        assertEquals("Max number of products reached", exception.getMessage());

    }

    @Test
    void shouldNotComplete_WhenIsCompleted() {

        var item = Mockito.mock(ImportItem.class);

        Import importObj = Import.fromRepository(1L,"FROZEN", yesterday,"UTC", yesterday.plusHours(2),"UTC",
            "2","ACCEPTABLE", BigDecimal.valueOf(20.5),            "THERM123",
            "ACCEPTABLE",
            "LOC123",
            "Test comment",
            "COMPLETED",
            "EMP123",
            zonedNow,
            zonedNow,List.of(item),1
        );

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> importObj.completeImport("EMP_1"));
        assertEquals("Import is already completed", exception.getMessage());

    }

    @Test
    void shouldNotComplete_WhenItemsIsEmpty() {

        Import importObj = Import.fromRepository(1L,"FROZEN", yesterday,"UTC", yesterday.plusHours(2),"UTC",
            "2","ACCEPTABLE", BigDecimal.valueOf(20.5),            "THERM123",
            "ACCEPTABLE",
            "LOC123",
            "Test comment",
            "PENDING",
            "EMP123",
            zonedNow,
            zonedNow, Collections.emptyList(),1
        );

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> importObj.completeImport("EMP_1"));
        assertEquals("Import must have at least one product in the batch", exception.getMessage());

    }

    @Test
    void shouldComplete_WhenIsPending() {

        var item = Mockito.mock(ImportItem.class);

        Import importObj = Import.fromRepository(1L,"FROZEN", yesterday,"UTC", yesterday.plusHours(2),"UTC",
            "2","ACCEPTABLE", BigDecimal.valueOf(20.5),            "THERM123",
            "ACCEPTABLE",
            "LOC123",
            "Test comment",
            "PENDING",
            "EMP123",
            zonedNow,
            zonedNow,List.of(item),1
        );

        // Assert
        var response = importObj.completeImport("EMP_1");
        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertNotNull(response.getCompleteDate());
        assertEquals("EMP_1", response.getCompleteEmployeeId());

    }
}






