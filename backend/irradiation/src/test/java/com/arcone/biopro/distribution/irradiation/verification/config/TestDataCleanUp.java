package com.arcone.biopro.distribution.irradiation.verification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;

@Component
public class TestDataCleanUp {
    private final Resource cleanUpAllQuery;
    private final Resource cleanUpAvailableInventoriesScenarioQuery;
    private final Resource cleanUpValidateInventoryScenarioQuery;
    private final Resource cleanUpGetInventoryScenarioQuery;
    private final ConnectionFactory connectionFactory;

    public TestDataCleanUp(
        @Value("classpath:/db/cleanUpAll.sql") Resource cleanUpAllQuery,
        @Value("classpath:/db/cleanUpAvailableInventoriesScenarios.sql") Resource cleanUpAvailableInventoriesScenarioQuery,
        @Value("classpath:/db/cleanUpValidateInventoriesScenarios.sql") Resource cleanUpValidateInventoryScenarioQuery,
        @Value("classpath:/db/cleanUpGetInventoryScenarios.sql") Resource cleanUpGetInventoryScenarioQuery,
        ConnectionFactory connectionFactory
    ) {
        this.cleanUpAllQuery = cleanUpAllQuery;
        this.connectionFactory = connectionFactory;
        this.cleanUpAvailableInventoriesScenarioQuery = cleanUpAvailableInventoriesScenarioQuery;
        this.cleanUpValidateInventoryScenarioQuery = cleanUpValidateInventoryScenarioQuery;
        this.cleanUpGetInventoryScenarioQuery = cleanUpGetInventoryScenarioQuery;
    }

    public void cleanUpAll() {
        var populator = new ResourceDatabasePopulator(cleanUpAllQuery);
        Mono.from(populator.populate(connectionFactory)).block();
    }

    public void cleanUpAvailableInventoryScenarios() {
        var populator = new ResourceDatabasePopulator(cleanUpAvailableInventoriesScenarioQuery);
        Mono.from(populator.populate(connectionFactory)).block();
    }

    public void cleanUpValidateInventoryScenarios() {
        var populator = new ResourceDatabasePopulator(cleanUpValidateInventoryScenarioQuery);
        Mono.from(populator.populate(connectionFactory)).block();
    }

    public void cleanUpGetInventoryScenarios() {
        var populator = new ResourceDatabasePopulator(cleanUpGetInventoryScenarioQuery);
        Mono.from(populator.populate(connectionFactory)).block();
    }
}
