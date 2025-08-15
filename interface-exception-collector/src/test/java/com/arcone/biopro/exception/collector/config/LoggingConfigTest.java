package com.arcone.biopro.exception.collector.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static com.arcone.biopro.exception.collector.config.LoggingConfig.LoggingContext;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for LoggingConfig to verify correlation ID and MDC management.
 */
class LoggingConfigTest {

    @BeforeEach
    @AfterEach
    void clearMDC() {
        MDC.clear();
    }

    @Test
    void shouldSetCorrelationId() {
        // Given
        String correlationId = "test-correlation-id";

        // When
        LoggingContext.setCorrelationId(correlationId);

        // Then
        assertThat(LoggingContext.getCorrelationId()).isEqualTo(correlationId);
        assertThat(MDC.get(LoggingConfig.CORRELATION_ID_MDC_KEY)).isEqualTo(correlationId);
    }

    @Test
    void shouldSetTransactionId() {
        // Given
        String transactionId = "test-transaction-id";

        // When
        LoggingContext.setTransactionId(transactionId);

        // Then
        assertThat(LoggingContext.getTransactionId()).isEqualTo(transactionId);
        assertThat(MDC.get(LoggingConfig.TRANSACTION_ID_MDC_KEY)).isEqualTo(transactionId);
    }

    @Test
    void shouldSetInterfaceType() {
        // Given
        String interfaceType = "ORDER";

        // When
        LoggingContext.setInterfaceType(interfaceType);

        // Then
        assertThat(MDC.get(LoggingConfig.INTERFACE_TYPE_MDC_KEY)).isEqualTo(interfaceType);
    }

    @Test
    void shouldSetUserId() {
        // Given
        String userId = "test-user";

        // When
        LoggingContext.setUserId(userId);

        // Then
        assertThat(MDC.get(LoggingConfig.USER_ID_MDC_KEY)).isEqualTo(userId);
    }

    @Test
    void shouldNotSetNullOrEmptyValues() {
        // When
        LoggingContext.setCorrelationId(null);
        LoggingContext.setCorrelationId("");
        LoggingContext.setCorrelationId("  ");

        // Then
        assertThat(LoggingContext.getCorrelationId()).isNull();
    }

    @Test
    void shouldClearAllContext() {
        // Given
        LoggingContext.setCorrelationId("correlation-id");
        LoggingContext.setTransactionId("transaction-id");
        LoggingContext.setInterfaceType("ORDER");
        LoggingContext.setUserId("user-id");

        // When
        LoggingContext.clear();

        // Then
        assertThat(LoggingContext.getCorrelationId()).isNull();
        assertThat(LoggingContext.getTransactionId()).isNull();
        assertThat(MDC.get(LoggingConfig.INTERFACE_TYPE_MDC_KEY)).isNull();
        assertThat(MDC.get(LoggingConfig.USER_ID_MDC_KEY)).isNull();
    }

    @Test
    void shouldClearSpecificKeys() {
        // Given
        LoggingContext.setCorrelationId("correlation-id");
        LoggingContext.setTransactionId("transaction-id");
        LoggingContext.setInterfaceType("ORDER");

        // When
        LoggingContext.clearKeys(LoggingConfig.CORRELATION_ID_MDC_KEY, LoggingConfig.INTERFACE_TYPE_MDC_KEY);

        // Then
        assertThat(LoggingContext.getCorrelationId()).isNull();
        assertThat(LoggingContext.getTransactionId()).isEqualTo("transaction-id");
        assertThat(MDC.get(LoggingConfig.INTERFACE_TYPE_MDC_KEY)).isNull();
    }

    @Test
    void shouldCreateAndSetCorrelationId() {
        // When
        String correlationId = LoggingContext.createAndSetCorrelationId();

        // Then
        assertThat(correlationId).isNotNull();
        assertThat(correlationId).isNotEmpty();
        assertThat(LoggingContext.getCorrelationId()).isEqualTo(correlationId);
    }

    @Test
    void shouldExecuteWithCorrelationId() {
        // Given
        String testCorrelationId = "test-correlation-id";
        String originalCorrelationId = "original-correlation-id";
        LoggingContext.setCorrelationId(originalCorrelationId);

        final String[] capturedCorrelationId = new String[1];

        // When
        LoggingContext.withCorrelationId(testCorrelationId, () -> {
            capturedCorrelationId[0] = LoggingContext.getCorrelationId();
        });

        // Then
        assertThat(capturedCorrelationId[0]).isEqualTo(testCorrelationId);
        assertThat(LoggingContext.getCorrelationId()).isEqualTo(originalCorrelationId);
    }

    @Test
    void shouldExecuteWithCorrelationIdWhenNoPreviousValue() {
        // Given
        String testCorrelationId = "test-correlation-id";
        final String[] capturedCorrelationId = new String[1];

        // When
        LoggingContext.withCorrelationId(testCorrelationId, () -> {
            capturedCorrelationId[0] = LoggingContext.getCorrelationId();
        });

        // Then
        assertThat(capturedCorrelationId[0]).isEqualTo(testCorrelationId);
        assertThat(LoggingContext.getCorrelationId()).isNull();
    }

    @Test
    void shouldExecuteWithContext() {
        // Given
        String correlationId = "correlation-id";
        String transactionId = "transaction-id";
        String interfaceType = "ORDER";

        final String[] capturedValues = new String[3];

        // When
        LoggingContext.withContext(correlationId, transactionId, interfaceType, () -> {
            capturedValues[0] = LoggingContext.getCorrelationId();
            capturedValues[1] = LoggingContext.getTransactionId();
            capturedValues[2] = MDC.get(LoggingConfig.INTERFACE_TYPE_MDC_KEY);
        });

        // Then
        assertThat(capturedValues[0]).isEqualTo(correlationId);
        assertThat(capturedValues[1]).isEqualTo(transactionId);
        assertThat(capturedValues[2]).isEqualTo(interfaceType);

        // Context should be cleared after execution
        assertThat(LoggingContext.getCorrelationId()).isNull();
        assertThat(LoggingContext.getTransactionId()).isNull();
        assertThat(MDC.get(LoggingConfig.INTERFACE_TYPE_MDC_KEY)).isNull();
    }

    @Test
    void shouldRestorePreviousContextAfterExecution() {
        // Given
        String originalCorrelationId = "original-correlation";
        String originalTransactionId = "original-transaction";
        String originalInterfaceType = "COLLECTION";

        LoggingContext.setCorrelationId(originalCorrelationId);
        LoggingContext.setTransactionId(originalTransactionId);
        LoggingContext.setInterfaceType(originalInterfaceType);

        String newCorrelationId = "new-correlation";
        String newTransactionId = "new-transaction";
        String newInterfaceType = "ORDER";

        // When
        LoggingContext.withContext(newCorrelationId, newTransactionId, newInterfaceType, () -> {
            // Context should be updated during execution
            assertThat(LoggingContext.getCorrelationId()).isEqualTo(newCorrelationId);
            assertThat(LoggingContext.getTransactionId()).isEqualTo(newTransactionId);
            assertThat(MDC.get(LoggingConfig.INTERFACE_TYPE_MDC_KEY)).isEqualTo(newInterfaceType);
        });

        // Then - original context should be restored
        assertThat(LoggingContext.getCorrelationId()).isEqualTo(originalCorrelationId);
        assertThat(LoggingContext.getTransactionId()).isEqualTo(originalTransactionId);
        assertThat(MDC.get(LoggingConfig.INTERFACE_TYPE_MDC_KEY)).isEqualTo(originalInterfaceType);
    }
}