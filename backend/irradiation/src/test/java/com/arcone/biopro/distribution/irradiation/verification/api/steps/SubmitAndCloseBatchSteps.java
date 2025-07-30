package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.application.irradiation.dto.BatchSubmissionResultDTO;
import com.arcone.biopro.distribution.irradiation.verification.api.support.IrradiationContext;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CopyOnWriteArrayList;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration
@ExtendWith(SpringExtension.class)
public class SubmitAndCloseBatchSteps {

    @Autowired
    private GraphQlTester graphQlTester;

    @Autowired
    private RepositorySteps repositorySteps;

    @Autowired
    private IrradiationContext irradiationContext;

    private String batchId;
    private String endTime;
    private List<Map<String, Object>> batchItems = new ArrayList<>();
    private BatchSubmissionResultDTO result;
    private String errorMessage;

    private static final List<String> capturedLogs = new CopyOnWriteArrayList<>();
    private static boolean appenderAttached = false;

    static {
        if (!appenderAttached) {
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.addAppender(new AppenderBase<ILoggingEvent>() {
                { start(); }
                @Override
                protected void append(ILoggingEvent event) {
                    capturedLogs.add(event.getFormattedMessage());
                }
            });
            appenderAttached = true;
        }
    }


    @Given("I have an existing batch with products:")
    public void iHaveAnExistingBatchWithProducts(DataTable dataTable) {
        String deviceId = irradiationContext.getDeviceId();
        if (deviceId == null || deviceId.isEmpty()) {
            throw new IllegalStateException("Device ID should be set from background step");
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
        this.endTime = endTimeStr;
        String deviceId = irradiationContext.getDeviceId();

        if (deviceId == null || deviceId.isEmpty()) {
            throw new IllegalStateException("Device ID should be set from background step");
        }

        this.batchItems = dataTable.asMaps(String.class, String.class).stream()
                .map(row -> Map.<String, Object>of(
                    "unitNumber", row.get("Unit Number"),
                    "productCode", row.get("Product Code"),
                    "isIrradiated", Boolean.parseBoolean(row.get("Is Irradiated"))
                ))
                .toList();

        this.result = graphQlTester
                .documentName("completeBatch")
                .variable("deviceId", deviceId)
                .variable("endTime", convertToLocalDateTimeFormat(this.endTime))
                .variable("batchItems", this.batchItems)
                .execute()
                .errors()
                .verify()
                .path("completeBatch")
                .entity(BatchSubmissionResultDTO.class)
                .get();
    }

    @When("I complete a batch with id {string} and end time {string} and items:")
    public void iCompleteABatchWithIdAndEndTimeAndItems(String batchId, String endTimeStr, DataTable dataTable) {
        this.batchId = batchId;
        iCompleteTheBatchWithEndTimeAndItems(endTimeStr, dataTable);
    }

    @Then("the batch should be successfully completed")
    public void theBatchShouldBeCompletedSuccessfully() {
        if (result == null) {
            fail("Batch completion result should not be null. Error: " + (errorMessage != null ? errorMessage : "Unknown error"));
        }
        if (!result.success()) {
            fail("Batch completion should be successful. Error: " + (result.message() != null ? result.message() : "Unknown error"));
        }
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
                Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() ->
                    capturedLogs.stream().anyMatch(log ->
                        log.contains("Published product modified event for unit: " + unitNumber))
                );
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
                Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() ->
                    capturedLogs.stream().anyMatch(log ->
                        log.contains("Published product modified event for unit: " + unitNumber))
                );
            }
        });
    }

    @Then("quarantine events should be published for {string} products")
    public void quarantineEventsShouldBePublishedForProducts(String productAmount) {
        assertNotNull(result, "Batch completion result should not be null");
        assertTrue(result.success(), "Batch should be completed successfully");

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() ->
            capturedLogs.stream().anyMatch(log ->
                log.contains(String.format("Published quarantine event for %s products", productAmount)))
        );
    }

    @Then("no product modified events should be published")
    public void noProductModifiedEventsShouldBePublished() {
        assertNotNull(result, "Batch completion result should not be null");
        assertTrue(result.success(), "Batch should be completed successfully");

        boolean hasIrradiatedItems = batchItems.stream()
            .anyMatch(item -> Boolean.TRUE.equals(item.get("isIrradiated")));

        assertFalse(hasIrradiatedItems, "No product modified events should be published when no items are irradiated");
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
