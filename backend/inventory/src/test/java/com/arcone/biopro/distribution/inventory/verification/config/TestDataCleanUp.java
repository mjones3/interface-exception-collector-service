package com.arcone.biopro.distribution.inventory.verification.config;

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
    private final ConnectionFactory connectionFactory;

    public TestDataCleanUp(
        @Value("classpath:/db/cleanUpAll.sql") Resource cleanUpAllQuery,
        @Value("classpath:/db/cleanUpAvailableInventoriesScenarios.sql") Resource cleanUpAvailableInventoriesScenarioQuery,
        ConnectionFactory connectionFactory
    ) {
        this.cleanUpAllQuery = cleanUpAllQuery;
        this.connectionFactory = connectionFactory;
        this.cleanUpAvailableInventoriesScenarioQuery = cleanUpAvailableInventoriesScenarioQuery;
    }

    public void cleanUpAll() {
        var populator = new ResourceDatabasePopulator(cleanUpAllQuery);
        Mono.from(populator.populate(connectionFactory)).block();
    }

    public void cleanUpAvailableInventoryScenariosAll() {
        var populator = new ResourceDatabasePopulator(cleanUpAvailableInventoriesScenarioQuery);
        Mono.from(populator.populate(connectionFactory)).block();
    }
}
