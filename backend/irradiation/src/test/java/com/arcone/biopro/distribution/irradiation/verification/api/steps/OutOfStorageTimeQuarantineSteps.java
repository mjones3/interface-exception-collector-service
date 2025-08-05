package com.arcone.biopro.distribution.irradiation.verification.api.steps;

import com.arcone.biopro.distribution.irradiation.application.usecase.ProductStoredUseCase;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.awaitility.Awaitility;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration
public class OutOfStorageTimeQuarantineSteps {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OutOfStorageTimeQuarantineSteps.class);
    private static final List<String> capturedLogs = new CopyOnWriteArrayList<>();
    private static boolean appenderAttached = false;
    private int quarantineLogCountBeforeStep = 0;

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

    @Autowired
    private ProductStoredUseCase productStoredUseCase;

    @Autowired
    private RepositorySteps repositorySteps;

    @Given("I have an irradiation batch with the following products:")
    public void iHaveAnIrradiationBatchWithTheFollowingProducts(DataTable dataTable) {
        createBatchWithProducts(dataTable, LocalDateTime.now());
    }

    @Given("I have an open irradiation batch with the following products:")
    public void iHaveAnOpenIrradiationBatchWithTheFollowingProducts(DataTable dataTable) {
        createBatchWithProducts(dataTable, null);
    }

    private void createBatchWithProducts(DataTable dataTable, LocalDateTime endTime) {
        dataTable.asMaps(String.class, String.class).forEach(product -> {
            String unitNumber = product.get("Unit Number");
            String productCode = product.get("Product Code");
            String device = product.get("Device");
            String location = product.get("Location");
            String productFamily = product.get("Product Family");
            String lotNumber = product.get("Lot Number");

            repositorySteps.iHaveAValidDeviceAtLocation(device, location, "ACTIVE");
            Long batchId = repositorySteps.createBatch(device, LocalDateTime.now(), endTime);
            repositorySteps.createBatchItem(batchId, unitNumber, lotNumber, productCode, productFamily);

            log.info("Created {} product: unit={}, product={}, device={}",
                endTime == null ? "open batch" : "closed batch", unitNumber, productCode, device);
        });
    }

    @And("the irradiation batch for unit {string} was started {string} hours ago")
    public void theIrradiationBatchForUnitWasStartedHoursAgo(String unitNumber, String hoursAgo) {
        updateBatchStartTime(unitNumber, Long.parseLong(hoursAgo), "hours");
    }

    @And("the irradiation batch for unit {string} was started {string} minutes ago")
    public void theIrradiationBatchForUnitWasStartedMinutesAgo(String unitNumber, String minutesAgo) {
        updateBatchStartTime(unitNumber, Long.parseLong(minutesAgo), "minutes");
    }

    private void updateBatchStartTime(String unitNumber, long timeValue, String timeUnit) {
        LocalDateTime batchStartTime = "hours".equals(timeUnit)
            ? LocalDateTime.now().minusHours(timeValue)
            : LocalDateTime.now().minusMinutes(timeValue);
        repositorySteps.updateBatchStartTimeForUnit(unitNumber, batchStartTime);
        log.info("Updated batch start time for unit {} to {} {} ago", unitNumber, timeValue, timeUnit);
    }

    @When("I receive a product stored event for unit {string} with product {string} stored {string} minutes ago")
    public void iReceiveAProductStoredEventForUnitWithProductStoredMinutesAgo(String unitNumber, String productCode, String minutesAgo) {
        processProductStoredEvent(unitNumber, productCode, Long.parseLong(minutesAgo), "minutes");
    }

    @When("I receive a product stored event for unit {string} with product {string} stored {string} hours ago")
    public void iReceiveAProductStoredEventForUnitWithProductStoredHoursAgo(String unitNumber, String productCode, String hoursAgo) {
        processProductStoredEvent(unitNumber, productCode, Long.parseLong(hoursAgo), "hours");
    }

    private void processProductStoredEvent(String unitNumber, String productCode, long timeValue, String timeUnit) {
        quarantineLogCountBeforeStep = getQuarantineLogCount(unitNumber, productCode);

        ZonedDateTime storageTime = "hours".equals(timeUnit)
            ? ZonedDateTime.now().minusHours(timeValue)
            : ZonedDateTime.now().minusMinutes(timeValue);

        ProductStoredUseCase.Input input = ProductStoredUseCase.Input.builder()
            .unitNumber(unitNumber)
            .productCode(productCode)
            .deviceStored("STORAGE_DEVICE")
            .deviceUsed("DEVICE_USED")
            .storageLocation("STORAGE_LOC")
            .location("123456789")
            .locationType("WAREHOUSE")
            .storageTime(storageTime)
            .performedBy("TEST_USER")
            .build();

        productStoredUseCase.execute(input).block();
        log.info("Processed product stored event for unit {} stored {} {} ago", unitNumber, timeValue, timeUnit);
    }

    private int getQuarantineLogCount(String unitNumber, String productCode) {
        return (int) capturedLogs.stream()
            .filter(logMessage -> logMessage.contains("has exceeded out-of-storage time limit, triggering quarantine") &&
                logMessage.contains(unitNumber) && logMessage.contains(productCode))
            .count();
    }

    @Then("the product {string} should be quarantined with reason {string}")
    public void theProductShouldBeQuarantinedWithReason(String unitNumber, String reason) {
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(() ->
            capturedLogs.stream().anyMatch(logMessage ->
                logMessage.contains("has exceeded out-of-storage time limit, triggering quarantine") &&
                logMessage.contains(unitNumber)
            )
        );
        log.info("Verified product {} was quarantined with reason {}", unitNumber, reason);
    }

    @Then("the product {string} should not be quarantined")
    public void theProductShouldNotBeQuarantined(String unitNumber) {
        waitAndVerifyNoQuarantine(unitNumber, null);
    }

    @Then("the product {string} should not be quarantined for product {string}")
    public void theProductShouldNotBeQuarantinedForProduct(String unitNumber, String productCode) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long quarantineLogCountAfter = getQuarantineLogCount(unitNumber, productCode);
        assertEquals(quarantineLogCountBeforeStep, quarantineLogCountAfter,
            "No new quarantine should occur for " + productCode + " in unit " + unitNumber);
        log.info("Verified product {} in unit {} was not quarantined on this event", productCode, unitNumber);
    }

    private void waitAndVerifyNoQuarantine(String unitNumber, String productCode) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean quarantineLogged = capturedLogs.stream().anyMatch(logMessage ->
            logMessage.contains("has exceeded out-of-storage time limit, triggering quarantine") &&
            logMessage.contains(unitNumber) &&
            (productCode == null || logMessage.contains(productCode))
        );

        assertFalse(quarantineLogged, "Product " + unitNumber + " should not have been quarantined");
        log.info("Verified product {} was not quarantined", unitNumber);
    }

    @Then("the timing rule validated flag should be {string} for unit {string} and product {string}")
    public void theProductStoredEventProcessedFlagShouldBe(String expectedFlag, String unitNumber, String productCode) {
        boolean expected = Boolean.parseBoolean(expectedFlag);
        boolean actual = repositorySteps.isProductStoredEventProcessed(unitNumber, productCode);
        assertEquals(expected, actual,
            String.format("Timing rule validated flag should be %s for unit %s and product %s",
                expectedFlag, unitNumber, productCode));
        log.info("Verified timing rule validated flag is {} for unit {} and product {}",
            expectedFlag, unitNumber, productCode);
    }

    @Then("the system should log that the product stored event was already processed for unit {string} and product {string}")
    public void theSystemShouldLogThatTheProductStoredEventWasAlreadyProcessed(String unitNumber, String productCode) {
        Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() ->
            capturedLogs.stream().anyMatch(logMessage ->
                logMessage.contains("Product stored event already processed for unit: " + unitNumber + ", product: " + productCode + ", ignoring")
            )
        );
        log.info("Verified system logged that product stored event was already processed for unit {} and product {}", unitNumber, productCode);
    }

    @When("the batch is closed for unit {string} and product {string}")
    public void theBatchIsClosedForUnitAndProduct(String unitNumber, String productCode) {
        repositorySteps.closeBatchForUnit(unitNumber);
        log.info("Closed batch for unit {} and product {}", unitNumber, productCode);
    }
}
