package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.BatchEntity;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.BatchItemEntity;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.DeviceEntity;
import com.arcone.biopro.distribution.irradiation.verification.api.support.IrradiationContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ContextConfiguration
public class RepositorySteps {

    @Autowired
    private ReactiveCrudRepository<DeviceEntity, Long> deviceRepository;

    @Autowired
    private ReactiveCrudRepository<BatchEntity, Long> batchRepository;

    @Autowired
    private ReactiveCrudRepository<BatchItemEntity, Long> batchItemRepository;

    @Autowired
    private IrradiationContext irradiationContext;

    @Setter
    private Boolean validationResult;

    @Getter @Setter
    private String batchDeviceId;

    @Getter @Setter
    private String batchStartTime;

    @Getter @Setter
    private List<Map<String, String>> batchItems = new ArrayList<>();

    @Setter
    private String batchSubmissionResult;

    @Setter
    private String batchSubmissionError;

    @Getter
    private String lastCreatedDeviceId;

    @Getter
    private Long lastCreatedBatchId;

    @Given("I have a device {string} at location {string} with status {string}")
    public void iHaveAValidDeviceAtLocation(String deviceId, String location, String status) {
        // Check if device already exists
        DeviceEntity existingDevice = deviceRepository.findAll()
            .filter(device -> deviceId.equals(device.getDeviceId()))
            .blockFirst();

        if (existingDevice == null) {
            DeviceEntity device = new DeviceEntity(deviceId, location, status);
            deviceRepository.save(device).block();
        }

        this.lastCreatedDeviceId = deviceId;

        // Store device ID in context for later use
        irradiationContext.setDeviceId(deviceId);
        irradiationContext.setLocation(location);
    }

    @Given("I have an open batch for device {string}")
    public void iHaveAnOpenBatchForDevice(String deviceId) {
        DeviceEntity device = new DeviceEntity(deviceId, "DEFAULT_LOCATION", "ACTIVE");
        deviceRepository.save(device).block();
        BatchEntity batch = new BatchEntity(deviceId, java.time.LocalDateTime.now(), null);
        batchRepository.save(batch).block();
    }

    @Then("the device validation should be successful")
    public void theDeviceValidationShouldBeSuccessful() {
        assertTrue(validationResult, "Device validation should be successful");
    }

    @Then("the device validation should fail with error {string}")
    public void theDeviceValidationShouldFailWithError(String errorMessage) {
        assertFalse(validationResult, errorMessage);
    }

    @Given("I have a batch submission request with device {string}, start time {string} and products below:")
    public void iHaveABatchSubmissionRequestWithDeviceStartTimeAndUnitNumbers(String deviceId, String startTime, DataTable dataTable) {
        this.batchDeviceId = deviceId;
        this.batchStartTime = startTime;
        this.batchItems = dataTable.asMaps().stream()
                .map(row -> Map.of(
                    "unitNumber", row.get("Unit Number"),
                    "productCode", row.get("Product Code"),
                    "lotNumber", row.get("Irradiator Indicator")
                ))
                .toList();
    }

    @Given("I have a batch submission request with device {string}, start time {string} and no unit numbers")
    public void iHaveABatchSubmissionRequestWithDeviceStartTimeAndNoUnitNumbers(String deviceId, String startTime) {
        this.batchDeviceId = deviceId;
        this.batchStartTime = startTime;
        this.batchItems = new ArrayList<>();
    }

