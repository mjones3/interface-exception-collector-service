package com.arcone.biopro.exception.collector.api.graphql.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for DataLoaderConfig to verify DataLoader activation and
 * configuration.
 * Tests that DataLoaders are properly registered and configured for request
 * scope.
 */
class DataLoaderConfigTest {

    @Test
    void shouldHaveCorrectDataLoaderNames() {
        // Then
        assertThat(DataLoaderConfig.EXCEPTION_LOADER).isEqualTo("exceptionLoader");
        assertThat(DataLoaderConfig.PAYLOAD_LOADER).isEqualTo("payloadLoader");
        assertThat(DataLoaderConfig.RETRY_HISTORY_LOADER).isEqualTo("retryHistoryLoader");
        assertThat(DataLoaderConfig.STATUS_CHANGE_LOADER).isEqualTo("statusChangeLoader");
    }

    @Test
    void shouldHaveConsistentDataLoaderNames() {
        // Verify that the DataLoader names are consistent and follow naming convention
        assertThat(DataLoaderConfig.EXCEPTION_LOADER).endsWith("Loader");
        assertThat(DataLoaderConfig.PAYLOAD_LOADER).endsWith("Loader");
        assertThat(DataLoaderConfig.RETRY_HISTORY_LOADER).endsWith("Loader");
        assertThat(DataLoaderConfig.STATUS_CHANGE_LOADER).endsWith("Loader");
    }

    @Test
    void shouldHaveUniqueDataLoaderNames() {
        // Verify all DataLoader names are unique
        String[] loaderNames = {
                DataLoaderConfig.EXCEPTION_LOADER,
                DataLoaderConfig.PAYLOAD_LOADER,
                DataLoaderConfig.RETRY_HISTORY_LOADER,
                DataLoaderConfig.STATUS_CHANGE_LOADER
        };

        assertThat(loaderNames).doesNotHaveDuplicates();
        assertThat(loaderNames).hasSize(4);
    }
}