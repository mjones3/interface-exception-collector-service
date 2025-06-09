package com.arcone.biopro.distribution.receiving.verification.steps;

import com.arcone.biopro.distribution.receiving.verification.support.ApiHelper;
import com.arcone.biopro.distribution.receiving.verification.support.graphql.GraphQLQueryMapper;
import com.arcone.biopro.distribution.receiving.verification.support.kafka.KafkaHelper;
import com.arcone.biopro.distribution.receiving.verification.support.TestUtils;
import com.arcone.biopro.distribution.receiving.verification.support.kafka.Topics;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

@Slf4j
public class DeviceSteps {

    @Autowired
    TestUtils testUtils;
    @Autowired
    KafkaHelper kafkaHelper;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    ApiHelper apiHelper;

    Map validateDeviceResponse;

    @Given("I have a thermometer configured as location {string}, Device ID as {string}, Category as {string} and Device Type as {string}.")
    public void configureThermometer(String location, String deviceId, String deviceCategory, String deviceType) throws Exception {
        var JSON = testUtils.getResource("events/device-created-event.json")
            .replace("{DEVICE_ID}", deviceId)
            .replace("{DEVICE_CATEGORY}", deviceCategory)
            .replace("{DEVICE_TYPE}", deviceType)
            .replace("{LOCATION}", location);
        log.debug("Event payload: {}", JSON);

        String eventId = UUID.randomUUID().toString();
        var event = kafkaHelper.sendEvent(eventId, objectMapper.readTree(JSON), Topics.DEVICE_CREATED).block();
        Assert.assertNotNull(event);
    }

    @When("I enter thermometer ID {string} for the location code {string} and Temperature Category {string}.")
    public void iEnterThermometerIDForTheLocationCodeAndTemperatureCategory(String thermometerId, String location, String temperatureCategory) {
        var response = apiHelper.graphQlRequest(GraphQLQueryMapper.validateDevice(thermometerId, location), "validateDevice");
        validateDeviceResponse = (Map) response.get("data");
    }

    @Then("I should receive the device details containing Device ID as {string}, Category as {string} and Device Type as {string}.")
    public void iShouldReceiveTheDeviceDetailsContainingDeviceIDAsCategoryAsAndDeviceTypeAs(String deviceId, String deviceCategory, String deviceType) {
        Assert.assertNotNull(validateDeviceResponse);

        Assert.assertEquals(deviceId, validateDeviceResponse.get("bloodCenterId").toString());
        Assert.assertEquals(deviceCategory, validateDeviceResponse.get("deviceCategory").toString());
        Assert.assertEquals(deviceType, validateDeviceResponse.get("deviceType").toString());
    }
}
