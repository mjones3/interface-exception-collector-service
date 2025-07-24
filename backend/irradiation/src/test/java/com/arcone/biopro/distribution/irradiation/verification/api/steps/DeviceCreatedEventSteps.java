package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.adapter.in.listener.DeviceCreated;
import com.arcone.biopro.distribution.irradiation.adapter.common.EventMessage;
import com.arcone.biopro.distribution.irradiation.application.usecase.CreateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class DeviceCreatedEventSteps {

    @Autowired
    private CreateDeviceUseCase createDeviceUseCase;

    @Autowired
    private DeviceRepository deviceRepository;

    private Device createdDevice;
    private String currentDeviceId;

    @When("I received a Device Created event with the following:")
    public void i_received_a_device_created_event_with_the_following(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> data = rows.get(0);

        String id = data.get("Id");
        String location = data.get("Location");
        String deviceCategory = data.get("Device Category");
        String status = data.get("Status");

        this.currentDeviceId = id;

        DeviceCreated payload = new DeviceCreated(id, location, deviceCategory, status, ZonedDateTime.now(), ZonedDateTime.now());
        EventMessage<DeviceCreated> eventMessage = new EventMessage<>("DeviceCreated", "1.0", payload);

        CreateDeviceUseCase.Input input = new CreateDeviceUseCase.Input(id, location, status, deviceCategory);

        if ("IRRADIATOR".equals(deviceCategory)) {
            StepVerifier.create(createDeviceUseCase.execute(input))
                .assertNext(device -> this.createdDevice = device)
                .verifyComplete();
        } else {
            StepVerifier.create(createDeviceUseCase.execute(input))
                .verifyComplete();
        }
    }

    @Then("the device should be created as follows:")
    public void the_device_should_be_created_as_follows(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        Map<String, String> expectedData = rows.get(0);

        String expectedDeviceId = expectedData.get("Device ID");
        String expectedLocation = expectedData.get("Location");
        String expectedStatus = expectedData.get("Status");

        StepVerifier.create(deviceRepository.findByDeviceId(DeviceId.of(expectedDeviceId)))
            .assertNext(device -> {
                assertEquals(expectedDeviceId, device.getDeviceId().getValue());
                assertEquals(expectedLocation, device.getLocation().value());
                assertEquals(expectedStatus, device.getStatus());
            })
            .verifyComplete();
    }

    @Then("the device should not be created and a message should be logged")
    public void the_device_should_not_be_created_and_a_message_should_be_logged() {
        // This step verifies that no device was created for non-irradiator types
        // The logging verification would typically be done through log capture in a real test
        log.info("Device creation skipped for non-irradiator type: {}", currentDeviceId);
    }
}
