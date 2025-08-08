package com.arcone.biopro.exception.collector.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify Flyway database migrations work correctly.
 * This test validates that all migration scripts execute successfully
 * and create the expected database schema.
 */
@Testcontainers
class DatabaseMigrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("exception_collector_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @Test
    void shouldExecuteAllMigrationsSuccessfully() throws Exception {
        // Configure and run Flyway migrations
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load();

        // Execute migrations
        int migrationsExecuted = flyway.migrate().migrationsExecuted;

        // Verify that all 5 migrations were executed
        assertEquals(5, migrationsExecuted, "Expected 5 migrations to be executed");

        // Verify database schema was created correctly
        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
                Statement statement = connection.createStatement()) {

            // Verify interface_exceptions table exists with correct structure
            verifyInterfaceExceptionsTable(statement);

            // Verify retry_attempts table exists with correct structure
            verifyRetryAttemptsTable(statement);

            // Verify indexes were created
            verifyIndexesExist(statement);

            // Verify triggers were created
            verifyTriggersExist(statement);
        }
    }

    private void verifyInterfaceExceptionsTable(Statement statement) throws Exception {
        // Check table exists
        ResultSet tables = statement.executeQuery(
                "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = 'public' AND table_name = 'interface_exceptions'");
        assertTrue(tables.next(), "interface_exceptions table should exist");

        // Check key columns exist
        ResultSet columns = statement.executeQuery(
                "SELECT column_name, data_type, is_nullable " +
                        "FROM information_schema.columns " +
                        "WHERE table_name = 'interface_exceptions' " +
                        "ORDER BY column_name");

        boolean hasTransactionId = false;
        boolean hasInterfaceType = false;
        boolean hasExceptionReason = false;
        boolean hasStatus = false;
        boolean hasSeverity = false;

        while (columns.next()) {
            String columnName = columns.getString("column_name");
            switch (columnName) {
                case "transaction_id" -> hasTransactionId = true;
                case "interface_type" -> hasInterfaceType = true;
                case "exception_reason" -> hasExceptionReason = true;
                case "status" -> hasStatus = true;
                case "severity" -> hasSeverity = true;
            }
        }

        assertTrue(hasTransactionId, "transaction_id column should exist");
        assertTrue(hasInterfaceType, "interface_type column should exist");
        assertTrue(hasExceptionReason, "exception_reason column should exist");
        assertTrue(hasStatus, "status column should exist");
        assertTrue(hasSeverity, "severity column should exist");
    }

    private void verifyRetryAttemptsTable(Statement statement) throws Exception {
        // Check table exists
        ResultSet tables = statement.executeQuery(
                "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = 'public' AND table_name = 'retry_attempts'");
        assertTrue(tables.next(), "retry_attempts table should exist");

        // Check foreign key constraint exists
        ResultSet constraints = statement.executeQuery(
                "SELECT constraint_name FROM information_schema.table_constraints " +
                        "WHERE table_name = 'retry_attempts' AND constraint_type = 'FOREIGN KEY'");
        assertTrue(constraints.next(), "Foreign key constraint should exist on retry_attempts table");
    }

    private void verifyIndexesExist(Statement statement) throws Exception {
        // Check that key indexes were created
        ResultSet indexes = statement.executeQuery(
                "SELECT indexname FROM pg_indexes " +
                        "WHERE tablename IN ('interface_exceptions', 'retry_attempts') " +
                        "AND indexname LIKE 'idx_%'");

        boolean hasTransactionIdIndex = false;
        boolean hasInterfaceTypeIndex = false;
        boolean hasFullTextIndex = false;

        while (indexes.next()) {
            String indexName = indexes.getString("indexname");
            if (indexName.contains("transaction_id")) {
                hasTransactionIdIndex = true;
            }
            if (indexName.contains("interface_type")) {
                hasInterfaceTypeIndex = true;
            }
            if (indexName.contains("fts")) {
                hasFullTextIndex = true;
            }
        }

        assertTrue(hasTransactionIdIndex, "Transaction ID index should exist");
        assertTrue(hasInterfaceTypeIndex, "Interface type index should exist");
        assertTrue(hasFullTextIndex, "Full-text search index should exist");
    }

    private void verifyTriggersExist(Statement statement) throws Exception {
        // Check that the update trigger was created
        ResultSet triggers = statement.executeQuery(
                "SELECT trigger_name FROM information_schema.triggers " +
                        "WHERE event_object_table = 'interface_exceptions'");

        boolean hasUpdateTrigger = false;
        while (triggers.next()) {
            String triggerName = triggers.getString("trigger_name");
            if (triggerName.contains("updated_at")) {
                hasUpdateTrigger = true;
            }
        }

        assertTrue(hasUpdateTrigger, "Updated_at trigger should exist");
    }
}