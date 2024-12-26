package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.common.TestUtil;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.History;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import com.arcone.biopro.distribution.inventory.verification.common.ScenarioContext;
import com.arcone.biopro.distribution.inventory.verification.utils.ISBTProductUtil;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.arcone.biopro.distribution.inventory.verification.steps.KafkaListenersSteps.*;
import static com.arcone.biopro.distribution.inventory.verification.steps.UseCaseSteps.quarantineReasonMap;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.in;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepositorySteps {
    private final InventoryEntityRepository inventoryEntityRepository;

    private final ScenarioContext scenarioContext;

    private final String defaultLocation = "123456789";

    public InventoryEntity getInventory(String unitNumber, String productCode) {
        return inventoryEntityRepository.findByUnitNumberAndProductCode(unitNumber, productCode).block();
    }

    public InventoryEntity getInventory(String unitNumber, String productCode, InventoryStatus status) {
        return inventoryEntityRepository.findByUnitNumberAndProductCodeAndInventoryStatus(unitNumber, productCode, status).block();
    }

    public InventoryEntity getStoredInventory(String unitNumber, String productCode, InventoryStatus status) {

        return inventoryEntityRepository.findByUnitNumberAndProductCodeAndInventoryStatus(unitNumber, productCode, status).block();
    }

    private void createInventory(InventoryEntity inventoryEntity) {
        inventoryEntityRepository.save(inventoryEntity).block();
    }

    private InventoryEntity newInventoryObject(String unitNumber, String productCode, InventoryStatus status) {
        InventoryEntity inventory =
            InventoryEntity.builder()
                .id(UUID.randomUUID())
                .productFamily(ISBTProductUtil.getProductFamily(productCode))
                .aboRh(AboRhType.OP)
                .location(defaultLocation)
                .collectionDate(ZonedDateTime.now())
                .inventoryStatus(status)
                .expirationDate(LocalDateTime.now().plusDays(1))
                .unitNumber(unitNumber)
                .productCode(productCode)
                .shortDescription(ISBTProductUtil.getProductDescription(productCode))
                .build();
        return inventory;
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
    public void forUnitNumberAndProductCodeTheDeviceStoredIsAndTheStorageLocationIs(String unitNumber, String productCode, String deviceStorage, String storageLocation) throws InterruptedException {
        InventoryEntity inventory = getStoredInventory(scenarioContext.getUnitNumber(), scenarioContext.getProductCode(), InventoryStatus.AVAILABLE);
        assertNotNull(inventory);
        assertEquals(deviceStorage, inventory.getDeviceStored());
        assertEquals(storageLocation, inventory.getStorageLocation());
    }

    @Given("I have a unit number {string} with product {string} that is {string}")
    public void iAmListeningEventForUnitNumber(String unitNumber, String productCode, String status) {
        scenarioContext.setUnitNumber(unitNumber);
        scenarioContext.setProductCode(productCode);
        InventoryEntity inventoryEntity = newInventoryObject(unitNumber, productCode, InventoryStatus.valueOf(status));
        createInventory(inventoryEntity);
    }

    @Then("I verify the quarantine reason {string} with id {string} is found {string} for unit number {string} and product {string}")
    public void iVerifyTheQuarantineReasonIsInactiveForUnitNumberAndProduct(String quarantineReason, String quarantineReasonId, String isFound, String unitNumber, String productCode) throws InterruptedException {
        InventoryEntity inventory = getInventory(scenarioContext.getUnitNumber(), scenarioContext.getProductCode(), InventoryStatus.AVAILABLE);

        assert inventory != null;
        List<Quarantine> productsReason = inventory.getQuarantines().stream().filter(q -> quarantineReasonMap.get(quarantineReason)
            .equals(q.reason()) && q.externId().equals(Long.parseLong(quarantineReasonId))).toList();

        assertEquals(Boolean.valueOf(isFound), !productsReason.isEmpty());
    }

    @Given("I have a Discarded Product in Inventory with previous status {string}")
    public void iHaveADiscardedProductInInventoryWithPreviousStatus(String previousStatus) {
        List<Quarantine> quarantines = null;
        List<History> histories = null;

        scenarioContext.setUnitNumber(TestUtil.randomString(13));
        scenarioContext.setProductCode("E0869VA0");

        if (InventoryStatus.AVAILABLE.toString().equals(previousStatus)) {
            histories = List.of(new History(InventoryStatus.valueOf(previousStatus), null, null));
        }

        quarantines = List.of(new Quarantine(1L, "OTHER", "a comment"));
        histories = List.of(new History(InventoryStatus.valueOf(previousStatus), null, null));


        inventoryEntityRepository.save(InventoryEntity.builder()
            .id(UUID.randomUUID())
            .productFamily("PLASMA_TRANSFUSABLE")
            .aboRh(AboRhType.OP)
            .location("Miami")
            .collectionDate(ZonedDateTime.now())
            .inventoryStatus(InventoryStatus.DISCARDED)
            .expirationDate(LocalDateTime.now().plusDays(10))
            .unitNumber(scenarioContext.getUnitNumber())
            .productCode(scenarioContext.getProductCode())
            .quarantines(quarantines)
            .histories(histories)
            .shortDescription("Short description")
            .comments("Some comments")
            .statusReason("EXPIRED")
            .build()).block();
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
            InventoryEntity inventoryEntity = newInventoryObject(unitNumber, productCode, status);

            if (headers.contains("ABO/RH")) {
                String aboRh = inventory.get("ABO/RH");
                inventoryEntity.setAboRh(AboRhType.valueOf(aboRh));
            }

            if (headers.contains("Expiration Date")) {
                String expirationDate = inventory.get("Expiration Date");
                inventoryEntity.setExpirationDate(LocalDateTime.parse(expirationDate));
            }

            createInventory(inventoryEntity);
        }
    }

    @Then("the parent inventory statuses should be updated as follows:")
    @Then("the inventory statuses should be updated as follows:")
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
            assertEquals(expectedStatus, inventoryEntity.getInventoryStatus().name());
        }
    }
}
