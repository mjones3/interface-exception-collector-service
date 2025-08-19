package com.arcone.biopro.exception.collector.api.graphql.config;

import com.arcone.biopro.exception.collector.api.dto.PayloadResponse;
import com.arcone.biopro.exception.collector.api.graphql.dataloader.ExceptionDataLoader;
import com.arcone.biopro.exception.collector.api.graphql.dataloader.PayloadDataLoader;
import com.arcone.biopro.exception.collector.api.graphql.dataloader.RetryHistoryDataLoader;
import com.arcone.biopro.exception.collector.api.graphql.dataloader.StatusChangeDataLoader;
import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.entity.RetryAttempt;
import com.arcone.biopro.exception.collector.domain.entity.StatusChange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;

/**
 * Configuration for GraphQL DataLoader instances.
 * Sets up DataLoader registry with caching and batching configuration
 * to optimize GraphQL query performance and prevent N+1 problems.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoaderConfig {

        private final ExceptionDataLoader exceptionDataLoader;
        private final PayloadDataLoader payloadDataLoader;
        private final RetryHistoryDataLoader retryHistoryDataLoader;
        private final StatusChangeDataLoader statusChangeDataLoader;

        // Performance tuning configuration
        @Value("${graphql.dataloader.exception.batch-size:200}")
        private int exceptionBatchSize;

        @Value("${graphql.dataloader.payload.batch-size:25}")
        private int payloadBatchSize;

        @Value("${graphql.dataloader.retry-history.batch-size:150}")
        private int retryHistoryBatchSize;

        @Value("${graphql.dataloader.status-change.batch-size:150}")
        private int statusChangeBatchSize;

        @Value("${graphql.dataloader.cache-ttl-seconds:300}")
        private int cacheTtlSeconds;

        @Value("${graphql.dataloader.batch-delay-ms:10}")
        private int batchDelayMs;

        /**
         * DataLoader names used throughout the application
         */
        public static final String EXCEPTION_LOADER = "exceptionLoader";
        public static final String PAYLOAD_LOADER = "payloadLoader";
        public static final String RETRY_HISTORY_LOADER = "retryHistoryLoader";
        public static final String STATUS_CHANGE_LOADER = "statusChangeLoader";

        /**
         * Creates and configures the DataLoaderRegistry with all DataLoader instances.
         * Each DataLoader is configured with optimized caching and batching settings.
         *
         * @return configured DataLoaderRegistry
         */
        @Bean
        @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
        public DataLoaderRegistry dataLoaderRegistry() {
                log.debug("Creating new request-scoped DataLoader registry with optimized settings");

                DataLoaderRegistry registry = new DataLoaderRegistry();

                // Configure dedicated scheduled executor for DataLoader operations
                ScheduledExecutorService dataLoaderExecutor = Executors.newScheduledThreadPool(4, r -> {
                        Thread t = new Thread(r, "dataloader-executor");
                        t.setDaemon(true);
                        return t;
                });

                // Exception DataLoader - optimized for high-volume exception queries
                DataLoaderOptions exceptionOptions = DataLoaderOptions.newOptions()
                                .setCachingEnabled(true)
                                .setBatchingEnabled(true)
                                .setMaxBatchSize(exceptionBatchSize)
                                .setBatchLoaderScheduledExecutor(dataLoaderExecutor)
                                .setBatchLoadDelay(Duration.ofMillis(batchDelayMs))
                                .setCacheKeyFunction(String::valueOf);

                DataLoader<String, InterfaceException> exceptionLoader = DataLoader.newMappedDataLoader(
                                exceptionDataLoader, exceptionOptions);

                registry.register(EXCEPTION_LOADER, exceptionLoader);
                log.debug("Registered exception DataLoader with batch size: {}, delay: {}ms",
                                exceptionBatchSize, batchDelayMs);

                // Payload DataLoader - smaller batch size for external service calls
                DataLoaderOptions payloadOptions = DataLoaderOptions.newOptions()
                                .setCachingEnabled(true)
                                .setBatchingEnabled(true)
                                .setMaxBatchSize(payloadBatchSize)
                                .setBatchLoaderScheduledExecutor(dataLoaderExecutor)
                                .setBatchLoadDelay(Duration.ofMillis(batchDelayMs * 2)) // Longer delay for external
                                                                                        // calls
                                .setCacheKeyFunction(String::valueOf);

                DataLoader<String, PayloadResponse> payloadLoader = DataLoader.newMappedDataLoader(
                                payloadDataLoader, payloadOptions);

                registry.register(PAYLOAD_LOADER, payloadLoader);
                log.debug("Registered payload DataLoader with batch size: {}, delay: {}ms",
                                payloadBatchSize, batchDelayMs * 2);

                // Retry History DataLoader - optimized for audit trail queries
                DataLoaderOptions retryHistoryOptions = DataLoaderOptions.newOptions()
                                .setCachingEnabled(true)
                                .setBatchingEnabled(true)
                                .setMaxBatchSize(retryHistoryBatchSize)
                                .setBatchLoaderScheduledExecutor(dataLoaderExecutor)
                                .setBatchLoadDelay(Duration.ofMillis(batchDelayMs))
                                .setCacheKeyFunction(String::valueOf);

                DataLoader<String, List<RetryAttempt>> retryHistoryLoader = DataLoader.newMappedDataLoader(
                                retryHistoryDataLoader, retryHistoryOptions);

                registry.register(RETRY_HISTORY_LOADER, retryHistoryLoader);
                log.debug("Registered retry history DataLoader with batch size: {}", retryHistoryBatchSize);

                // Status Change DataLoader - optimized for status history queries
                DataLoaderOptions statusChangeOptions = DataLoaderOptions.newOptions()
                                .setCachingEnabled(true)
                                .setBatchingEnabled(true)
                                .setMaxBatchSize(statusChangeBatchSize)
                                .setBatchLoaderScheduledExecutor(dataLoaderExecutor)
                                .setBatchLoadDelay(Duration.ofMillis(batchDelayMs))
                                .setCacheKeyFunction(String::valueOf);

                DataLoader<String, List<StatusChange>> statusChangeLoader = DataLoader.newMappedDataLoader(
                                statusChangeDataLoader, statusChangeOptions);

                registry.register(STATUS_CHANGE_LOADER, statusChangeLoader);
                log.debug("Registered status change DataLoader with batch size: {}", statusChangeBatchSize);

                log.debug("DataLoader registry configured with {} loaders, cache TTL: {}s",
                                registry.getKeys().size(), cacheTtlSeconds);

                return registry;
        }

        /**
         * Creates a new DataLoaderRegistry instance for each GraphQL request.
         * This ensures proper isolation between requests and prevents cache leakage.
         * This method is called by the GraphQL configuration to get a fresh registry
         * per request.
         *
         * @return new DataLoaderRegistry instance
         */
        public DataLoaderRegistry requestScopedDataLoaderRegistry() {
                return dataLoaderRegistry();
        }
}