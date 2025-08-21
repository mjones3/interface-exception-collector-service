package com.arcone.biopro.exception.collector.api.graphql.config;

import com.arcone.biopro.exception.collector.api.graphql.util.DataLoaderUtil;
import graphql.schema.DataFetchingEnvironment;
import org.dataloader.DataLoaderRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test for DataLoader configuration and functionality.
 * Verifies that DataLoaders are properly configured and accessible in GraphQL
 * context.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "graphql.dataloader.exception.batch-size=100",
        "graphql.dataloader.payload.batch-size=20",
        "graphql.dataloader.retry-history.batch-size=75",
        "graphql.dataloader.status-change.batch-size=75",
        "graphql.dataloader.cache-ttl-seconds=180",
        "graphql.dataloader.batch-delay-ms=5"
})
class DataLoaderIntegrationTest {

    @Test
    void shouldHaveAllDataLoaderNamesConfigured() {
        // Verify that all expected DataLoader names are defined
        assertThat(DataLoaderConfig.EXCEPTION_LOADER).isNotNull();
        assertThat(DataLoaderConfig.PAYLOAD_LOADER).isNotNull();
        assertThat(DataLoaderConfig.RETRY_HISTORY_LOADER).isNotNull();
        assertThat(DataLoaderConfig.STATUS_CHANGE_LOADER).isNotNull();
    }

    @Test
    void shouldCreateDataLoaderRegistryWithCorrectKeys() {
        // Create a mock DataLoaderRegistry to simulate GraphQL context
        DataLoaderRegistry registry = new DataLoaderRegistry();

        // Verify that the registry can be populated with the expected keys
        registry.register(DataLoaderConfig.EXCEPTION_LOADER, null);
        registry.register(DataLoaderConfig.PAYLOAD_LOADER, null);
        registry.register(DataLoaderConfig.RETRY_HISTORY_LOADER, null);
        registry.register(DataLoaderConfig.STATUS_CHANGE_LOADER, null);

        assertThat(registry.getKeys()).containsExactlyInAnyOrder(
                DataLoaderConfig.EXCEPTION_LOADER,
                DataLoaderConfig.PAYLOAD_LOADER,
                DataLoaderConfig.RETRY_HISTORY_LOADER,
                DataLoaderConfig.STATUS_CHANGE_LOADER);
    }

    @Test
    void shouldHandleDataLoaderUtilWithMissingLoaders() {
        // Create a mock DataFetchingEnvironment with empty DataLoaderRegistry
        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);
        DataLoaderRegistry emptyRegistry = new DataLoaderRegistry();
        when(environment.getDataLoader(DataLoaderConfig.EXCEPTION_LOADER)).thenReturn(null);
        when(environment.getDataLoader(DataLoaderConfig.PAYLOAD_LOADER)).thenReturn(null);
        when(environment.getDataLoader(DataLoaderConfig.RETRY_HISTORY_LOADER)).thenReturn(null);
        when(environment.getDataLoader(DataLoaderConfig.STATUS_CHANGE_LOADER)).thenReturn(null);

        // Verify that DataLoaderUtil handles missing loaders gracefully
        assertThat(DataLoaderUtil.getExceptionLoader(environment)).isNull();
        assertThat(DataLoaderUtil.getPayloadLoader(environment)).isNull();
        assertThat(DataLoaderUtil.getRetryHistoryLoader(environment)).isNull();
        assertThat(DataLoaderUtil.getStatusChangeLoader(environment)).isNull();

        // Verify that areDataLoadersAvailable returns false when loaders are missing
        assertThat(DataLoaderUtil.areDataLoadersAvailable(environment)).isFalse();
    }

    @Test
    void shouldValidateDataLoaderNamingConvention() {
        // Verify that all DataLoader names follow the expected naming convention
        assertThat(DataLoaderConfig.EXCEPTION_LOADER).matches("^[a-z][a-zA-Z]*Loader$");
        assertThat(DataLoaderConfig.PAYLOAD_LOADER).matches("^[a-z][a-zA-Z]*Loader$");
        assertThat(DataLoaderConfig.RETRY_HISTORY_LOADER).matches("^[a-z][a-zA-Z]*Loader$");
        assertThat(DataLoaderConfig.STATUS_CHANGE_LOADER).matches("^[a-z][a-zA-Z]*Loader$");
    }

    @Test
    void shouldHaveUniqueDataLoaderIdentifiers() {
        // Collect all DataLoader names
        String[] loaderNames = {
                DataLoaderConfig.EXCEPTION_LOADER,
                DataLoaderConfig.PAYLOAD_LOADER,
                DataLoaderConfig.RETRY_HISTORY_LOADER,
                DataLoaderConfig.STATUS_CHANGE_LOADER
        };

        // Verify uniqueness
        assertThat(loaderNames).doesNotHaveDuplicates();

        // Verify expected count
        assertThat(loaderNames).hasSize(4);

        // Verify none are null or empty
        for (String loaderName : loaderNames) {
            assertThat(loaderName).isNotNull().isNotEmpty();
        }
    }

    @Test
    void shouldHandleNullTransactionIdInDataLoaderUtil() {
        // Create a mock DataFetchingEnvironment
        DataFetchingEnvironment environment = mock(DataFetchingEnvironment.class);

        // Test that DataLoaderUtil methods handle null transaction IDs gracefully
        // These should not throw exceptions but return appropriate default values
        assertThat(DataLoaderUtil.loadException(environment, null)).isNotNull();
        assertThat(DataLoaderUtil.loadException(environment, "")).isNotNull();
        assertThat(DataLoaderUtil.loadPayload(environment, null)).isNotNull();
        assertThat(DataLoaderUtil.loadPayload(environment, "")).isNotNull();
        assertThat(DataLoaderUtil.loadRetryHistory(environment, null)).isNotNull();
        assertThat(DataLoaderUtil.loadRetryHistory(environment, "")).isNotNull();
        assertThat(DataLoaderUtil.loadStatusHistory(environment, null)).isNotNull();
        assertThat(DataLoaderUtil.loadStatusHistory(environment, "")).isNotNull();
    }
}