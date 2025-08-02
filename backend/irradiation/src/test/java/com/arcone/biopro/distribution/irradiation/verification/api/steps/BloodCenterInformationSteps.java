package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchSubmissionResultDTO;
import com.arcone.biopro.distribution.irradiation.verification.api.support.IrradiationContext;
import com.arcone.biopro.distribution.irradiation.verification.common.GraphQlHelper;
import com.arcone.biopro.distribution.irradiation.verification.common.GraphQlResponse;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration
public class BloodCenterInformationSteps {

    @Autowired
    private GraphQlHelper graphQlHelper;

    @Autowired
    private RepositorySteps repositorySteps;

    @Autowired
    private IrradiationContext irradiationContext;

    private List<Map<String, Object>> batchProducts = new ArrayList<>();
    private BatchSubmissionResultDTO result;
    private String errorMessage;

    @Given("an active device {string} exists at location {string}")
    public void anActiveDeviceExistsAtLocation(String deviceId, String location) {
        repositorySteps.createDevice(deviceId, location, "ACTIVE");
        irradiationContext.setDeviceId(deviceId);
    }

    @Given("I prepare a batch with imported units:")
    public void iPrepareABatchWithImportedUnits(DataTable dataTable) {
        batchProducts.clear();
        List<Map<String, String>> units = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> unit : units) {
            Map<String, Object> product = new HashMap<>();
            product.put("unitNumber", unit.get("Unit Number"));
            product.put("productCode", unit.get("Product Code"));
            product.put("lotNumber", "123");

            batchProducts.add(product);
        }
    }

    @Given("I prepare a batch with mixed units:")
    public void iPrepareABatchWithMixedUnits(DataTable dataTable) {
        batchProducts.clear();
        List<Map<String, String>> units = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> unit : units) {
            Map<String, Object> product = new HashMap<>();
            product.put("unitNumber", unit.get("Unit Number"));
            product.put("productCode", unit.get("Product Code"));
            product.put("lotNumber", "123");

            batchProducts.add(product);
        }
    }

    @Given("I prepare a batch with imported unit {string}")
    public void iPrepareABatchWithImportedUnit(String unitNumber) {
        batchProducts.clear();
        Map<String, Object> product = new HashMap<>();
        product.put("unitNumber", unitNumber);
        product.put("productCode", "E003300");
        product.put("lotNumber", "123");

        batchProducts.add(product);
    }

    @When("I submit the batch with blood center information:")
    public void iSubmitTheBatchWithBloodCenterInformation(DataTable dataTable) {
        List<Map<String, String>> bloodCenters = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> center : bloodCenters) {
            String unitNumber = center.get("Unit Number");
            batchProducts.stream()
                .filter(product -> unitNumber.equals(product.get("unitNumber")))
                .findFirst()
                .ifPresent(product -> {
                    product.put("bloodCenterName", center.get("Blood Center Name"));
                    product.put("address", center.get("Address"));
                    product.put("registrationNumber", center.get("Registration Number"));
                    product.put("licenseNumber", center.get("License Number"));
                });
        }

        submitBatch();
    }

    @When("I submit the batch with blood center information for imported units only:")
    public void iSubmitTheBatchWithBloodCenterInformationForImportedUnitsOnly(DataTable dataTable) {
        iSubmitTheBatchWithBloodCenterInformation(dataTable);
    }

    @When("I submit the batch without blood center information")
    public void iSubmitTheBatchWithoutBloodCenterInformation() {
        submitBatch();
    }

    @When("I submit the batch with incomplete blood center information:")
    public void iSubmitTheBatchWithIncompleteBloodCenterInformation(DataTable dataTable) {
        iSubmitTheBatchWithBloodCenterInformation(dataTable);
    }

    private void submitBatch() {
        String deviceId = irradiationContext.getDeviceId();

        Map<String, Object> input = new HashMap<>();
        input.put("deviceId", deviceId);
        input.put("startTime", LocalDateTime.now().toString());
        input.put("batchItems", batchProducts);

        Map<String, Object> variables = new HashMap<>();
        variables.put("input", input);

        GraphQlResponse<BatchSubmissionResultDTO> response = graphQlHelper.executeQuery(
            "submitBatch", variables, "submitBatch", BatchSubmissionResultDTO.class);

        if (response.hasErrors()) {
            errorMessage = response.getErrors().get(0).getMessage();
        } else {
            result = response.getData();
        }
    }

    @Then("the batch is created successfully")
    public void theBatchIsCreatedSuccessfully() {
        assertNotNull(result, "Batch submission result should not be null");
        assertTrue(result.success(), "Batch should be created successfully");
    }

    @Then("each unit contains the provided blood center information")
    public void eachUnitContainsTheProvidedBloodCenterInformation() {
        assertNotNull(result, "Batch submission result should not be null");
        assertTrue(result.success(), "Batch should be created successfully");

        for (Map<String, Object> product : batchProducts) {
            if (product.containsKey("bloodCenterName")) {
                String unitNumber = (String) product.get("unitNumber");
                repositorySteps.verifyBloodCenterInformation(
                    unitNumber,
                    (String) product.get("bloodCenterName"),
                    (String) product.get("address"),
                    (String) product.get("registrationNumber"),
                    (String) product.get("licenseNumber")
                );
            }
        }
    }

    @Then("unit {string} contains blood center information")
    public void unitContainsBloodCenterInformation(String unitNumber) {
        assertNotNull(result, "Batch submission result should not be null");
        assertTrue(result.success(), "Batch should be created successfully");

        Map<String, Object> product = batchProducts.stream()
            .filter(p -> unitNumber.equals(p.get("unitNumber")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Product not found for unit: " + unitNumber));

        repositorySteps.verifyBloodCenterInformation(
            unitNumber,
            (String) product.get("bloodCenterName"),
            (String) product.get("address"),
            (String) product.get("registrationNumber"),
            (String) product.get("licenseNumber")
        );
    }

    @Then("unit {string} has no blood center information")
    public void unitHasNoBloodCenterInformation(String unitNumber) {
        assertNotNull(result, "Batch submission result should not be null");
        assertTrue(result.success(), "Batch should be created successfully");

        repositorySteps.verifyNoBloodCenterInformation(unitNumber);
    }

    @Then("I receive confirmation {string}")
    public void iReceiveConfirmation(String expectedMessage) {
        assertNotNull(result, "Batch submission result should not be null");
        assertEquals(expectedMessage, result.message(), "Confirmation message should match");
    }

    @Then("the batch submission is rejected")
    public void theBatchSubmissionIsRejected() {
        assertTrue(result == null || !result.success() || errorMessage != null,
                  "Batch submission should be rejected");
    }

    @Then("I receive error {string}")
    public void iReceiveError(String expectedError) {
        assertTrue(errorMessage != null && errorMessage.contains(expectedError) ||
                  (result != null && !result.success() && result.message() != null && result.message().contains(expectedError)),
                  "Expected error message: " + expectedError + ", but got: " +
                  (errorMessage != null ? errorMessage : (result != null ? result.message() : "null")));
    }
}
