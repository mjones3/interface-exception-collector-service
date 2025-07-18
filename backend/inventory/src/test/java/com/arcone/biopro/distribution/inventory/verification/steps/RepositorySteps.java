package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.History;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Volume;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.PropertyEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.PropertyEntityRepository;
import com.arcone.biopro.distribution.inventory.verification.common.ScenarioContext;
import com.arcone.biopro.distribution.inventory.verification.utils.InventoryUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.arcone.biopro.distribution.inventory.verification.steps.KafkaListenersSteps.*;
import static com.arcone.biopro.distribution.inventory.verification.steps.UseCaseSteps.quarantineReasonMap;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepositorySteps {
    private final InventoryEntityRepository inventoryEntityRepository;

    private final PropertyEntityRepository propertyEntityRepository;

    private final ScenarioContext scenarioContext;

    private final InventoryUtil inventoryUtil;

    public InventoryEntity getInventory(String unitNumber, String productCode) {
        return inventoryEntityRepository.findByUnitNumberAndProductCode(unitNumber, productCode).block();
    }

    public InventoryEntity getInventory(String unitNumber, String productCode, InventoryStatus status) {
        return inventoryEntityRepository.findByUnitNumberAndProductCodeAndInventoryStatus(unitNumber, productCode, status).block();
    }

    public InventoryEntity getStoredInventory(String unitNumber, String productCode, InventoryStatus status) {

        return inventoryEntityRepository.findByUnitNumberAndProductCodeAndInventoryStatus(unitNumber, productCode, status).block();
    }

    @Then("The inventory status is {string}")
    public void theInventoryIsCreatedCorrectly(String status) {
        InventoryEntity inventory = getInventory(scenarioContext.getUnitNumber(), scenarioContext.getProductCode(), InventoryStatus.valueOf(status));
        assertNotNull(inventory);
        assertEquals(scenarioContext.getProductCode(), inventory.getProductCode());
        assertEquals(scenarioContext.getUnitNumber(), inventory.getUnitNumber());
        assertEquals(status, inventory.getInventoryStatus().name());

        if (EVENT_QUARANTINE_UPDATED.equals(scenarioContext.getEvent())) {
            assertEquals("UNDER_INVESTIGATION", inventory.getQuarantines().getFirst().reason());
        }
    }

    @Then("The inventory status has quarantine")
    public void theInventoryIsCreatedCorrectly() {
        InventoryEntity inventory = getInventory(scenarioContext.getUnitNumber(), scenarioContext.getProductCode(), InventoryStatus.AVAILABLE);
        assertNotNull(inventory);
        assertEquals(scenarioContext.getProductCode(), inventory.getProductCode());
        assertEquals(scenarioContext.getUnitNumber(), inventory.getUnitNumber());
        assertFalse(inventory.getQuarantines().isEmpty());

        if (EVENT_QUARANTINE_UPDATED.equals(scenarioContext.getEvent())) {
            assertEquals("UNDER_INVESTIGATION", inventory.getQuarantines().getFirst().reason());
        }
    }

    @And("For unit number {string} and product code {string} the device stored is {string} and the storage location is {string}")
    public void forUnitNumberAndProductCodeTheDeviceStoredIsAndTheStorageLocationIs(String unitNumber, String productCode, String deviceStorage, String storageLocation) {
        InventoryEntity inventory = getStoredInventory(unitNumber, productCode, InventoryStatus.AVAILABLE);
        assertNotNull(inventory);
        assertEquals(deviceStorage, inventory.getDeviceStored());
        assertEquals(storageLocation, inventory.getStorageLocation());
    }

    @Given("I have a unit number {string} with product {string} that is {string}")
    public void iAmListeningEventForUnitNumber(String unitNumber, String productCode, String status) {
        scenarioContext.setUnitNumber(unitNumber);
        scenarioContext.setProductCode(productCode);
        InventoryEntity inventoryEntity = inventoryUtil.newInventoryEntity(unitNumber, productCode, InventoryStatus.valueOf(status));
        inventoryUtil.saveInventory(inventoryEntity);
    }

    @Then("I verify the quarantine reason {string} with id {string} is found {string} for unit number {string} and product {string}")
    public void iVerifyTheQuarantineReasonIsInactiveForUnitNumberAndProduct(String quarantineReason, String quarantineReasonId, String isFound, String unitNumber, String productCode) {
        InventoryEntity inventory = getInventory(unitNumber, productCode, InventoryStatus.AVAILABLE);

        assert inventory != null;
        List<Quarantine> productsReason = inventory.getQuarantines().stream().filter(q -> quarantineReasonMap.get(quarantineReason)
            .equals(q.reason()) && q.externId().equals(Long.parseLong(quarantineReasonId))).toList();

        assertEquals(Boolean.valueOf(isFound), !productsReason.isEmpty());
    }

    @Given("I have a Discarded Product in Inventory with unit number {string} and previous status {string}")
    public void iHaveADiscardedProductInInventoryWithPreviousStatus(String unitNumber, String previousStatus) {
        List<Quarantine> quarantines;
        List<History> histories;

        scenarioContext.setUnitNumber(unitNumber);
        scenarioContext.setProductCode("E0869VA0");

        histories = List.of(new History(InventoryStatus.valueOf(previousStatus), null, null));

        if (InventoryStatus.AVAILABLE.toString().equals(previousStatus)) {
            histories = List.of(new History(InventoryStatus.valueOf(previousStatus), null, null));
        }

        quarantines = List.of(new Quarantine(1L, "OTHER", "a comment"));

        var inventory = inventoryUtil.newInventoryEntity(scenarioContext.getUnitNumber(), scenarioContext.getProductCode(), InventoryStatus.DISCARDED);
        inventory.setQuarantines(quarantines);
        inventory.setHistories(histories);
        inventoryUtil.saveInventory(inventory);
    }

    @And("the expected fields for {string} are stored")
    public void theExpectedFieldsForArePresent(String event) {
        InventoryEntity inventory = getInventory(scenarioContext.getUnitNumber(), scenarioContext.getProductCode());
        assertNotNull(inventory, "Inventory not found in the database");

        switch (event) {
            case EVENT_LABEL_APPLIED:
                assertThat(inventory)
                    .hasNoNullFieldsOrPropertiesExcept(
                        "histories",
                        "deviceStored",
                        "comments",
                        "storageLocation",
                        "quarantines",
                        "statusReason"
                    )
                    .hasFieldOrPropertyWithValue("isLabeled", true);
                break;
            case EVENT_SHIPMENT_COMPLETED:
                break;
            case EVENT_PRODUCT_STORED:
                break;
            case EVENT_PRODUCT_DISCARDED:
                break;
            case EVENT_PRODUCT_QUARANTINED:
                break;
            case EVENT_QUARANTINE_UPDATED:
                break;
            case EVENT_QUARANTINE_REMOVED:
                break;
            case EVENT_PRODUCT_RECOVERED:
                break;
            default:
                fail("Unknown event: " + event);
        }
    }

    @Given("I have the following inventories:")
    public void iHaveTheFollowingInventories(DataTable dataTable) {
        List<Map<String, String>> inventories = dataTable.asMaps(String.class, String.class);
        List<String> headers = dataTable.row(0);
        for (Map<String, String> inventory : inventories) {
            String unitNumber = inventory.get("Unit Number");
            String productCode = inventory.get("Product Code");

            InventoryStatus status = InventoryStatus.AVAILABLE;
            if (headers.contains("Status")) {
                status = InventoryStatus.valueOf(inventory.get("Status"));
            }
            InventoryEntity inventoryEntity = inventoryUtil.newInventoryEntity(unitNumber, productCode, status);

            if (headers.contains("ABO/RH")) {
                String aboRh = inventory.get("ABO/RH");
                inventoryEntity.setAboRh(AboRhType.valueOf(aboRh));
            }

            if (headers.contains("Expiration Date")) {
                String expirationDate = inventory.get("Expiration Date");
                inventoryEntity.setExpirationDate(LocalDateTime.parse(expirationDate));
            }

            if (headers.contains("Location")) {
                String location = inventory.get("Location");
                if (location != null && !location.isEmpty()) {
                    inventoryEntity.setInventoryLocation(location);
                }
            }

            if (headers.contains("Collection Location")) {
                String location = inventory.get("Collection Location");
                if (StringUtils.isNotBlank(location)) {
                    inventoryEntity.setCollectionLocation(location);
                }
            }

            if(headers.contains("Collection TimeZone")){
                String timeZone = inventory.get("Collection TimeZone");
                if(StringUtils.isNotBlank(timeZone)){
                    inventoryEntity.setCollectionTimeZone(timeZone);
                }
            }

            if(headers.contains("Expiration Timezone")){
                String timeZone = inventory.get("Expiration Timezone");
                if(StringUtils.isNotBlank(timeZone)){
                    inventoryEntity.setExpirationTimeZone(timeZone);
                }
            }

            if (headers.contains("Is licensed")) {
                var field = inventory.get("Is licensed");
                switch (field) {
                    case "MISSING":
                        inventoryEntity.setIsLicensed(null);
                    case "YES":
                        inventoryEntity.setIsLicensed(true);
                    case "NO":
                        inventoryEntity.setIsLicensed(false);
                }
            }

            if(headers.contains("Quarantine Reasons") && inventory.get("Quarantine Reasons")!=null ){
                String quarantineReasons = inventory.get("Quarantine Reasons");
                if(quarantineReasons.equalsIgnoreCase("EMPTY")) {
                    inventoryEntity.setQuarantines(new ArrayList<>());
                } else {
                    String comments = inventory.get("Comments");
                    List<Quarantine> quarantines = Arrays.stream(quarantineReasons.split(",")).map(String::trim).map(reason -> new Quarantine(1L, reason, comments)).toList();
                    inventoryEntity.setQuarantines(quarantines);
                }
            }

            if(headers.contains("Discard Reason") && inventory.get("Discard Reason")!=null){
                String discardReason = inventory.get("Discard Reason");
                inventoryEntity.setStatusReason(discardReason);
                inventoryEntity.setComments(inventory.get("Comments"));
            }

            if (headers.contains("Unsuitable Reason") && inventory.get("Unsuitable Reason")!=null) {
                inventoryEntity.setUnsuitableReason(inventory.get("Unsuitable Reason"));
            }

            if(headers.contains("Is Labeled")){
                inventoryEntity.setIsLabeled(Boolean.valueOf(inventory.get("Is Labeled")));
            }

            if(headers.contains("Carton Number")){
                inventoryEntity.setCartonNumber(inventory.get("Carton Number"));
            }

            if(headers.contains("Expires In Days")) {
                int expiresInDays = Integer.parseInt(inventory.get("Expires In Days"));
                inventoryEntity.setExpirationDate(LocalDateTime.now().plusDays(expiresInDays));
            }

            if(headers.contains("Expires In")) {
                int expiresIn = Integer.parseInt(inventory.get("Expires In").split(" ")[0]);
                String type = inventory.get("Expires In").split(" ")[1];
                String expirationTimezone = inventory.get("Expiration Timezone");
                String timezoneRelevant = inventory.get("Timezone Relevant");

                LocalDateTime expirationDate;
                if ("Y".equals(timezoneRelevant) && StringUtils.isNotBlank(expirationTimezone)) {
                    // For timezone-relevant products, calculate expiration in the specified timezone
                    java.time.ZonedDateTime zonedExpiration = java.time.ZonedDateTime.now(java.time.ZoneId.of(expirationTimezone));
                    if (type.equals("Hours")) {
                        zonedExpiration = zonedExpiration.plusHours(expiresIn);
                    } else {
                        zonedExpiration = zonedExpiration.plusDays(expiresIn);
                    }
                    expirationDate = zonedExpiration.toLocalDateTime();
                } else {
                    // For non-timezone-relevant products, use local time
                    if (type.equals("Hours")) {
                        expirationDate = LocalDateTime.now().plusHours(expiresIn);
                    } else {
                        expirationDate = LocalDateTime.now().plusDays(expiresIn);
                    }
                }
                inventoryEntity.setExpirationDate(expirationDate);
            }

            inventoryEntity.setVolumes(new ArrayList<>());
            if(headers.contains("Volumes") && inventory.get("Volumes") != null){
                var volumes = inventory.get("Volumes").split(",");
                for(String volume: volumes) {
                    var volumeFields = volume.split("-");
                    inventoryEntity.getVolumes().add(new Volume(volumeFields[0].trim().toUpperCase(), Integer.parseInt(volumeFields[1].trim()), "MILLILITERS"));
                }
            }

            inventoryEntity.setTemperatureCategory(headers.contains("Temperature Category") ? inventory.get("Temperature Category") : "FROZEN");

            if(headers.contains("Storage Location")) {
                inventoryEntity.setStorageLocation(inventory.get("Storage Location"));
            }

            if(headers.contains("Device Stored")) {
                inventoryEntity.setDeviceStored(inventory.get("Device Stored"));
            }

            inventoryUtil.saveInventory(inventoryEntity);

            if("Y".equals(inventory.get("Timezone Relevant"))) {
                PropertyEntity property = PropertyEntity.builder()
                    .id(UUID.randomUUID())
                    .key("TIMEZONE_RELEVANT")
                    .value("Y")
                    .inventoryId(inventoryEntity.getId())
                    .build();

                propertyEntityRepository.save(property).block();
            }

            if (inventory.get("Properties") != null){
                List<String> properties = Arrays.stream(inventory.get("Properties").split(",")).map(String::trim).toList();
                for (String property : properties) {
                    String[] propertyFields = property.split("=");
                    PropertyEntity propertyEntity = PropertyEntity.builder()
                        .id(UUID.randomUUID())
                        .key(propertyFields[0].trim())
                        .value(propertyFields[1].trim())
                        .inventoryId(inventoryEntity.getId())
                        .build();

                    propertyEntityRepository.save(propertyEntity).block();
                }
            }
        }
    }

    @Then("the inventory volume should be updated as follows:")
    @Then("the parent inventory statuses should be updated as follows:")
    @Then("the inventory statuses should be updated as follows:")
    @Then("the inventories should be:")
    public void theInventoryStatusesShouldBeUpdatedAsFollows(DataTable dataTable) {
        List<Map<String, String>> table = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : table) {
            String unitNumber = row.get("Unit Number");
            String productCode = row.get("Product Code");
            String expectedStatus = row.get("Status");
            var inventoryEntity = this.getInventory(unitNumber, productCode);
            if (row.containsKey("Is Labeled")) {
                assertEquals(Boolean.valueOf(row.get("Is Labeled")), inventoryEntity.getIsLabeled());
            }

            if (row.containsKey("Is licensed")) {
                assertEquals(Boolean.valueOf(row.get("Is licensed")), inventoryEntity.getIsLicensed());
            }

            if (row.containsKey("Temperature Category") && Strings.isNotBlank(row.get("Temperature Category"))) {
                assertEquals(row.get("Temperature Category"), inventoryEntity.getTemperatureCategory());
            }

            if (row.containsKey("Unsuitable reason")) {
                if (row.get("Unsuitable reason").equalsIgnoreCase("Empty")) {
                    assertNull(inventoryEntity.getUnsuitableReason());
                } else {
                    assertEquals(row.get("Unsuitable reason"), inventoryEntity.getUnsuitableReason());
                }
            }

            List<Volume> volumes = inventoryEntity.getVolumes();
            if(row.containsKey("Volume")){
                assertTrue(volumes.stream()
                    .filter(v -> v.type().equals("volume"))
                    .findFirst()
                    .map(v -> Objects.equals(v.value(), Integer.valueOf(row.get("Volume"))))
                    .orElse(false));
            }

            if(row.containsKey("Weight")){
                assertEquals(Integer.valueOf(row.get("Weight")), inventoryEntity.getWeight());
            }

            if(row.containsKey("Modification Location")){
                assertEquals(row.get("Modification Location"), inventoryEntity.getModificationLocation());
            }

            if(row.containsKey("Anticoagulant Volume")){
                assertTrue(volumes.stream()
                    .filter(v -> v.type().equals("anticoagulantVolume"))
                    .findFirst()
                    .map(v -> Objects.equals(v.value(), Integer.valueOf(row.get("Anticoagulant Volume"))))
                    .orElse(false));
            }

            if(row.containsKey("Location")){
                assertEquals(row.get("Location"), inventoryEntity.getInventoryLocation());
            }

            if(row.containsKey("Collection Location")){
                assertEquals(row.get("Collection Location"), inventoryEntity.getCollectionLocation());
            }

            if(row.containsKey("Checkin Location")){
                assertEquals(row.get("Checkin Location"), inventoryEntity.getInventoryLocation());
            }

            if(row.containsKey("Collection TimeZone")){
                assertEquals(row.get("Collection TimeZone"), inventoryEntity.getCollectionTimeZone());
            }

            if(row.containsKey("Shipped Location")){
                assertEquals(row.get("Shipped Location"), inventoryEntity.getShippedLocation());
            }

            if(row.containsKey("Expiration Date")){
                assertNotNull(inventoryEntity.getExpirationDate());
                assertEquals(row.get("Expiration Date"), inventoryEntity.getExpirationDate().toString());
            }

            if(row.containsKey("Modification Date")) {
                assertEquals(row.get("Modification Date"),inventoryEntity.getProductModificationDate().withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
            }

            if(row.containsKey("Expiration Time Zone")) {
                assertEquals(row.get("Expiration Time Zone"), inventoryEntity.getExpirationTimeZone());
            }

            if(row.get("Properties") != null) {
                var key = row.get("Properties").split("=")[0];
                var value = row.get("Properties").split("=")[1];

                List<PropertyEntity> propertyEntities = propertyEntityRepository.findByInventoryId(inventoryEntity.getId())
                    .filter(propertyEntity -> propertyEntity.getKey().equals(key))
                    .collectList()
                    .block();

                assert propertyEntities != null;
                assertFalse(propertyEntities.isEmpty());
                PropertyEntity propertyEntity = propertyEntities.getFirst();
                assertEquals(value, propertyEntity.getValue());
            }

            assertEquals(expectedStatus, inventoryEntity.getInventoryStatus().name());
        }
    }

    @Given("I have the following units of products in inventory")
    public void iHaveTheFollowingUnitsOfProductsInInventory(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps();

        for (Map<String, String> product : products) {
            String unitNumber = product.get("Unit Number");
            int units = Integer.parseInt(product.get("Units"));
            String family = product.get("Family");
            String abORh = product.get("ABORh");
            String location = product.get("Location");
            String expiresIn = product.get("Expires In");

            String productCode = "E0869V00";
            if(product.containsKey("Product Code")){
                productCode = product.get("Product Code");
            }

            InventoryStatus status = InventoryStatus.AVAILABLE;
            if(product.containsKey("Status")){
                status = InventoryStatus.valueOf(product.get("Status"));
            }
            String temperatureCategory = "FROZEN";
            if(product.containsKey("Temperature Category")){
                temperatureCategory = product.get("Temperature Category");
            }

            inventoryUtil.createMultipleProducts(unitNumber, productCode, units, family, abORh, location, expiresIn, status, temperatureCategory, product.get("Expiration Timezone"), product.get("Timezone Relevant"));
        }
    }



    @Then("the properties should be added:")
    public void thePropertiesShouldBeAdded(DataTable dataTable) {
        List<Map<String, String>> table = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : table) {
            InventoryEntity inventory = inventoryEntityRepository.findByUnitNumberAndProductCode(row.get("Unit Number"), row.get("Product Code")).block();
            assert inventory != null;
            List<PropertyEntity> properties = propertyEntityRepository.findByInventoryId(inventory.getId()).collectList().block();
            assert properties != null;
            properties.stream().map(PropertyEntity::getKey).forEach(p -> assertTrue(row.get("Properties").contains(p)));
        }
    }
}
