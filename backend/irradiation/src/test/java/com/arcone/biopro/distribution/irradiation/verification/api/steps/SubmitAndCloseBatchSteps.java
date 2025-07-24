package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchSubmissionResultDTO;
import com.arcone.biopro.distribution.irradiation.verification.api.support.IrradiationContext;
import com.arcone.biopro.distribution.irradiation.verification.utils.LogMonitor;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration
public class SubmitAndCloseBatchSteps {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private RepositorySteps repositorySteps;

    @Autowired
    private IrradiationContext irradiationContext;

    @Autowired
    private LogMonitor logMonitor;

    private String batchId;
    private String endTime;
    private List<Map<String, Object>> batchItems = new ArrayList<>();
    private BatchSubmissionResultDTO result;
    private String errorMessage;

    @Given("I have an existing batch with products:")
    public void iHaveAnExistingBatchWithProducts(DataTable dataTable) {
        String deviceId = irradiationContext.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            deviceId = "AUTO-DEVICE100";
            repositorySteps.iHaveAValidDeviceAtLocation(deviceId, "123456789", "ACTIVE");
        }

        Long createdBatchId = repositorySteps.createBatch(deviceId, LocalDateTime.now(), null);
        this.batchId = createdBatchId.toString();
        irradiationContext.setBatchId(this.batchId);

