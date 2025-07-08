package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.BatchEntity;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.DeviceEntity;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration
public class RepositorySteps {

    @Autowired
    private ReactiveCrudRepository<DeviceEntity, Long> deviceRepository;

    @Autowired
    private ReactiveCrudRepository<BatchEntity, Long> batchRepository;

    private Boolean validationResult;
    @Setter
    private Boolean batchActiveResult;
    @Getter
    private String batchId;

    @Given("I have a device {string} at location {string} with status {string}")
    public void iHaveAValidDeviceAtLocation(String deviceId, String location, String status) {
        DeviceEntity device = new DeviceEntity(deviceId, location, status);
        deviceRepository.save(device).block();
    }

    @Then("the device validation should be successful")
    public void theDeviceValidationShouldBeSuccessful() {
        assertTrue(validationResult, "Device validation should be successful");
    }
    @Then("I should see a notification {string}")
    public void iShouldSeeANotification(String message) {
        assertFalse(validationResult, message);
    }

    public void setValidationResult(Boolean result) {
        this.validationResult = result;
    }

    @Given("I have a batch {string} for device {string} with end time null")
    public void iHaveABatchForDeviceWithEndTimeNull(String batchId, String deviceId) {
        this.batchId = batchId;
        DeviceEntity device = new DeviceEntity(deviceId, "DEFAULT_LOCATION", "ACTIVE");
        deviceRepository.save(device).block();
        BatchEntity batch = new BatchEntity(deviceId, LocalDateTime.now(), null);
        batchRepository.save(batch).block();
    }

    @Then("the batch should be active")
    public void theBatchShouldBeActive() {
        assertTrue(batchActiveResult, "Batch should be active");
    }

}
