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
    private final ConnectionFactory connectionFactory;

    public TestDataCleanUp(
        @Value("classpath:/db/cleanUpAll.sql") Resource cleanUpAllQuery,
        ConnectionFactory connectionFactory
    ) {
        this.cleanUpAllQuery = cleanUpAllQuery;
        this.connectionFactory = connectionFactory;
    }

    public void cleanUpAll() {
        var populator = new ResourceDatabasePopulator(cleanUpAllQuery);
        Mono.from(populator.populate(connectionFactory)).block();
    }
}
