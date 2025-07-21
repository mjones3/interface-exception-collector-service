package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.application.dto.BatchProductDTO;
import com.arcone.biopro.distribution.irradiation.verification.api.support.IrradiationContext;
import com.arcone.biopro.distribution.irradiation.verification.common.GraphQlHelper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ContextConfiguration
public class DeviceValidateOnCloseBatchSteps {

    @Autowired
    private GraphQlHelper graphQlHelper;

    @Autowired
    private IrradiationContext irradiationContext;

    @Autowired
    private RepositorySteps repositorySteps;



    @Given("the device has an active batch with products:")
    public void theDeviceHasAnActiveBatchWithProducts(DataTable dataTable) {
        List<Map<String, String>> products = dataTable.asMaps(String.class, String.class);
        String deviceId = repositorySteps.getLastCreatedDeviceId();

        // Create batch for the device
        Long batchId = repositorySteps.createBatch(deviceId, LocalDateTime.now(), null);

        // Add products to the batch
        for (Map<String, String> product : products) {
            repositorySteps.createBatchItem(batchId, product.get("unitNumber"), product.get("productType"));
        }
    }

    @Given("the device is not associated with any open batch")
    public void theDeviceIsNotAssociatedWithAnyOpenBatch() {
        // No batch creation - device exists but has no open batch
    }

    @When("I scan the device {string} at location {string} for batch closing")
    public void iScanTheDeviceAtLocationForBatchClosing(String deviceId, String location) {
        Map<String, Object> variables = Map.of(
            "deviceId", deviceId,
            "location", location
        );

        var response = graphQlHelper.executeQuery("validateDeviceOnCloseBatch", variables, "validateDeviceOnCloseBatch", Object.class);

        if (response.getErrors().isEmpty()) {
            irradiationContext.setBatchProducts((List<BatchProductDTO>) response.getData());
        } else {
            irradiationContext.setResponseErrors(response.getErrors());
        }
    }

    @Then("I should see all products in the batch")
    public void iShouldSeeAllProductsInTheBatch() {
        assertNotNull(irradiationContext.getBatchProducts());
        // Additional validation can be added based on the response structure
    }

    @Then("I should see notification {string}")
    public void iShouldSeeNotification(String expectedMessage) {
        assertEquals(expectedMessage, irradiationContext.getResponseErrors().get(0).getMessage());
    }
}
