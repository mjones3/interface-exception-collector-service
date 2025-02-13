package com.arcone.biopro.distribution.partnerorderprovider.verification.steps;

import com.arcone.biopro.distribution.partnerorderprovider.verification.support.ApiHelper;
import com.arcone.biopro.distribution.partnerorderprovider.verification.support.Endpoints;
import com.arcone.biopro.distribution.partnerorderprovider.verification.support.TestUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class CancelOrderSteps {

    private JSONObject cancelOrderRequest;
    private JSONObject cancelOrderResponse;
    private ResponseEntity<String> orderInboundResponse;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ApiHelper apiHelper;

    @Given("I have a Partner cancel order request payload {string}.")
    public void givenPartnerCancelOrder(String jsonFileName) throws Exception {
        var jsonContent = testUtils.getResource(jsonFileName);
        cancelOrderRequest = new JSONObject(jsonContent);
        log.info("JSON PAYLOAD :{}", cancelOrderRequest);
        Assert.assertNotNull(cancelOrderRequest);
    }

    @When("I send a request to the Partner Cancel Order Inbound Interface.")
    public void sendOrderCancelRequest() throws JSONException {
        orderInboundResponse = apiHelper.patchRequest(Endpoints.CANCEL_ORDER_INBOUND.replace("{externalId}",cancelOrderRequest.getString("externalId")), cancelOrderRequest.toString());
        log.info("Cancel Order inbound response :{}", orderInboundResponse.getBody());
        cancelOrderResponse = new JSONObject(String.valueOf(orderInboundResponse.getBody()));
        Assert.assertNotNull(orderInboundResponse);
    }

    @Then("The response status code should be {int}.")
    public void validateResponse(int responseCode){
        Assert.assertEquals(responseCode, orderInboundResponse.getStatusCode().value());
    }

    @And("The response status should be {string}.")
    public void checkResponseStatus(String status){
        var statusResponse  = HttpStatus.valueOf(status);
        Assert.assertEquals(statusResponse, orderInboundResponse.getStatusCode());
    }

    @And("The cancel error message should be {string}.")
    public void checkErrorMessage(String message) throws JSONException {
        testUtils.checkErrorMessage(message,cancelOrderResponse);
    }
}
