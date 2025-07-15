package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.BatchEntity;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.BatchItemEntity;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.DeviceEntity;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration
public class RepositorySteps {

    @Autowired
    private ReactiveCrudRepository<DeviceEntity, Long> deviceRepository;

    @Autowired
    private ReactiveCrudRepository<BatchEntity, Long> batchRepository;

    @Autowired
    private ReactiveCrudRepository<BatchItemEntity, Long> batchItemRepository;

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

    @Given("I have a device {string} at location {string} with status {string}")
    public void iHaveAValidDeviceAtLocation(String deviceId, String location, String status) {
        DeviceEntity device = new DeviceEntity(deviceId, location, status);
        deviceRepository.save(device).block();
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
}
