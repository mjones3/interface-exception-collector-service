package com.arcone.biopro.distribution.inventory.application.mapper;

import com.arcone.biopro.distribution.inventory.application.dto.*;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhCriteria;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.MessageType;
import com.arcone.biopro.distribution.inventory.domain.model.vo.NotificationMessage;
import com.arcone.biopro.distribution.inventory.domain.service.TextConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class InventoryOutputMapperTest {

    @Mock
    private TextConfigService service;

    private InventoryOutputMapper mapper;

    private InventoryAggregate inventoryAggregate;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.openMocks(this);
        mapper = Mappers.getMapper(InventoryOutputMapper.class);
        mapper.setTextConfigService(service);
        String jsonContent = FileUtils.readFileToString(
            new File("src/test/resources/json/inventory-aggregate.json"),
            StandardCharsets.UTF_8
        );
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        inventoryAggregate = objectMapper.readValue(jsonContent, InventoryAggregate.class);
    }

    @Test
    @DisplayName("should return output with unitNumber and productCode")
    public void testOutputInventory(){
        InventoryOutput outPut = mapper.toOutput(inventoryAggregate.getInventory());
        assertEquals(inventoryAggregate.getInventory().getUnitNumber().value(), outPut.unitNumber());
        assertEquals(inventoryAggregate.getInventory().getProductCode().value(), outPut.productCode());
    }

    @ParameterizedTest
    @DisplayName("should return correct storageLocation based on deviceStored and storageLocation values")
    @CsvSource({
        "'REFRIGERATOR 1', 'SHELF 1, BIN2', 'REFRIGERATOR 1 SHELF 1, BIN2'",
        "REFRIGERATOR 1, , REFRIGERATOR 1",
        ", SHELF 1, ",
        ", , "
    })
    void testStorageLocation(String deviceStored, String storageLocation, String expected) {
        Inventory inventory = new Inventory();
        inventory.setDeviceStored(deviceStored);
        inventory.setStorageLocation(storageLocation);
        Product product = mapper.toOutput(InventoryAggregate.builder().inventory(inventory).build());
        assertEquals(expected, product.storageLocation());
    }

    @Test
    @DisplayName("should return output with InventoryFamily")
    public void testOutputFamily(){
        var unitNumber = inventoryAggregate.getInventory().getUnitNumber().value();
        var productCode = inventoryAggregate.getInventory().getProductCode().value();
        var storageLocation = inventoryAggregate.getInventory().getDeviceStored() + " " + inventoryAggregate.getInventory().getStorageLocation();
        var aboRh = inventoryAggregate.getInventory().getAboRh();
        InventoryFamily family = mapper.toOutput("a-product-family", AboRhCriteria.A, 1L, List.of(inventoryAggregate));
        assertEquals("a-product-family", family.productFamily());
        assertEquals(AboRhCriteria.A, family.aboRh());
        assertEquals(1L, family.quantityAvailable());
        assertEquals(new Product(unitNumber, productCode, storageLocation, aboRh), family.shortDateProducts().getFirst());
    }

    @Test
    @DisplayName("should return output with GetAllAvailableInventoriesOutput")
    public void testOutputGetAllAvailableInventories(){
        var unitNumber = inventoryAggregate.getInventory().getUnitNumber().value();
        var productCode = inventoryAggregate.getInventory().getProductCode().value();
        var storageLocation = inventoryAggregate.getInventory().getStorageLocation();
        var aboRh = inventoryAggregate.getInventory().getAboRh();
        var families = List.of(new InventoryFamily("a-product-family", AboRhCriteria.A, 1L, List.of(new Product(unitNumber, productCode, storageLocation, aboRh))));
        GetAllAvailableInventoriesOutput output = mapper.toOutput("a-location", families);
        assertEquals("a-location", output.location());
        assertEquals(families, output.inventories());
    }

    @Test
    @DisplayName("should return output with Product")
    public void testOutputProduct(){
        Product output = mapper.toOutput(inventoryAggregate);
        assertEquals(inventoryAggregate.getInventory().getUnitNumber().value(), output.unitNumber());
        assertEquals(inventoryAggregate.getInventory().getProductCode().value(), output.productCode());
        assertEquals(inventoryAggregate.getInventory().getDeviceStored() + " " + inventoryAggregate.getInventory().getStorageLocation(), output.storageLocation());
        assertEquals(inventoryAggregate.getInventory().getAboRh(), output.aboRh());
    }

    @Test
    @DisplayName("should return output NotificationMessage")
    public void testOutputNotificationMessage(){
        NotificationMessage message = inventoryAggregate.getNotificationMessages().getFirst();
        when(service.getText(message.name(), message.message())).thenReturn("This product is currently in quarantine and needs to be returned to storage.");
        message.details().forEach(detail -> when(service.getText(message.name() + "_DETAIL", detail)).thenReturn(detail));
        NotificationMessage output = mapper.toOutput(message);
        assertEquals("This product is currently in quarantine and needs to be returned to storage.", output.message());
        assertEquals("INVENTORY_IS_QUARANTINED", output.name());
        assertEquals("INFO", output.type());
        assertEquals(4, output.code());
        assertNull(output.reason());
        assertEquals("BACK_TO_STORAGE", output.action());
        assertEquals(List.of(
            "ABS_POSITIVE",
            "BCA_UNIT_NEEDED",
            "CCP_ELIGIBLE",
            "FAILED_VISUAL_INSPECTION",
            "OTHER_SEE_COMMENTS: The blood bag is in quarantine for safety testing to ensure it’s free from contaminants and safe for transfusion."),
            output.details());
    }

    @ParameterizedTest
    @EnumSource(MessageType.class)  // Runs the test for each MessageType enum value
    @DisplayName("should return output ValidateInventoryOutput for each MessageType")
    public void testOutputValidateInventoryOutput(MessageType messageType) {
        NotificationMessage message = inventoryAggregate.getNotificationMessages().getFirst();
        when(service.getText(message.message(), message.name())).thenReturn(messageType.name());
        message.details().forEach(detail -> when(service.getText(detail, message.name())).thenReturn(detail));
        ValidateInventoryOutput output = mapper.toOutput(messageType);
        assertNull(output.inventoryOutput());
        assertEquals(List.of(
            new NotificationMessage(
                messageType.name(),
                messageType.getCode(),
                messageType.getCode().equals(4) ? "INVENTORY_IS_QUARANTINED" : null,
                messageType.getType().name(),
                messageType.getAction().name(),
                null,
                null
            )
        ), output.notificationMessages());
    }

    @Test
    @DisplayName("should map InventoryInput to InventoryAggregate")
    public void testToAggregate() {
        InventoryInput input = InventoryInput.builder()
            .unitNumber("W012345678903")
            .productCode("E0869V02")
            .shortDescription("Test product")
            .expirationDate(LocalDateTime.now().plusDays(30))
            .collectionDate(ZonedDateTime.now().minusHours(2))
            .isLicensed(true)
            .weight(5)
            .location("Storage A")
            .productFamily("Family X")
            .aboRh(AboRhType.ABN)
            .build();

        InventoryAggregate aggregate = mapper.toAggregate(input);

        assertNotNull(aggregate);
        assertEquals(input.unitNumber(), aggregate.getInventory().getUnitNumber().value());
        assertEquals(input.productCode(), aggregate.getInventory().getProductCode().value());
        assertEquals(input.shortDescription(), aggregate.getInventory().getShortDescription());
        assertEquals(input.expirationDate(), aggregate.getInventory().getExpirationDate());
        assertEquals(input.collectionDate(), aggregate.getInventory().getCollectionDate());
        assertEquals(input.isLicensed(), aggregate.getInventory().getIsLicensed());
        assertEquals(input.weight(), aggregate.getInventory().getWeight());
        assertEquals(input.location(), aggregate.getInventory().getLocation());
        assertEquals(input.productFamily(), aggregate.getInventory().getProductFamily());
        assertEquals(input.aboRh(), aggregate.getInventory().getAboRh());

        assertNotNull(aggregate.getInventory().getId());
        assertEquals(InventoryStatus.AVAILABLE, aggregate.getInventory().getInventoryStatus());
    }


}
