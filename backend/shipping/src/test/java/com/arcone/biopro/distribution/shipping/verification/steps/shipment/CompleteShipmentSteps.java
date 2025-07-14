package com.arcone.biopro.distribution.shipping.verification.steps.shipment;

import com.arcone.biopro.distribution.shipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.shipping.verification.support.graphql.GraphQLMutationMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
public class CompleteShipmentSteps {

    @Autowired
    private SharedContext context;

    @Autowired
    private ApiHelper apiHelper;

    private Map apiResponse;


    @When("I request to complete a shipment.")
    public void iRequestToCompleteAShipment() {
        var response = apiHelper.graphQlRequest(GraphQLMutationMapper.completeShipmentMutation(context.getShipmentId(), "test-emplyee-id"), "completeShipment");
        Assert.assertNotNull(response);
        this.apiResponse = response;
    }


    @And("I should receive the unit {string}, product code {string} flagged as {string}")
    public void iShouldReceiveTheUnitProductCodeFlaggedAs(String unitNumber, String productCode, String productStatus) {
        Assert.assertNotNull(apiResponse);
        if (apiResponse.get("results") != null) {
            var results = (LinkedHashMap) apiResponse.get("results");
            if(results.get("validations") != null) {
                var validations = (List<LinkedHashMap>) results.get("validations");
                Assert.assertNotNull(validations);
                var notifications = validations.get(0);
                var inventoryDetails = (LinkedHashMap) notifications.get("inventoryResponseDTO");
                Assert.assertNotNull(inventoryDetails);
                Assert.assertEquals(unitNumber,inventoryDetails.get("unitNumber"));
                Assert.assertEquals(productCode,inventoryDetails.get("productCode"));
                Assert.assertEquals(productStatus,inventoryDetails.get("status"));

                var notificationDetails = (List<LinkedHashMap>) notifications.get("inventoryNotificationsDTO");
                Assert.assertNotNull(notificationDetails);

                var match = notificationDetails.stream().anyMatch(item -> item.get("errorName").equals(productStatus));
                Assert.assertTrue(match);
            }
        }
    }
}
