package com.arcone.biopro.distribution.shipping.verification.steps.shipment;

import com.arcone.biopro.distribution.shipping.verification.pages.distribution.FillProductsPage;
import com.arcone.biopro.distribution.shipping.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shipping.verification.support.SharedContext;
import com.arcone.biopro.distribution.shipping.verification.support.controllers.ShipmentTestingController;
import com.arcone.biopro.distribution.shipping.verification.support.graphql.GraphQLMutationMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
public class RemoveProductsFromShipmentSteps {

    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    private SharedContext context;

    @Autowired
    private ShipmentTestingController shipmentTestingController;

    @Autowired
    private FillProductsPage fillProductsPage;

    private Map removeProductResultsList;

    @When("I remove the product {string} with product code {string} from the line item {string} {string}.")
    public void removeProductFromShipment(String unitNumber, String productCode, String family, String bloodType) {
        log.debug("Removing product {} with product code {} from the line item {} {}.", unitNumber, productCode, family, bloodType);

        Long shipmentItemId = shipmentTestingController.getShipmentItemId(context.getShipmentId(), family, bloodType);
        String removeProductMutation = GraphQLMutationMapper.unpackItemsMutation(shipmentItemId, context.getFacility(), context.getEmployeeId(), unitNumber, productCode);
        var response = apiHelper.graphQlRequest(removeProductMutation, "unpackItems");

        if (!response.get("ruleCode").toString().contains("400")) {
            context.setTotalRemoved(context.getTotalRemoved() + 1);
            Map results = (Map) response.get("results");
            this.removeProductResultsList = ((List<Map>) results.get("results")).stream().toList().get(0);
        }
    }

    @Then("The product {string} and {string} should not be part of the shipment.")
    public void theProductAndShouldNotBePartOfTheShipment(String unitNumber, String productCode) {
        log.debug("Verifying that the product {} and {} are not part of the shipment.", unitNumber, productCode);

        List<Map> packedItems = this.removeProductResultsList.get("packedItems") != null ? (List<Map>) this.removeProductResultsList.get("packedItems") : List.of();
        packedItems.forEach(item -> {
            if (item.get("unitNumber").equals(unitNumber) && item.get("productCode").equals(productCode)) {
                throw new AssertionError("Product " + unitNumber + " and " + productCode + " are still part of the shipment.");
            }
        });
    }

    @And("I should have {int} items {string}.")
    public void iShouldHaveExpectedQtyItems(Integer expectedQuantity, String status) {
        log.debug("Verifying that I have {} items {}.", expectedQuantity, status);

        List<Map> packedItems = this.removeProductResultsList.get("packedItems") != null ? (List<Map>) this.removeProductResultsList.get("packedItems") : List.of();

        if (status.equalsIgnoreCase("packed")) {
            Assert.assertEquals(expectedQuantity, Integer.valueOf(packedItems.size()));
        } else if (status.equalsIgnoreCase("verified")) {
            var verified = packedItems.stream().filter(item -> item.get("secondVerification").equals("COMPLETE")).toList();
            Assert.assertEquals(expectedQuantity, Integer.valueOf(verified.size()));
        }
    }

    @When("I select the product {string} with product code {string}.")
    public void iSelectTheProductWithProductCode(String unitNumber, String productCode) throws InterruptedException {
        fillProductsPage.selectProduct(unitNumber, productCode);
    }
}
