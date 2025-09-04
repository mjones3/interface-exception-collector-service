package com.arcone.biopro.exception.collector.infrastructure.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RSocketLoggingInterceptorTest {

    private RSocketLoggingInterceptor loggingInterceptor;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        loggingInterceptor = new RSocketLoggingInterceptor();
        
        // Set up log capture
        logger = (Logger) LoggerFactory.getLogger(RSocketLoggingInterceptor.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        logger.detachAppender(listAppender);
    }

    @Test
    void shouldLogRSocketCallStart() {
        // Given
        String externalId = "TEST-ORDER-1";
        String operation = "GET_ORDER_DATA";
        String transactionId = "txn-123";

        // When
        String correlationId = loggingInterceptor.logRSocketCallStart(externalId, operation, transactionId);

        // Then
        assertThat(correlationId).isNotNull();
        assertThat(correlationId).startsWith("rsocket-");
        
        // Verify MDC context is set
        assertThat(MDC.get("rsocket.correlation_id")).isEqualTo(correlationId);
        assertThat(MDC.get("rsocket.external_id")).isEqualTo(externalId);
        assertThat(MDC.get("rsocket.operation")).isEqualTo(operation);
        assertThat(MDC.get("rsocket.service")).isEqualTo("mock-rsocket-server");
        assertThat(MDC.get("rsocket.transaction_id")).isEqualTo(transactionId);

        // Verify log message
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        assertThat(logsList.get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(logsList.get(0).getFormattedMessage()).contains("Starting RSocket call to mock server");
    }

    @Test
    void shouldLogRSocketCallSuccess() {
        // Given
        String correlationId = "rsocket-12345678";
        String externalId = "TEST-ORDER-1";
        String operation = "GET_ORDER_DATA";
        Duration duration = Duration.ofMillis(150);
        boolean dataRetrieved = true;

        // When
        loggingInterceptor.logRSocketCallSuccess(correlationId, externalId, operation, duration, dataRetrieved);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        assertThat(logsList.get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(logsList.get(0).getFormattedMessage()).contains("RSocket call completed successfully");
        assertThat(logsList.get(0).getFormattedMessage()).contains("dataRetrieved=true");
        assertThat(logsList.get(0).getFormattedMessage()).contains("duration=150ms");
    }

    @Test
    void shouldLogRSocketCallFailure() {
        // Given
        String correlationId = "rsocket-12345678";
        String externalId = "TEST-ORDER-1";
        String operation = "GET_ORDER_DATA";
        Duration duration = Duration.ofMillis(200);
        String errorType = "TimeoutException";
        String errorMessage = "Request timed out";

        // When
        loggingInterceptor.logRSocketCallFailure(correlationId, externalId, operation, duration, errorType, errorMessage);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        assertThat(logsList.get(0).getLevel()).isEqualTo(Level.WARN);
        assertThat(logsList.get(0).getFormattedMessage()).contains("RSocket call failed");
        assertThat(logsList.get(0).getFormattedMessage()).contains("errorType=TimeoutException");
        assertThat(logsList.get(0).getFormattedMessage()).contains("errorMessage=Request timed out");
        assertThat(logsList.get(0).getFormattedMessage()).contains("duration=200ms");
    }

    @Test
    void shouldLogRSocketTimeout() {
        // Given
        String correlationId = "rsocket-12345678";
        String externalId = "TEST-ORDER-1";
        String operation = "GET_ORDER_DATA";
        Duration timeout = Duration.ofSeconds(5);

        // When
        loggingInterceptor.logRSocketTimeout(correlationId, externalId, operation, timeout);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        assertThat(logsList.get(0).getLevel()).isEqualTo(Level.WARN);
        assertThat(logsList.get(0).getFormattedMessage()).contains("RSocket call timed out after 5000ms");
    }

    @Test
    void shouldLogCircuitBreakerEvent() {
        // Given
        String externalId = "TEST-ORDER-1";
        String operation = "GET_ORDER_DATA";
        String eventType = "FALLBACK_TRIGGERED";
        String state = "OPEN";

        // When
        loggingInterceptor.logCircuitBreakerEvent(externalId, operation, eventType, state);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        assertThat(logsList.get(0).getLevel()).isEqualTo(Level.WARN);
        assertThat(logsList.get(0).getFormattedMessage()).contains("Circuit breaker event");
        assertThat(logsList.get(0).getFormattedMessage()).contains("eventType=FALLBACK_TRIGGERED");
        assertThat(logsList.get(0).getFormattedMessage()).contains("state=OPEN");
    }

    @Test
    void shouldLogRetryAttempt() {
        // Given
        String correlationId = "rsocket-12345678";
        String externalId = "TEST-ORDER-1";
        String operation = "GET_ORDER_DATA";
        int attemptNumber = 2;

        // When
        loggingInterceptor.logRetryAttempt(correlationId, externalId, operation, attemptNumber);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        assertThat(logsList.get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(logsList.get(0).getFormattedMessage()).contains("Retrying RSocket call");
        assertThat(logsList.get(0).getFormattedMessage()).contains("attempt=2");
    }

    @Test
    void shouldLogConnectionEvent() {
        // Given
        String eventType = "CONNECTED";
        String serverHost = "localhost";
        int serverPort = 7000;
        String details = "Connection established successfully";

        // When
        loggingInterceptor.logConnectionEvent(eventType, serverHost, serverPort, details);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        assertThat(logsList.get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(logsList.get(0).getFormattedMessage()).contains("RSocket connection event");
        assertThat(logsList.get(0).getFormattedMessage()).contains("eventType=CONNECTED");
        assertThat(logsList.get(0).getFormattedMessage()).contains("host=localhost:7000");
    }

    @Test
    void shouldLogHealthCheck() {
        // Given
        boolean isHealthy = true;
        Duration duration = Duration.ofMillis(50);
        String details = "Health check passed";

        // When
        loggingInterceptor.logHealthCheck(isHealthy, duration, details);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        assertThat(logsList.get(0).getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logsList.get(0).getFormattedMessage()).contains("Mock server health check passed");
        assertThat(logsList.get(0).getFormattedMessage()).contains("duration=50ms");
    }

    @Test
    void shouldLogHealthCheckFailure() {
        // Given
        boolean isHealthy = false;
        Duration duration = Duration.ofMillis(100);
        String details = "Connection refused";

        // When
        loggingInterceptor.logHealthCheck(isHealthy, duration, details);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        assertThat(logsList.get(0).getLevel()).isEqualTo(Level.WARN);
        assertThat(logsList.get(0).getFormattedMessage()).contains("Mock server health check failed");
        assertThat(logsList.get(0).getFormattedMessage()).contains("details=Connection refused");
    }

    @Test
    void shouldLogConfigurationEvent() {
        // Given
        String eventType = "PROPERTY_CHANGED";
        String configKey = "rsocket.timeout";
        String configValue = "10s";

        // When
        loggingInterceptor.logConfigurationEvent(eventType, configKey, configValue);

        // Then
        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).hasSize(1);
        assertThat(logsList.get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(logsList.get(0).getFormattedMessage()).contains("RSocket configuration event");
        assertThat(logsList.get(0).getFormattedMessage()).contains("eventType=PROPERTY_CHANGED");
        assertThat(logsList.get(0).getFormattedMessage()).contains("key=rsocket.timeout");
    }
}