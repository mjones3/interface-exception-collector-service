package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.History;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Volume;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import com.arcone.biopro.distribution.inventory.verification.common.ScenarioContext;
import com.arcone.biopro.distribution.inventory.verification.utils.InventoryUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
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
                    inventoryEntity.setLocation(location);
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

            inventoryEntity.setVolumes(new ArrayList<>());
            if(headers.contains("Volumes") && inventory.get("Volumes") != null){
                var volumes = inventory.get("Volumes").split(",");
                for(String volume: volumes) {
                    var volumeFields = volume.split("-");
                    inventoryEntity.getVolumes().add(new Volume(volumeFields[0].trim().toUpperCase(), Integer.parseInt(volumeFields[1].trim()), "MILLILITERS"));
                }
            }

            inventoryEntity.setTemperatureCategory(headers.contains("Temperature Category") ? inventory.get("Temperature Category") : "FROZEN");

            inventoryUtil.saveInventory(inventoryEntity);
        }
    }

    @Then("the inventory volume should be updated as follows:")
    @Then("the parent inventory statuses should be updated as follows:")
    @Then("the inventory statuses should be updated as follows:")
    @Then("the inventories should be:")
    public void theInventoryStatusesShouldBeUpdatedAsFollows(DataTable dataTable) {
        List<Map<String, String>> inventories = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> inventory : inventories) {
            String unitNumber = inventory.get("Unit Number");
            String productCode = inventory.get("Product Code");
            String expectedStatus = inventory.get("Status");
            var inventoryEntity = this.getInventory(unitNumber, productCode);
            if (inventory.containsKey("Is Labeled")) {
                assertEquals(Boolean.valueOf(inventory.get("Is Labeled")), inventoryEntity.getIsLabeled());
            }
            if (inventory.containsKey("Is licensed")) {
                assertEquals(Boolean.valueOf(inventory.get("Is licensed")), inventoryEntity.getIsLicensed());
            }
            if (inventory.containsKey("Temperature Category") && Strings.isNotBlank(inventory.get("Temperature Category"))) {
                assertEquals(inventory.get("Temperature Category"), inventoryEntity.getTemperatureCategory());
            }
            if (inventory.containsKey("Unsuitable reason")) {
                if (inventory.get("Unsuitable reason").equalsIgnoreCase("Empty")) {
                    assertNull(inventoryEntity.getUnsuitableReason());
                } else {
                    assertEquals(inventory.get("Unsuitable reason"), inventoryEntity.getUnsuitableReason());
                }
            }
            List<Volume> volumes = inventoryEntity.getVolumes();
            if(inventory.containsKey("Volume")){
                assertTrue(volumes.stream()
                    .filter(v -> v.type().equals("volume"))
                    .findFirst()
                    .map(v -> Objects.equals(v.value(), Integer.valueOf(inventory.get("Volume"))))
                    .orElse(false));
            }
            if(inventory.containsKey("Anticoagulant Volume")){
                assertTrue(volumes.stream()
                    .filter(v -> v.type().equals("anticoagulantVolume"))
                    .findFirst()
                    .map(v -> Objects.equals(v.value(), Integer.valueOf(inventory.get("Anticoagulant Volume"))))
                    .orElse(false));
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
            int expiresInDays = Integer.parseInt(product.get("Expires In Days"));

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

            createMultipleProducts(unitNumber, productCode, units, family, abORh, location, expiresInDays, status, temperatureCategory);
        }
    }

    private void createInventory(String unitNumber, String productCode, String productFamily, AboRhType aboRhType, String location, Integer daysToExpire, InventoryStatus status, String temperatureCategory, Boolean isLabeled, String statusReason, String comments) {
        var inventory = inventoryUtil.newInventoryEntity(unitNumber, productCode, status);
        inventory.setLocation(location);
        inventory.setExpirationDate(LocalDateTime.now().plusDays(daysToExpire));
        inventory.setProductFamily(productFamily);
        inventory.setAboRh(aboRhType);
        inventory.setStatusReason(statusReason);
        inventory.setComments(comments);
        inventory.setIsLabeled(isLabeled);
        inventory.setTemperatureCategory(temperatureCategory);
        inventoryUtil.saveInventory(inventory);
    }

    private void createMultipleProducts(String unitNumber, String productCode, Integer quantity, String productFamily, String aboRh, String location, Integer daysToExpire, InventoryStatus status, String temperatureCategory) {
        AboRhType aboRhType = AboRhType.valueOf(aboRh);

        for (int i = 0; i < quantity; i++) {
            createInventory(unitNumber, productCode, productFamily, aboRhType, location, daysToExpire, status, temperatureCategory, true, null, null);
        }
    }
}
