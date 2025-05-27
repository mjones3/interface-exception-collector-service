package com.arcone.biopro.distribution.inventory.verification.config;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CucumberHooks {

    @AfterAll
    public static void afterAll() {
        log.info("Running @AfterAll – Cleaning up all remaining test data once all scenarios are done");
        ApplicationContextProvider
            .getBean(TestDataCleanUp.class)
            .cleanUpAll();
    }

    @After("@inventoryAvailability")
    public static void afterInventoryAvailabilityScenarios() {
        log.info("Running @After each Inventory Availability Scenario");
        ApplicationContextProvider
            .getBean(TestDataCleanUp.class)
            .cleanUpAvailableInventoryScenarios();
    }

    @After("@validateInventory")
    public static void afterValidateInventoryScenarios() {
        log.info("Running @After each Validate Inventory Scenario");
        ApplicationContextProvider
            .getBean(TestDataCleanUp.class)
            .cleanUpValidateInventoryScenarios();
    }
}