    @Then("the batch should be successfully created in the repository with items:")
    public void theBatchShouldBeSuccessfullyCreatedInTheRepositoryWithItems(DataTable expectedItems) {
        BatchEntity batch = batchRepository.findAll()
                .filter(b -> batchDeviceId.equals(b.getDeviceId()))
                .blockFirst();

        assertNotNull(batch, "Batch should be created in repository");

        // Store batch ID for later use in completion steps
        irradiationContext.setBatchId(batch.getId().toString());

        List<BatchItemEntity> actualItems = batchItemRepository.findAll()
                .filter(item -> batch.getId().equals(item.getBatchId()))
                .collectList()
                .block();

        List<Map<String, String>> expectedItemMaps = expectedItems.asMaps();
        assertEquals(expectedItemMaps.size(), actualItems.size(), "Batch items count should match");

        for (int i = 0; i < expectedItemMaps.size(); i++) {
            Map<String, String> expected = expectedItemMaps.get(i);
            BatchItemEntity actual = actualItems.get(i);

            assertEquals(expected.get("Unit Number"), actual.getUnitNumber(), "Unit number should match");
            assertEquals(expected.get("Product Code"), actual.getProductCode(), "Product code should match");
            assertEquals(expected.get("Irradiator Indicator"), actual.getLotNumber(), "Lot number should match");
        }
    }

    @Then("the batch should be successfully created")
    public void theBatchShouldBeSuccessfullyCreated() {
        BatchEntity batch = batchRepository.findAll()
                .filter(b -> batchDeviceId.equals(b.getDeviceId()))
                .blockFirst();

        assertNotNull(batch, "Batch should be created in repository");

        // Store batch ID for later use in completion steps
        irradiationContext.setBatchId(batch.getId().toString());
    }

    @Then("I should see the success message {string}")
    public void iShouldSeeTheSuccessMessage(String expectedMessage) {
        assertEquals(expectedMessage, batchSubmissionResult);
    }

    @Then("the batch submission should fail")
    public void theBatchSubmissionShouldFail() {
        assertNotNull(batchSubmissionError, "Batch submission should have failed");
    }

    @Then("I should see the error message {string}")
    public void iShouldSeeTheErrorMessage(String expectedError) {
        assertNotNull(batchSubmissionError);
        assertTrue(batchSubmissionError.contains(expectedError), "Error message should contain: " + expectedError);
    }

    @And("the product {string} in the unit {string} was already irradiated into {string} in a completed batch for device {string}")
    public void theProductInTheUnitWasAlreadyIrradiatedInACompletedBatchForDevice(String productCode, String unitNumber, String newProductCode, String deviceId) {
        deviceRepository.save(DeviceEntity.builder().deviceId(deviceId).status("ACTIVE").location("123456789").build()).block();
        var batch = batchRepository.save(BatchEntity.builder().deviceId(deviceId).startTime(LocalDateTime.now()).endTime(LocalDateTime.now()).build()).block();
        batchItemRepository.save(BatchItemEntity.builder().batchId(batch.getId()).lotNumber("123").unitNumber(unitNumber).productCode(productCode).newProductCode(newProductCode).build()).block();
    }

    @And("the product {string} in the unit {string} was already irradiated in a opened batch for device {string}")
    public void theProductInTheUnitWasAlreadyIrradiatedInAOpenedBatchForDevice(String productCode, String unitNumber, String deviceId) {
        // Create device first
        deviceRepository.save(DeviceEntity.builder().deviceId(deviceId).status("ACTIVE").location("123456789").build()).block();

        // Create open batch (endTime = null)
        var batch = batchRepository.save(BatchEntity.builder()
            .deviceId(deviceId)
            .startTime(LocalDateTime.now())
            .endTime(null)
            .build()).block();

        // Create batch item
        batchItemRepository.save(BatchItemEntity.builder()
            .batchId(batch.getId())
            .lotNumber("123")
            .unitNumber(unitNumber)
            .productCode(productCode)
            .build()).block();
    }

    @Given("An irradiation batch has been started with the following units for irradiator {string}")
    public void anIrradiationBatchHasBeenStartedWithTheFollowingUnits(String deviceId, DataTable dataTable) {
        deviceRepository.save(DeviceEntity.builder().deviceId(deviceId).status("ACTIVE").location("123456789").build()).block();
        long batchId = createBatch(deviceId,LocalDateTime.now(), null);
        List<Map<String, String>> batchItems = dataTable.asMaps();
        for (Map<String, String> item : batchItems) {
            createBatchItem(batchId, item.get("Unit Number"), item.get("Lot Number"), item.get("Product Code"), null);
        }
    }

