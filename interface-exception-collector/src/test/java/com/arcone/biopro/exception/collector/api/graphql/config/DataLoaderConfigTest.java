package com.arcone.biopro.exception.collector.api.graphql.config;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.api.graphql.dataloader.ExceptionDataLoader;
import com.arcone.biopro.exception.collector.api.graphql.dataloader.PayloadDataLoader;
import com.arcone.biopro.exception.collector.api.graphql.dataloader.RetryHistoryDataLoader;
import com.arcone.biopro.exception.collector.api.graphql.dataloader.StatusChangeDataLoader;
import com.arcone.biopro.exception.collector.application.service.PayloadRetrievalService;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.entity.StatusChange;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.RetryAttemptRepository;
import com.arcone.biopro.exception.collector.infrastructure.repository.StatusChangeRepository;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for DataLoaderConfig to verify proper configuration and
 * registration
 * of DataLoader instances.
 */
@ExtendWith(MockitoExtension.class)
class DataLoaderConfigTest {

    @Mock
    private InterfaceExceptionRepository exceptionRepository;

    @Mock
    private PayloadRetrievalService payloadRetrievalService;

    @Mock
    private RetryAttemptRepository retryAttemptRepository;

    @Mock
    private StatusChangeRepository statusChangeRepository;

    private DataLoaderConfig dataLoaderConfig;

    @BeforeEach
    void setUp() {
        // Create DataLoader instances
        ExceptionDataLoader exceptionDataLoader = new ExceptionDataLoader(exceptionRepository);
        PayloadDataLoader payloadDataLoader = new PayloadDataLoader(payloadRetrievalService, exceptionRepository);
        RetryHistoryDataLoader retryHistoryDataLoader = new RetryHistoryDataLoader(retryAttemptRepository,
                exceptionRepository);
        StatusChangeDataLoader statusChangeDataLoader = new StatusChangeDataLoader(statusChangeRepository);

        // Create DataLoaderConfig
        dataLoaderConfig = new DataLoaderConfig(
                exceptionDataLoader,
                payloadDataLoader,
                retryHistoryDataLoader,
                statusChangeDataLoader);
    }

    @Test
    void testDataLoaderRegistryConfiguration() {
        // Act
        DataLoaderRegistry registry = dataLoaderConfig.dataLoaderRegistry();

        // Assert
        assertThat(registry).isNotNull();
        assertThat(registry.getKeys()).hasSize(4);

        // Verify all expected DataLoaders are registered
        assertThat(registry.getKeys()).containsExactlyInAnyOrder(
                DataLoaderConfig.EXCEPTION_LOADER,
                DataLoaderConfig.PAYLOAD_LOADER,
                DataLoaderConfig.RETRY_HISTORY_LOADER,
                DataLoaderConfig.STATUS_CHANGE_LOADER);
    }

    @Test
    void testExceptionDataLoaderRegistration() {
        // Act
        DataLoaderRegistry registry = dataLoaderConfig.dataLoaderRegistry();
        DataLoader<String, InterfaceException> loader = registry.getDataLoader(DataLoaderConfig.EXCEPTION_LOADER);

        // Assert
        assertThat(loader).isNotNull();
        assertThat(loader).isInstanceOf(DataLoader.class);
    }

    @Test
    void testPayloadDataLoaderRegistration() {
        // Act
        DataLoaderRegistry registry = dataLoaderConfig.dataLoaderRegistry();
        DataLoader<String, PayloadResponse> loader = registry.getDataLoader(DataLoaderConfig.PAYLOAD_LOADER);

        // Assert
        assertThat(loader).isNotNull();
        assertThat(loader).isInstanceOf(DataLoader.class);
    }

    @Test
    void testRetryHistoryDataLoaderRegistration() {
        // Act
        DataLoaderRegistry registry = dataLoaderConfig.dataLoaderRegistry();
        DataLoader<String, List<RetryAttempt>> loader = registry.getDataLoader(DataLoaderConfig.RETRY_HISTORY_LOADER);

        // Assert
        assertThat(loader).isNotNull();
        assertThat(loader).isInstanceOf(DataLoader.class);
    }

    @Test
    void testStatusChangeDataLoaderRegistration() {
        // Act
        DataLoaderRegistry registry = dataLoaderConfig.dataLoaderRegistry();
        DataLoader<String, List<StatusChange>> loader = registry.getDataLoader(DataLoaderConfig.STATUS_CHANGE_LOADER);

        // Assert
        assertThat(loader).isNotNull();
        assertThat(loader).isInstanceOf(DataLoader.class);
    }

    @Test
    void testRequestScopedDataLoaderRegistry() {
        // Act - Create multiple registry instances
        DataLoaderRegistry registry1 = dataLoaderConfig.requestScopedDataLoaderRegistry();
        DataLoaderRegistry registry2 = dataLoaderConfig.requestScopedDataLoaderRegistry();

        // Assert - Each request should get a fresh registry
        assertThat(registry1).isNotSameAs(registry2);

        // But they should have the same structure
        assertThat(registry1.getKeys()).isEqualTo(registry2.getKeys());
        assertThat(registry1.getKeys()).hasSize(4);
        assertThat(registry2.getKeys()).hasSize(4);
    }

    @Test
    void testDataLoaderConstants() {
        // Assert that all constants are properly defined
        assertThat(DataLoaderConfig.EXCEPTION_LOADER).isEqualTo("exceptionLoader");
        assertThat(DataLoaderConfig.PAYLOAD_LOADER).isEqualTo("payloadLoader");
        assertThat(DataLoaderConfig.RETRY_HISTORY_LOADER).isEqualTo("retryHistoryLoader");
        assertThat(DataLoaderConfig.STATUS_CHANGE_LOADER).isEqualTo("statusChangeLoader");
    }

    @Test
    void testDataLoaderFunctionality() {
        // Act
        DataLoaderRegistry registry = dataLoaderConfig.dataLoaderRegistry();

        // Test that all DataLoaders are properly configured and functional
        DataLoader<String, InterfaceException> exceptionLoader = registry
                .getDataLoader(DataLoaderConfig.EXCEPTION_LOADER);
        DataLoader<String, PayloadResponse> payloadLoader = registry.getDataLoader(DataLoaderConfig.PAYLOAD_LOADER);
        DataLoader<String, List<RetryAttempt>> retryLoader = registry
                .getDataLoader(DataLoaderConfig.RETRY_HISTORY_LOADER);
        DataLoader<String, List<StatusChange>> statusLoader = registry
                .getDataLoader(DataLoaderConfig.STATUS_CHANGE_LOADER);

        // Assert all loaders are properly instantiated
        assertThat(exceptionLoader).isNotNull();
        assertThat(payloadLoader).isNotNull();
        assertThat(retryLoader).isNotNull();
        assertThat(statusLoader).isNotNull();

        // Verify they can accept load requests (this tests the basic functionality)
        assertThat(exceptionLoader.load("test-id")).isNotNull();
        assertThat(payloadLoader.load("test-id")).isNotNull();
        assertThat(retryLoader.load("test-id")).isNotNull();
        assertThat(statusLoader.load("test-id")).isNotNull();
    }
}