package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.DeviceEntity;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration
public class RepositorySteps {

    @Autowired
    private ReactiveCrudRepository<DeviceEntity, Long> deviceRepository;

    private Boolean validationResult;

    @Given("I have a device {string} at location {string} with status {string}")
    public void iHaveAValidDeviceAtLocation(String deviceId, String location, String status) {
        DeviceEntity device = new DeviceEntity(deviceId, location, status);
        deviceRepository.save(device).block();
    }

    @Then("the device validation should be successful")
    public void theDeviceValidationShouldBeSuccessful() {
        assertTrue(validationResult, "Device validation should be successful");
    }

    public void setValidationResult(Boolean result) {
        this.validationResult = result;
    }
}
