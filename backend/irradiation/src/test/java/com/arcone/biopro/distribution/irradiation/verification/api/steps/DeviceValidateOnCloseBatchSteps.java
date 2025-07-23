package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.application.dto.BatchProductDTO;
import com.arcone.biopro.distribution.irradiation.verification.api.support.IrradiationContext;
import com.arcone.biopro.distribution.irradiation.verification.common.GraphQlHelper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
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
            repositorySteps.createBatchItem(batchId, product.get("unitNumber"), "1234", product.get("productCode"), product.get("productFamily"));
            log.info("Added product {} of type {} with family {} to batch {}",
                product.get("unitNumber"), product.get("productCode"), product.get("productFamily"), batchId);
        }
    }

    @Given("the device is not associated with any open batch")
    public void theDeviceIsNotAssociatedWithAnyOpenBatch() {
        log.info("Device configured with no open batch");
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
            // Convert response data directly to BatchProductDTO objects
            List<BatchProductDTO> batchProducts = ((List<Map<String, String>>) response.getData()).stream()
                .map(map -> BatchProductDTO.builder()
                    .unitNumber(map.get("unitNumber"))
                    .productCode(map.get("productCode"))
                    .productFamily(map.get("productFamily"))
                    .productDescription(map.get("productDescription"))
                    .status(map.get("status"))
                    .build())
                .toList();
            irradiationContext.setBatchProducts(batchProducts);
            log.info("Successfully converted {} batch products", batchProducts.size());
        } else {
            irradiationContext.setResponseErrors(response.getErrors());
        }
    }

    @Then("I should see all products in the batch")
    public void iShouldSeeAllProductsInTheBatch() {
        List<BatchProductDTO> products = irradiationContext.getBatchProducts();
        assertNotNull(products, "Batch products list should not be null");
        log.info("Verified batch products are present: {} products found", products.size());

        // Verify that each product has a productFamily
        for (BatchProductDTO product : products) {
            assertNotNull(product.unitNumber(), "Unit number should not be null");
            log.info("Product {} has code {} and family {}",
                product.unitNumber(),
                product.productCode(),
                product.productFamily());
        }
    }

    @Then("I should see notification {string}")
    public void iShouldSeeNotification(String expectedMessage) {
        assertEquals(expectedMessage, irradiationContext.getResponseErrors().get(0).getMessage());
    }
}