    public Long createBatch(String deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        BatchEntity batch = new BatchEntity(deviceId, startTime, endTime);
        BatchEntity savedBatch = batchRepository.save(batch).block();
        this.lastCreatedBatchId = savedBatch.getId();
        return savedBatch.getId();
    }

    public void createBatchItem(Long batchId, String unitNumber, String lotNumber, String productCode, String productFamily) {
        BatchItemEntity batchItem = BatchItemEntity.builder()
            .batchId(batchId)
            .unitNumber(unitNumber)
            .lotNumber(lotNumber)
            .productCode(productCode)
            .productFamily(productFamily)
            .build();
        batchItemRepository.save(batchItem).block();
    }

    public void createBatchItemWithExpiration(Long batchId, String unitNumber, String lotNumber, String productCode, String productFamily, String expirationDate) {
        LocalDateTime expiration = null;
        if (expirationDate != null && !expirationDate.isEmpty()) {
            expiration = LocalDateTime.parse(expirationDate + "T23:59:00");
        }

        BatchItemEntity batchItem = BatchItemEntity.builder()
            .batchId(batchId)
            .unitNumber(unitNumber)
            .lotNumber(lotNumber)
            .productCode(productCode)
            .productFamily(productFamily)
            .expirationDate(expiration)
            .build();
        batchItemRepository.save(batchItem).block();
    }

    public void verifyProductCodeUpdate(String unitNumber, String expectedNewCode) {
        BatchItemEntity batchItem = batchItemRepository.findAll()
            .filter(item -> unitNumber.equals(item.getUnitNumber()))
            .blockFirst();

        assertNotNull(batchItem, "Batch item should exist for unit number: " + unitNumber);
        assertEquals(expectedNewCode, batchItem.getNewProductCode(),
            "New product code should be updated for unit: " + unitNumber);
    }

    public void updateBatchStartTimeForUnit(String unitNumber, LocalDateTime startTime) {
        // Find the batch that contains this unit
        BatchItemEntity batchItem = batchItemRepository.findAll()
            .filter(item -> unitNumber.equals(item.getUnitNumber()))
            .blockFirst();
        
        if (batchItem != null) {
            // Find and update the batch
            BatchEntity batch = batchRepository.findById(batchItem.getBatchId()).block();
            if (batch != null) {
                // Set realistic end time (e.g., 2 hours after start time for typical irradiation duration)
                LocalDateTime endTime = startTime.plusHours(2);
                batch.setStartTime(startTime);
                batch.setEndTime(endTime);
                batchRepository.save(batch).block();
                log.info("Updated batch {} times - start: {}, end: {} for unit {}", batch.getId(), startTime, endTime, unitNumber);
            }
        }
    }

    public boolean isProductStoredEventProcessed(String unitNumber, String productCode) {
        BatchItemEntity batchItem = batchItemRepository.findAll()
            .filter(item -> unitNumber.equals(item.getUnitNumber()) && productCode.equals(item.getProductCode()))
            .blockFirst();
        
        return batchItem != null && Boolean.TRUE.equals(batchItem.getIsTimingRuleValidated());
    }

    public void closeBatchForUnit(String unitNumber) {
        // Find the batch that contains this unit
        BatchItemEntity batchItem = batchItemRepository.findAll()
            .filter(item -> unitNumber.equals(item.getUnitNumber()))
            .blockFirst();
        
        if (batchItem != null) {
            // Find and close the batch
            BatchEntity batch = batchRepository.findById(batchItem.getBatchId()).block();
            if (batch != null && batch.getEndTime() == null) {
                batch.setEndTime(LocalDateTime.now());
                batchRepository.save(batch).block();
                log.info("Closed batch {} for unit {}", batch.getId(), unitNumber);
            }
        }
    }
}
