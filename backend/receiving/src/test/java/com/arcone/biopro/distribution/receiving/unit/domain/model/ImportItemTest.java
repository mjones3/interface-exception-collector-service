package com.arcone.biopro.distribution.receiving.unit.domain.model;

import com.arcone.biopro.distribution.receiving.domain.model.AddImportItemCommand;
import com.arcone.biopro.distribution.receiving.domain.model.ImportItem;
import com.arcone.biopro.distribution.receiving.domain.model.Product;
import com.arcone.biopro.distribution.receiving.domain.model.vo.AboRh;
import com.arcone.biopro.distribution.receiving.domain.model.vo.ImportItemConsequence;
import com.arcone.biopro.distribution.receiving.domain.service.ConfigurationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ImportItemTest {

     @Mock
    private ConfigurationService configurationService;

    @Test
    void create_WithValidCommand_ShouldCreateImportItem() {
        // Arrange
        AddImportItemCommand command = createValidCommand();
        List<ImportItemConsequence> consequences = new ArrayList<>();
        Product product = createProductDetails();

        Mockito.when(configurationService.findProductByCode(Mockito.anyString())).thenReturn(Mono.just(product));

        // Act
        ImportItem result = ImportItem.create(command, configurationService, consequences);

        // Assert
        assertNotNull(result);
        assertEquals(command.getImportId(), result.getImportId());
        assertEquals(command.getVisualInspection(), result.getVisualInspection());
        assertEquals(command.getLicenseStatus(), result.getLicenseStatus());
        assertEquals(command.getUnitNumber(), result.getUnitNumber());
        assertEquals(command.getProductCode(), result.getProductCode());
        assertEquals(command.getAboRh(), result.getAboRh());
        assertEquals(command.getExpirationDate(), result.getExpirationDate());
        assertEquals(product.getProductFamily(), result.getProductFamily());
        assertEquals(product.getShortDescription(), result.getProductDescription());
        assertEquals(command.getEmployeeId(), result.getEmployeeId());
        assertNotNull(result.getCreateDate());
        assertNotNull(result.getModificationDate());
    }

    @Test
    void fromRepository_WithValidData_ShouldCreateImportItem() {
        // Arrange
        Long id = 1L;
        Long importId = 2L;
        String unitNumber = "UNIT123";
        String productCode = "PROD123";
        String aboRh = "AP";
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(30);
        String productFamily = "Family1";
        String productDescription = "Description";
        ZonedDateTime createDate = ZonedDateTime.now();
        ZonedDateTime modificationDate = ZonedDateTime.now();
        String employeeId = "EMP123";
        Map<String, String> properties = new HashMap<>();
        properties.put("VISUAL_INSPECTION","SATISFACTORY");
        properties.put("LICENSE_STATUS","LICENSED");
        List<ImportItemConsequence> consequences = new ArrayList<>();

        // Act
        ImportItem result = ImportItem.fromRepository(
            id, importId, unitNumber, productCode, aboRh, expirationDate,
            productFamily, productDescription, createDate, modificationDate,
            employeeId, properties, consequences
        );

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(importId, result.getImportId());
        assertEquals(unitNumber, result.getUnitNumber());
        assertEquals(productCode, result.getProductCode());
        assertEquals(AboRh.getInstance(aboRh), result.getAboRh());
        assertEquals(expirationDate, result.getExpirationDate());
        assertEquals(productFamily, result.getProductFamily());
        assertEquals(productDescription, result.getProductDescription());
        assertEquals(createDate, result.getCreateDate());
        assertEquals(modificationDate, result.getModificationDate());
        assertEquals(employeeId, result.getEmployeeId());
        assertEquals(properties, result.getProperties());
        assertEquals(consequences, result.getConsequences());
    }

    @Test
    void checkValid_WithMissingRequiredFields_ShouldThrowExceptions() {
        // Testing all required fields

        assertThrows(IllegalArgumentException.class, () -> ImportItem.fromRepository(
                1L, null, "UNIT123", "PROD123", "AP", LocalDateTime.now().plusDays(30),
                "Family1", "Description", ZonedDateTime.now(), ZonedDateTime.now(),
                "EMP123", new HashMap<>(), new ArrayList<>()
            ),
            "Import Id is required");
    }


    private AddImportItemCommand createValidCommand() {
       return new AddImportItemCommand(
            1L,
            "UNIT123",
            "PROD123",
            "AP",
            LocalDateTime.now().plusDays(30),
            "SATISFACTORY",
            "LICENSED",
            "validEmployeeId"
        );
    }

    private Product createProductDetails() {
        String id = "123";
        String productCode = "PRD001";
        String shortDescription = "Test Product";
        String productFamily = "Test Family";
        boolean active = true;
        ZonedDateTime modificationDate = ZonedDateTime.now();

        return new Product(id, productCode, shortDescription,
            productFamily, active, ZonedDateTime.now(), modificationDate);


    }

    @Test
    void create_WithNullCommand_ShouldThrowNullPointerException() {
        assertThrows(IllegalArgumentException.class, () ->
            ImportItem.create(null, configurationService, new ArrayList<>()));
    }

    @Test
    void create_WithNullConfigurationService_ShouldThrowNullPointerException() {
        AddImportItemCommand command = createValidCommand();
        assertThrows(IllegalArgumentException.class, () ->
            ImportItem.create(command, null, new ArrayList<>()));
    }

    @Test
    void shouldBeQuarantined() {
        List<ImportItemConsequence> consequences = new ArrayList<>();
        consequences.add(ImportItemConsequence.builder().build());

        Map<String, String> properties = new HashMap<>();
        properties.put("VISUAL_INSPECTION","SATISFACTORY");
        properties.put("LICENSE_STATUS","LICENSED");


        ImportItem importItem = ImportItem.fromRepository(1L, 1L, "UNIT123", "PROD123", "AP", LocalDateTime.now().plusDays(30),
            "Family1", "Description", ZonedDateTime.now(), ZonedDateTime.now(),
            "EMP123", properties, consequences);

        assertTrue(importItem.isQuarantined());
    }

    @Test
    void shouldNotBeQuarantined() {

        Map<String, String> properties = new HashMap<>();
        properties.put("VISUAL_INSPECTION","SATISFACTORY");
        properties.put("LICENSE_STATUS","LICENSED");


        ImportItem importItem = ImportItem.fromRepository(1L, 1L, "UNIT123", "PROD123", "AP", LocalDateTime.now().plusDays(30),
            "Family1", "Description", ZonedDateTime.now(), ZonedDateTime.now(),
            "EMP123", properties, null);

        assertFalse(importItem.isQuarantined());
    }


}
