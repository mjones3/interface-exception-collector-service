package com.arcone.biopro.distribution.recoveredplasmashipping.verification.steps;

import com.arcone.biopro.distribution.recoveredplasmashipping.verification.controllers.CartonTestingController;
import com.arcone.biopro.distribution.recoveredplasmashipping.verification.support.SharedContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

@Slf4j
public class CloseCartonSteps {

    @Autowired
    private CartonTestingController cartonTestingController;
    @Autowired
    private SharedContext sharedContext;

    @Value("${default.employee.id}")
    private String employeeId;

    @Given("I request to close the carton.")
    public void closeCarton() {
        cartonTestingController.closeCarton(
            sharedContext.getCreateCartonResponseList().getFirst().get("id").toString(),
            employeeId,
            sharedContext.getLocationCode()
        );
    }

    @And("The carton status should be {string}.")
    public void theCartonStatusShouldBe(String status) {
        Assert.assertEquals(status, sharedContext.getLastCartonResponse().get("status").toString());
    }

    @And("The status of all {string} products in the carton should be updated to {string}.")
    public void theStatusOfAllProductsInTheCartonShouldBeUpdatedTo(String statusKey, String status) {
        if (statusKey.equalsIgnoreCase("packed")) {
            var packedProducts = (List<Map>) sharedContext.getLastCartonResponse().get("packedProducts");
            Assert.assertTrue(packedProducts.stream().allMatch(product -> product.get("status").equals(status)));
        } else if (statusKey.equals("verified")) {
            var packedProducts = (List<Map>) sharedContext.getLastCartonResponse().get("verifiedProducts");
            Assert.assertTrue(packedProducts.stream().allMatch(product -> product.get("status").equals(status)));
        } else {
            Assert.fail("Product status invalid for comparison");
        }
    }

}
