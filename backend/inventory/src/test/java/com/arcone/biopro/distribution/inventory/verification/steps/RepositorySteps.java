package com.arcone.biopro.distribution.inventory.verification.steps;

import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.ProductFamily;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntity;
import com.arcone.biopro.distribution.inventory.infrastructure.persistence.InventoryEntityRepository;
import com.arcone.biopro.distribution.inventory.verification.common.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
public class RepositorySteps {
    @Autowired
    private InventoryEntityRepository inventoryEntityRepository;

    @Autowired
    private ScenarioContext scenarioContext;

    private final CountDownLatch waiter = new CountDownLatch(1);

    public InventoryEntity getInventoryWithRetry(String unitNumber, String productCode, InventoryStatus status) throws InterruptedException {
        int maxTryCount = 60;
        int tryCount = 0;

        InventoryEntity entity = null;
        while (tryCount < maxTryCount && entity == null) {
            entity = inventoryEntityRepository.findByUnitNumberAndProductCodeAndInventoryStatus(unitNumber, productCode, status).block();

            tryCount++;
            waiter.await(1, TimeUnit.SECONDS);
        }
        return entity;
    }

    public InventoryEntity getStoredInventory(String unitNumber, String productCode, InventoryStatus status) throws InterruptedException {
        int maxTryCount = 60;
        int tryCount = 0;

        InventoryEntity entity = null;
        while (tryCount < maxTryCount && entity == null) {
            entity = inventoryEntityRepository.findByUnitNumberAndProductCodeAndInventoryStatus(unitNumber, productCode, status).block();

            if (Objects.nonNull(entity) && Objects.isNull(entity.getDeviceStored())) {
                entity = null;
            }

            tryCount++;
            waiter.await(1, TimeUnit.SECONDS);
        }
        return entity;
    }

    private void createInventory(String unitNumber, String productCode, ProductFamily productFamily, AboRhType aboRhType, String location, Integer daysToExpire, InventoryStatus status) {
        inventoryEntityRepository.save(InventoryEntity.builder()
            .id(UUID.randomUUID())
            .productFamily(productFamily)
            .aboRh(aboRhType)
            .location(location)
            .collectionDate(ZonedDateTime.now().toString())
            .inventoryStatus(status)
            .expirationDate(LocalDateTime.now().plusDays(daysToExpire))
            .unitNumber(unitNumber)
            .productCode(productCode)
            .shortDescription("Short description")
            .build()).block();

    }

    @Then("The inventory status is {string}")
    public void theInventoryIsCreatedCorrectly(String status) throws InterruptedException {
//        InventoryEntity entity = getInventoryWithRetry(untNumber, "E0869VA0", InventoryStatus.valueOf(status));
        InventoryEntity inventory = getInventoryWithRetry(scenarioContext.getUnitNumber(), scenarioContext.getProductCode(), InventoryStatus.valueOf(status));


        assertNotNull(inventory);
//        assertEquals("E0869VA0", entity.getProductCode());
//        assertEquals(untNumber, entity.getUnitNumber());
        assertEquals(scenarioContext.getProductCode(), inventory.getProductCode());
        assertEquals(scenarioContext.getUnitNumber(), inventory.getUnitNumber());
        assertEquals(status, inventory.getInventoryStatus().name());
    }



    @And("For unit number {string} and product code {string} the device stored is {string} and the storage location is {string}")
    public void forUnitNumberAndProductCodeTheDeviceStoredIsAndTheStorageLocationIs(String unitNumber, String productCode, String deviceStorage, String storageLocation) throws InterruptedException {
//        InventoryEntity inventory = getStoredInventory(unitNumber, productCode, InventoryStatus.AVAILABLE);
        InventoryEntity inventory = getStoredInventory(scenarioContext.getUnitNumber(), scenarioContext.getProductCode(), InventoryStatus.AVAILABLE);
        assertNotNull(inventory);
        assertEquals(deviceStorage, inventory.getDeviceStored());
        assertEquals(storageLocation, inventory.getStorageLocation());
    }
}
