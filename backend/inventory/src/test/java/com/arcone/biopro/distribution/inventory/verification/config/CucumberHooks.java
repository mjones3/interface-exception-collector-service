package com.arcone.biopro.distribution.inventory.verification.config;

import io.cucumber.java.After;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CucumberHooks {

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

    @After("@getInventory")
    public static void afterGetInventoryScenarios() {
        log.info("Running @After each Get Inventory Scenario");
        ApplicationContextProvider
            .getBean(TestDataCleanUp.class)
            .cleanUpGetInventoryScenarios();
    }

    @After("@cleanUpAll")
    public static void cleanUpAll() {
        log.info("Running @After cleanUpAll");
        ApplicationContextProvider
            .getBean(TestDataCleanUp.class)
            .cleanUpGetInventoryScenarios();
    }
}
