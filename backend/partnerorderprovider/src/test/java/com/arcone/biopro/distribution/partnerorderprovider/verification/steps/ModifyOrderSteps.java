package com.arcone.biopro.distribution.partnerorderprovider.verification.steps;

import com.arcone.biopro.distribution.partnerorderprovider.verification.support.ApiHelper;
import com.arcone.biopro.distribution.partnerorderprovider.verification.support.Endpoints;
import com.arcone.biopro.distribution.partnerorderprovider.verification.support.SharedContext;
import com.arcone.biopro.distribution.partnerorderprovider.verification.support.TestUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ModifyOrderSteps {

    private JSONObject modifyOrderRequest;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    SharedContext context;

    @Given("I have a Partner modify order payload {string}.")
    public void iHaveAPartnerModifyOrderPayload(String payloadFileName) throws Exception {
        var jsonContent = testUtils.getResource(payloadFileName);
        modifyOrderRequest = new JSONObject(jsonContent);
        log.info("JSON PAYLOAD :{}", modifyOrderRequest);
        Assert.assertNotNull(modifyOrderRequest);
    }

    @When("I send a request to the Partner modify order interface to modify the order {string}.")
    public void sendModifyRequest(String externalId) throws JSONException {
        context.setExternalId(externalId);
        apiHelper.patchRequest(Endpoints.MODIFY_ORDER_INBOUND.replace("{externalId}", externalId), modifyOrderRequest.toString());

        Assert.assertNotNull(context.getApiMessageResponseBody());
        log.info("Modify Order inbound response :{}", context.getApiMessageResponseBody());
    }
}
