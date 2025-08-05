package com.arcone.biopro.distribution.inventory.verification.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TestDataLifecycle {

    private final TestDataCleanUp testDataCleanUp;

    /**
     * Runs once, right after the Spring context is fully initialized
     * (i.e. before any Cucumber scenarios fire).
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("ApplicationReadyEvent – cleaning up all test data at startup");
        testDataCleanUp.cleanUpAll();
    }

    /**
     * Runs once, when the Spring context is shutting down
     * (i.e. after Cucumber has finished all scenarios).
     */
    @EventListener(ContextClosedEvent.class)
    public void onShutdown() {
        log.info("ContextClosedEvent – cleaning up all test data at shutdown");
        testDataCleanUp.cleanUpAll();
    }
}