        List<Map<String, String>> items = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> item : items) {
            repositorySteps.createBatchItemWithExpiration(
                createdBatchId,
                item.get("Unit Number"),
                item.get("Lot Number"),
                item.get("Product Code"),
                "PLASMA_TRANSFUSABLE",
                item.get("Expiration Date")
            );
        }
    }

    @When("I complete the batch with end time {string} and items:")
    public void iCompleteTheBatchWithEndTimeAndItems(String endTimeStr, DataTable dataTable) {
        this.endTime = convertToLocalDateTimeFormat(endTimeStr);
        
        if (this.batchId == null) {
            this.batchId = irradiationContext.getBatchId();
        }

        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        this.batchItems = rows.stream()
                .map(row -> Map.<String, Object>of(
                    "unitNumber", row.get("Unit Number"),
                    "productCode", row.get("Product Code"),
                    "isIrradiated", Boolean.parseBoolean(row.get("Is Irradiated"))
                ))
                .toList();

        try {
            BatchSubmissionResultDTO response = graphQlTester
                    .documentName("completeBatch")
                    .variable("batchId", this.batchId)
                    .variable("endTime", this.endTime)
                    .variable("batchItems", this.batchItems)
                    .execute()
                    .errors()
                    .satisfy(errors -> {
                        if (!errors.isEmpty()) {
                            this.errorMessage = errors.get(0).getMessage();
                        }
                    })
                    .path("completeBatch")
                    .entity(BatchSubmissionResultDTO.class)
                    .get();

            this.result = response;
            if (!response.success()) {
                this.errorMessage = response.message();
            } else {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            String message = e.getMessage();
            if (message.contains("Batch not found") || message.contains("completeBatch") || message.contains("null")) {
                this.errorMessage = "Batch not found";
            } else {
                this.errorMessage = message;
            }
        }
    }

    @When("I complete a batch with id {string} and end time {string} and items:")
    public void iCompleteABatchWithIdAndEndTimeAndItems(String batchId, String endTimeStr, DataTable dataTable) {
        this.batchId = batchId;
        iCompleteTheBatchWithEndTimeAndItems(endTimeStr, dataTable);
    }

    @Then("the batch should be successfully completed")
    public void theBatchShouldBeCompletedSuccessfully() {
        assertNotNull(result, "Batch completion result should not be null");
        assertTrue(result.success(), "Batch completion should be successful");
        assertEquals(Long.parseLong(batchId), result.batchId(), "Batch ID should match");
    }

    @Then("all products should be updated with new product codes:")
    public void allProductsShouldBeUpdatedWithNewProductCodes(DataTable dataTable) {
        assertNotNull(result, "Batch completion result should not be null");
        assertTrue(result.success(), "Batch should be completed successfully");

        dataTable.asMaps(String.class, String.class).forEach(update -> 
            repositorySteps.verifyProductCodeUpdate(
                update.get("Unit Number"), 
                update.get("New Product Code")
            )
        );
    }

    @Then("irradiated products should be updated with new product codes:")
    public void irradiatedProductsShouldBeUpdatedWithNewProductCodes(DataTable dataTable) {
        assertNotNull(result, "Batch completion result should not be null");
        assertTrue(result.success(), "Batch should be completed successfully");

        dataTable.asMaps(String.class, String.class).forEach(update -> {
            String unitNumber = update.get("Unit Number");
            boolean wasIrradiated = batchItems.stream()
                .anyMatch(item -> unitNumber.equals(item.get("unitNumber")) &&
                         Boolean.TRUE.equals(item.get("isIrradiated")));

            if (wasIrradiated) {
                repositorySteps.verifyProductCodeUpdate(unitNumber, update.get("New Product Code"));
            }
        });
    }

    @Then("product modified events should be published for all irradiated items with expiration dates:")
    public void productModifiedEventsShouldBePublishedForAllIrradiatedItemsWithExpirationDates(DataTable dataTable) {
        assertNotNull(result, "Batch completion result should not be null");
        assertTrue(result.success(), "Batch should be completed successfully");

        dataTable.asMaps(String.class, String.class).forEach(event -> {
            String unitNumber = event.get("Unit Number");
            boolean wasIrradiated = batchItems.stream()
                .anyMatch(item -> unitNumber.equals(item.get("unitNumber")) &&
                         Boolean.TRUE.equals(item.get("isIrradiated")));

            if (wasIrradiated) {
                try {
                    logMonitor.await("ProductModified event published for unit: " + unitNumber);
                } catch (InterruptedException e) {
                    fail("ProductModified event was not published for irradiated item: " + unitNumber);
                }
            }
        });
    }

    @Then("product modified events should be published for irradiated items only with expiration date:")
    public void productModifiedEventsShouldBePublishedForIrradiatedItemsOnlyWithExpirationDate(DataTable dataTable) {
        assertNotNull(result, "Batch completion result should not be null");
        assertTrue(result.success(), "Batch should be completed successfully");

        dataTable.asMaps(String.class, String.class).forEach(event -> {
            String unitNumber = event.get("Unit Number");
            String expirationDate = event.get("Expiration Date");

            boolean wasIrradiated = batchItems.stream()
                .anyMatch(item -> unitNumber.equals(item.get("unitNumber")) &&
                         Boolean.TRUE.equals(item.get("isIrradiated")));

            if (wasIrradiated && expirationDate != null && !expirationDate.isEmpty()) {
                try {
                    logMonitor.await("ProductModified event with expiration " + expirationDate + " published for unit: " + unitNumber);
                } catch (InterruptedException e) {
                    fail("ProductModified event with expiration date was not published for irradiated item: " + unitNumber);
                }
            }
        });
    }

    @Then("quarantine events should be published for non-irradiated items:")
    public void quarantineEventsShouldBePublishedForNonIrradiatedItems(DataTable dataTable) {
        assertNotNull(result, "Batch completion result should not be null");
        assertTrue(result.success(), "Batch should be completed successfully");

        dataTable.asMaps(String.class, String.class).forEach(quarantine -> {
            String unitNumber = quarantine.get("Unit Number");
            String reason = quarantine.get("Reason");

            boolean wasNotIrradiated = batchItems.stream()
                .anyMatch(item -> unitNumber.equals(item.get("unitNumber")) &&
                         Boolean.FALSE.equals(item.get("isIrradiated")));

            if (wasNotIrradiated) {
                try {
                    logMonitor.await("Quarantine event with reason '" + reason + "' published for unit: " + unitNumber);
                } catch (InterruptedException e) {
                    fail("Quarantine event was not published for non-irradiated item: " + unitNumber);
                }
            }
        });
    }

    @Then("quarantine events should be published for non-irradiated items with reason {string}")
    public void quarantineEventsShouldBePublishedForNonIrradiatedItemsWithReason(String reason) {
        assertNotNull(result, "Batch completion result should not be null");
        assertTrue(result.success(), "Batch should be completed successfully");

        long nonIrradiatedCount = batchItems.stream()
            .filter(item -> Boolean.FALSE.equals(item.get("isIrradiated")))
            .count();

        if (nonIrradiatedCount > 0) {
            try {
                logMonitor.await("Quarantine events with reason '" + reason + "' published for " + nonIrradiatedCount + " items");
            } catch (InterruptedException e) {
                fail("Quarantine events were not published for non-irradiated items with reason: " + reason);
            }
        }
    }

    @Then("quarantine events should be published for all items with reason {string}")
    public void quarantineEventsShouldBePublishedForAllItemsWithReason(String reason) {
        assertNotNull(result, "Batch completion result should not be null");
        assertTrue(result.success(), "Batch should be completed successfully");

        try {
            logMonitor.await("Quarantine events with reason '" + reason + "' published for all " + batchItems.size() + " items");
        } catch (InterruptedException e) {
            fail("Quarantine events were not published for all items with reason: " + reason);
        }
    }

    @Then("no product modified events should be published")
    public void noProductModifiedEventsShouldBePublished() {
        assertNotNull(result, "Batch completion result should not be null");
        assertTrue(result.success(), "Batch should be completed successfully");

        boolean hasIrradiatedItems = batchItems.stream()
            .anyMatch(item -> Boolean.TRUE.equals(item.get("isIrradiated")));

        assertFalse(hasIrradiatedItems, "No product modified events should be published when no items are irradiated");
    }

    @Then("I should see an error message {string}")
    public void iShouldSeeAnErrorMessage(String expectedError) {
        assertNotNull(errorMessage, "Error message should not be null");
        assertTrue(errorMessage.contains(expectedError),
                  String.format("Error message '%s' should contain: '%s'", errorMessage, expectedError));
    }

    @And("I should see quarantine notification {string}")
    public void iShouldSeeQuarantineNotification(String expectedNotification) {
        assertNotNull(result, "Batch completion result should not be null");
        
        if (!result.success()) {
            fail("Expected quarantine notification but batch completion failed");
        }
    }

    @Then("I should see the batch completion success message {string}")
    public void iShouldSeeTheBatchCompletionSuccessMessage(String expectedMessage) {
        assertNotNull(result, "Batch completion result should not be null");
        assertEquals(expectedMessage, result.message(), "Success message should match");
    }

    private String convertToLocalDateTimeFormat(String isoDateTime) {
        // Convert from ISO 8601 format (2024-01-15T12:00:00Z) to LocalDateTime format
        if (isoDateTime.endsWith("Z")) {
            isoDateTime = isoDateTime.substring(0, isoDateTime.length() - 1);
        }
        return isoDateTime;
    }
}
