package com.arcone.biopro.exception.collector.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManagerFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration for mutation query timeouts and performance optimization.
 * Provides specific timeout settings for GraphQL mutation operations
 * to prevent long-running queries and ensure responsive mutation handling.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.arcone.biopro.exception.collector.infrastructure.repository")
@ConfigurationProperties(prefix = "app.mutation.query")
@Data
@Slf4j
public class MutationQueryTimeoutConfig {

    /**
     * Default timeout for mutation validation queries (in seconds)
     */
    private int validationTimeoutSeconds = 10;

    /**
     * Default timeout for mutation execution queries (in seconds)
     */
    private int executionTimeoutSeconds = 30;

    /**
     * Default timeout for batch mutation operations (in seconds)
     */
    private int batchTimeoutSeconds = 60;

    /**
     * Default timeout for retry limit checking queries (in seconds)
     */
    private int retryLimitCheckTimeoutSeconds = 5;

    /**
     * Default timeout for status validation queries (in seconds)
     */
    private int statusValidationTimeoutSeconds = 3;

    /**
     * Default timeout for existence check queries (in seconds)
     */
    private int existenceCheckTimeoutSeconds = 3;

    /**
     * Default timeout for optimistic locking queries (in seconds)
     */
    private int optimisticLockTimeoutSeconds = 10;

    /**
     * Maximum number of concurrent mutation operations
     */
    private int maxConcurrentMutations = 50;

    /**
     * Enable query timeout warnings
     */
    private boolean enableTimeoutWarnings = true;

    /**
     * Enable query performance monitoring
     */
    private boolean enablePerformanceMonitoring = true;

    /**
     * Query fetch size for mutation operations
     */
    private int mutationFetchSize = 100;

    /**
     * Enable query plan caching for mutations
     */
    private boolean enableQueryPlanCaching = true;

    /**
     * Transaction timeout for mutation operations (in seconds)
     */
    private int transactionTimeoutSeconds = 45;

    /**
     * Creates a transaction template specifically configured for mutation operations.
     * This template provides consistent transaction management with appropriate
     * timeouts for GraphQL mutations.
     *
     * @param transactionManager the JPA transaction manager
     * @return configured transaction template for mutations
     */
    @Bean("mutationTransactionTemplate")
    public TransactionTemplate mutationTransactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setTimeout(transactionTimeoutSeconds);
        template.setReadOnly(false);
        template.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
        
        log.info("Configured mutation transaction template with timeout: {} seconds", transactionTimeoutSeconds);
        return template;
    }

    /**
     * Creates a read-only transaction template for mutation validation queries.
     * This template is optimized for fast validation operations that don't
     * require write access to the database.
     *
     * @param transactionManager the JPA transaction manager
     * @return configured read-only transaction template for validation
     */
    @Bean("mutationValidationTransactionTemplate")
    public TransactionTemplate mutationValidationTransactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setTimeout(validationTimeoutSeconds);
        template.setReadOnly(true);
        template.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
        
        log.info("Configured mutation validation transaction template with timeout: {} seconds", validationTimeoutSeconds);
        return template;
    }

    /**
     * Creates a transaction template for batch mutation operations.
     * This template provides extended timeouts for operations that process
     * multiple mutations in a single transaction.
     *
     * @param transactionManager the JPA transaction manager
     * @return configured transaction template for batch operations
     */
    @Bean("mutationBatchTransactionTemplate")
    public TransactionTemplate mutationBatchTransactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setTimeout(batchTimeoutSeconds);
        template.setReadOnly(false);
        template.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
        
        log.info("Configured mutation batch transaction template with timeout: {} seconds", batchTimeoutSeconds);
        return template;
    }

    /**
     * Configures the JPA transaction manager with mutation-specific settings.
     * This ensures that all mutation operations use consistent transaction
     * management with appropriate timeouts and isolation levels.
     *
     * @param entityManagerFactory the JPA entity manager factory
     * @return configured JPA transaction manager
     */
    @Bean
    public PlatformTransactionManager mutationTransactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        transactionManager.setDefaultTimeout(transactionTimeoutSeconds);
        transactionManager.setRollbackOnCommitFailure(true);
        transactionManager.setValidateExistingTransaction(true);
        
        log.info("Configured JPA transaction manager for mutations with default timeout: {} seconds", 
                transactionTimeoutSeconds);
        return transactionManager;
    }

    /**
     * Gets the timeout value for a specific mutation operation type.
     * This allows different mutation types to have different timeout values
     * based on their expected execution time.
     *
     * @param operationType the type of mutation operation
     * @return timeout value in seconds
     */
    public int getTimeoutForOperation(MutationOperationType operationType) {
        return switch (operationType) {
            case VALIDATION -> validationTimeoutSeconds;
            case EXECUTION -> executionTimeoutSeconds;
            case BATCH -> batchTimeoutSeconds;
            case RETRY_LIMIT_CHECK -> retryLimitCheckTimeoutSeconds;
            case STATUS_VALIDATION -> statusValidationTimeoutSeconds;
            case EXISTENCE_CHECK -> existenceCheckTimeoutSeconds;
            case OPTIMISTIC_LOCK -> optimisticLockTimeoutSeconds;
        };
    }

    /**
     * Enumeration of mutation operation types for timeout configuration.
     */
    public enum MutationOperationType {
        VALIDATION,
        EXECUTION,
        BATCH,
        RETRY_LIMIT_CHECK,
        STATUS_VALIDATION,
        EXISTENCE_CHECK,
        OPTIMISTIC_LOCK
    }

    /**
     * Configuration properties for mutation query performance.
     */
    @Data
    public static class MutationQueryPerformanceProperties {
        private boolean enableQueryHints = true;
        private boolean enableResultCaching = true;
        private boolean enableBatchFetching = true;
        private int queryPlanCacheSize = 1000;
        private int resultCacheSize = 5000;
        private int resultCacheTtlSeconds = 300;
    }

    /**
     * Gets performance properties for mutation queries.
     *
     * @return mutation query performance properties
     */
    @Bean
    public MutationQueryPerformanceProperties mutationQueryPerformanceProperties() {
        MutationQueryPerformanceProperties properties = new MutationQueryPerformanceProperties();
        properties.setEnableQueryHints(enableQueryPlanCaching);
        properties.setEnableResultCaching(enablePerformanceMonitoring);
        properties.setQueryPlanCacheSize(1000);
        properties.setResultCacheSize(5000);
        properties.setResultCacheTtlSeconds(300);
        
        log.info("Configured mutation query performance properties: {}", properties);
        return properties;
    }
}