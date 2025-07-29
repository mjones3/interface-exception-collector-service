package com.arcone.biopro.distribution.receiving.verification.steps;

import com.arcone.biopro.distribution.receiving.verification.controllers.TransferReceiptController;
import com.arcone.biopro.distribution.receiving.verification.pages.EnterShippingInformationPage;
import com.arcone.biopro.distribution.receiving.verification.support.TestUtils;
import com.arcone.biopro.distribution.receiving.verification.support.kafka.KafkaHelper;
import com.arcone.biopro.distribution.receiving.verification.support.kafka.Topics;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
public class TransferReceiptSteps {

    @Autowired
    TestUtils testUtils;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaHelper kafkaHelper;

    @Autowired
    TransferReceiptController transferReceiptController;

    @Autowired
    CommonSteps commonSteps;

    private Map apiResponse;

    @Value("${kafka.waiting.time}")
    private int kafkaWait;

    @Autowired
    private EnterShippingInformationPage enterShippingInformationPage;

    @Given("A Internal Transfer shipment is completed with the following details:")
    public void aInternalTransferShipmentIsCompletedWithTheFollowingDetails(DataTable dataTable) throws Exception {

        Assert.assertNotNull(dataTable);

        var dataList = dataTable.asMaps();

        var shipmentData = dataList.getFirst();

        Assert.assertNotNull(shipmentData);

        Random r = new Random();
        var shipmentId = String.format("%s",r.nextInt(1000));

        var JSON = testUtils.getResource("events/shipment-completed-event-automation.json")
            .replace("{SHIPMENT_ID}", shipmentId)
            .replace("{ORDER_NUMBER}", shipmentData.get("Order_Number"))
            .replace("{LOCATION_CODE_FROM}", shipmentData.get("Location_Code_From"))
            .replace("{LOCATION_NAME_FROM}", shipmentData.get("Location_Name_From"))
            .replace("{LOCATION_NAME_TO}", shipmentData.get("Customer_Name"))
            .replace("{LOCATION_CODE_TO}", shipmentData.get("Customer_ID"))
            .replace("{TEMPERATURE_CATEGORY}", shipmentData.get("Temperature_Category"))
            .replace("{LABEL_STATUS}", shipmentData.get("Label_Status"))
            .replace("{QUARANTINED_PRODUCTS}", shipmentData.get("Quarantined_Products"))
            .replace("{PRODUCT_FAMILY}", shipmentData.get("Product_Family"));

        log.debug("Event payload: {}", JSON);

        String eventId = UUID.randomUUID().toString();
        var event = kafkaHelper.sendEvent(eventId, objectMapper.readTree(JSON), Topics.SHIPMENT_COMPLETED).block();
        Assert.assertNotNull(event);

        Thread.sleep(kafkaWait);

    }

    @When("I request to validate the internal transfer order number {string} from the location code {string}.")
    public void iRequestToValidateTheInternalTransferOrderNumberFromTheLocationCode(String orderNumber, String locationCode) {

        var response = transferReceiptController.validateInternalTransferInformation(orderNumber,locationCode);
        Assert.assertNotNull(response);
        this.apiResponse = response;
    }

    @And("I should receive the order number as {string} and temperature category as {string}")
    public void iShouldReceiveTheOrderNumberAsAndTemperatureCategoryAs(String orderNumber, String temperatureCategory) {
        Assert.assertNotNull(apiResponse);
        var data = (LinkedHashMap) apiResponse.get("data");
        Assert.assertNotNull(data);
        Assert.assertEquals(Integer.parseInt(orderNumber),data.get("orderNumber"));
        Assert.assertEquals(temperatureCategory,data.get("productCategory"));
    }

    @Then("I should be able to enter the transfer receipt information for the following attributes: {string}.")
    public void iShouldBeAbleToEnterTheTransferReceiptInformationForTheFollowingAttributes(String shippingInformationAttributes) {
        commonSteps.validateShippingInformationInformationAttributes(apiResponse,shippingInformationAttributes);
    }

    @And("I am at the Transfer Receipt Page.")
    public void iAmAtTheTransferReceiptPage() throws InterruptedException {
        enterShippingInformationPage.navigateToTransferReceiptShippingInformation();
    }

    @When("I enter internal transfer order number {string}.")
    public void iEnterInternalTransferOrderNumber(String orderNumber) throws InterruptedException {
        enterShippingInformationPage.enterOrderNumber(orderNumber);
    }

    @Then("I should see the Temperature category information as {string}")
    public void iShouldSeeTheTemperatureCategoryInformationAs(String temperatureCategory) {
        enterShippingInformationPage.verifyTemperatureCategory(temperatureCategory);
    }

    @Then("The transfer information continue option should be {string}.")
    public void theTransferInformationContinueOptionShouldBe(String enabledDisabled) {

        if ("enabled".equals(enabledDisabled)) {
            enterShippingInformationPage.waitForTransferContinueButtonToBeEnabled();
            Assert.assertTrue(enterShippingInformationPage.isTransferContinueButtonEnabled());
        } else if ("disabled".equals(enabledDisabled)) {
            Assert.assertFalse(enterShippingInformationPage.isTransferContinueButtonEnabled());
        } else {
            Assert.fail("The Transfer continue button should be enabled or disabled");
        }
    }

    @Then("The start time zone field should be pre defined as {string}.")
    public void theStartTimeZoneFieldShouldBePreDefinedAs(String timeZone) {
        enterShippingInformationPage.verifyStartDefaultTzIs(timeZone);
    }

    @When("I enter the transfer receipt comments as {string}.")
    public void iEnterTheTransferReceiptCommentsAs(String comments) {
        enterShippingInformationPage.setComments(comments);
    }
}
