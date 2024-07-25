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
import org.springframework.http.ResponseEntity;

@Slf4j
public class OrderInboundSteps {
    private JSONObject partnerOrder;
    private JSONObject partnerOrderResponse;
    private ResponseEntity<String> orderInboundResponse;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ApiHelper apiHelper;


    @Given("I have a Partner order {string}.")
    public void givenPartnerOrder(String jsonFileName) throws Exception {
        var jsonContent = testUtils.getResource(jsonFileName);
        partnerOrder = new JSONObject(jsonContent);
        log.info("JSON PAYLOAD :{}", partnerOrder);
        Assert.assertNotNull(partnerOrder);
    }

    @When("I send a request to the Partner Order Inbound Interface.")
    public void sendOrderInboundRequest() throws JSONException {
        orderInboundResponse = apiHelper.postRequest(Endpoints.ORDER_INBOUND, partnerOrder.toString());
        log.info("Order inbound response :{}", orderInboundResponse.getBody());
        partnerOrderResponse = new JSONObject(String.valueOf(orderInboundResponse.getBody()));
        Assert.assertNotNull(orderInboundResponse);
    }

    @Then("The response status should be {int}.")
    public void validateResponse(int responseCode) {
        Assert.assertEquals(responseCode, orderInboundResponse.getStatusCode().value());
    }

    @And("The Order status should be {string}.")
    public void checkOrderStatus(String status) throws JSONException {
        Assert.assertEquals(status, partnerOrderResponse.getString("status"));
    }

    @And("The error message should be {string}.")
    public void checkErrorMessage(String message) throws JSONException {
        var errorList = partnerOrderResponse.getJSONArray("errors");
        var errorFound = false;
        for (int i = 0; i < errorList.length(); i++) {
            var error = errorList.getJSONObject(i);
            if (error.getString("description").equals(message)) {
                errorFound = true;
                break;
            }
        }
        Assert.assertTrue(errorFound);
    }

}
