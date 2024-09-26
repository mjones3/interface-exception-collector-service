package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.commm.TestUtil;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.History;
import com.arcone.biopro.distribution.inventory.domain.model.vo.Quarantine;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import com.arcone.biopro.distribution.inventory.verification.common.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.arcone.biopro.distribution.inventory.verification.steps.KafkaListenersSteps.EVENT_QUARANTINE_UPDATED;
import static com.arcone.biopro.distribution.inventory.verification.steps.UseCaseSteps.quarantineReasonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class RepositorySteps {
    @Autowired
    private InventoryEntityRepository inventoryEntityRepository;

    @Autowired
    private ScenarioContext scenarioContext;

    private final CountDownLatch waiter = new CountDownLatch(1);

    public InventoryEntity getInventory(String unitNumber, String productCode, InventoryStatus status) {
        return inventoryEntityRepository.findByUnitNumberAndProductCodeAndInventoryStatus(unitNumber, productCode, status).block();
    }

    public InventoryEntity getStoredInventory(String unitNumber, String productCode, InventoryStatus status) {

        return inventoryEntityRepository.findByUnitNumberAndProductCodeAndInventoryStatus(unitNumber, productCode, status).block();
    }

    private void createInventory(String unitNumber, String productCode, String productFamily, AboRhType aboRhType, String location, Integer daysToExpire, InventoryStatus status) {
        inventoryEntityRepository.save(InventoryEntity.builder()
            .id(UUID.randomUUID())
            .productFamily(productFamily)
            .aboRh(aboRhType)
            .location(location)
            .collectionDate(ZonedDateTime.now())
            .inventoryStatus(status)
            .expirationDate(LocalDateTime.now().plusDays(daysToExpire))
            .unitNumber(unitNumber)
            .productCode(productCode)
            .shortDescription("Short description")
            .build()).block();

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

    @And("For unit number {string} and product code {string} the device stored is {string} and the storage location is {string}")
    public void forUnitNumberAndProductCodeTheDeviceStoredIsAndTheStorageLocationIs(String unitNumber, String productCode, String deviceStorage, String storageLocation) throws InterruptedException {
        InventoryEntity inventory = getStoredInventory(scenarioContext.getUnitNumber(), scenarioContext.getProductCode(), InventoryStatus.AVAILABLE);
        assertNotNull(inventory);
        assertEquals(deviceStorage, inventory.getDeviceStored());
        assertEquals(storageLocation, inventory.getStorageLocation());
    }

    @Given("I have a unit number {string} with product {string} that is {string}")
    public void iAmListeningEventForUnitNumber(String untNumber, String productCode, String status) {
        scenarioContext.setUnitNumber(untNumber);
        scenarioContext.setProductCode(productCode);
        createInventory(scenarioContext.getUnitNumber(), scenarioContext.getProductCode(), "PLASMA_TRANSFUSABLE", AboRhType.OP, "Miami", 10, InventoryStatus.valueOf(status));
    }

    @Then("I verify the quarantine reason {string} with id {string} is found {string} for unit number {string} and product {string}")
    public void iVerifyTheQuarantineReasonIsInactiveForUnitNumberAndProduct(String quarantineReason, String quarantineReasonId, String isFound, String unitNumber, String productCode) throws InterruptedException {
        InventoryEntity inventory = getInventory(scenarioContext.getUnitNumber(), scenarioContext.getProductCode(), InventoryStatus.valueOf("QUARANTINED"));

        assert inventory != null;
        List<Quarantine> productsReason =  inventory.getQuarantines().stream().filter(q -> quarantineReasonMap.get(quarantineReason)
            .equals(q.reason()) && q.externId().equals(Long.parseLong(quarantineReasonId))).toList();

        assertEquals(Boolean.valueOf(isFound), !productsReason.isEmpty());
    }

    @Given("I have a Discarded Product in Inventory with previous status {string}")
    public void iHaveADiscardedProductInInventoryWithPreviousStatus(String previousStatus) {
        List<Quarantine> quarantines = null;
        List<History> histories = null;

        scenarioContext.setUnitNumber(TestUtil.randomString(13));
        scenarioContext.setProductCode("E0869VA0");

        if(InventoryStatus.AVAILABLE.toString().equals(previousStatus)) {
            histories = List.of(new History(InventoryStatus.valueOf(previousStatus), null, null));
        }
        if (InventoryStatus.QUARANTINED.toString().equals(previousStatus)) {
            quarantines = List.of(new Quarantine(1L, "OTHER", "a comment"));
            histories = List.of(new History(InventoryStatus.valueOf(previousStatus), null, null));
        }


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
}
