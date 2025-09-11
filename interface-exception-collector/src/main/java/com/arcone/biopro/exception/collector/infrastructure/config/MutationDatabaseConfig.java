package com.arcone.biopro.exception.collector.infrastructure.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Database connection pool configuration optimized for GraphQL mutation operations.
 * Provides enhanced connection pooling with mutation-specific optimizations.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "graphql.mutation.performance.database.optimization-enabled", havingValue = "true", matchIfMissing = true)
public class MutationDatabaseConfig {

    private final MutationPerformanceConfig performanceConfig;

    /**
     * Creates an optimized HikariCP data source for mutation operations
     */
    @Bean("mutationDataSource")
    @Primary
    public DataSource mutationDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Basic connection settings
        config.setJdbcUrl(getJdbcUrl());
        config.setUsername(getUsername());
        config.setPassword(getPassword());
        config.setDriverClassName("org.postgresql.Driver");

        // Connection pool optimization for mutations
        MutationPerformanceConfig.DatabaseConfig dbConfig = performanceConfig.getDatabase();
        
        config.setMaximumPoolSize(dbConfig.getMaxConnections());
        config.setMinimumIdle(dbConfig.getMinIdleConnections());
        config.setConnectionTimeout(dbConfig.getConnectionTimeoutMs());
        config.setMaxLifetime(dbConfig.getMaxLifetimeMs());
        config.setLeakDetectionThreshold(dbConfig.getLeakDetectionThresholdMs());

        // Performance optimizations
        config.setIdleTimeout(600000); // 10 minutes
        config.setValidationTimeout(5000); // 5 seconds
        config.setInitializationFailTimeout(30000); // 30 seconds

        // Connection test query optimized for PostgreSQL
        config.setConnectionTestQuery("SELECT 1");

        // Pool name for monitoring
        config.setPoolName("MutationPool");

        // Performance-related properties
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        // PostgreSQL-specific optimizations
        config.addDataSourceProperty("tcpKeepAlive", "true");
        config.addDataSourceProperty("socketTimeout", "30");
        config.addDataSourceProperty("loginTimeout", "10");
        config.addDataSourceProperty("connectTimeout", "10");

        HikariDataSource dataSource = new HikariDataSource(config);

        log.info("Initialized optimized mutation data source with maxConnections={}, minIdle={}, connectionTimeout={}ms",
                dbConfig.getMaxConnections(),
                dbConfig.getMinIdleConnections(),
                dbConfig.getConnectionTimeoutMs());

        return dataSource;
    }

    /**
     * Gets JDBC URL from environment variables or default
     */
    private String getJdbcUrl() {
        String host = System.getenv().getOrDefault("DB_HOST", "postgres");
        String port = System.getenv().getOrDefault("DB_PORT", "5432");
        String database = System.getenv().getOrDefault("DB_NAME", "exception_collector_db");
        
        return String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
    }

    /**
     * Gets database username from environment variables or default
     */
    private String getUsername() {
        return System.getenv().getOrDefault("DB_USERNAME", "exception_user");
    }

    /**
     * Gets database password from environment variables or default
     */
    private String getPassword() {
        return System.getenv().getOrDefault("DB_PASSWORD", "exception_pass");
    }
}