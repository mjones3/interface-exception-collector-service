package com.arcone.biopro.exception.collector.api.graphql;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.*;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for GraphQL WebSocket subscriptions.
 * Tests real-time updates and subscription filtering.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("integration-test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GraphQLSubscriptionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("exception_collector_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "test_pass");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @LocalServerPort
    private int port;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;
    private final BlockingQueue<Map<String, Object>> receivedMessages = new LinkedBlockingQueue<>();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "test_pass");
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setUp() throws Exception {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSessionHandler sessionHandler = new TestStompSessionHandler();
        String url = "ws://localhost:" + port + "/subscriptions";

        stompSession = stompClient.connect(url, sessionHandler).get(10, TimeUnit.SECONDS);
        receivedMessages.clear();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
        exceptionRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Should receive real-time exception updates via WebSocket")
    void testExceptionUpdatesSubscription() throws Exception {
        // Given - Subscribe to exception updates
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/topic/exceptions");

        stompSession.subscribe(headers, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedMessages.offer((Map<String, Object>) payload);
            }
        });

        // When - Create a new exception (simulating Kafka event)
        InterfaceException exception = createTestException("TXN-WS-001", InterfaceType.ORDER, ExceptionStatus.NEW);
        exceptionRepository.save(exception);

        // Simulate Kafka event that would trigger WebSocket update
        Map<String, Object> kafkaEvent = Map.of(
                "eventType", "ExceptionCaptured",
                "transactionId", "TXN-WS-001",
                "interfaceType", "ORDER",
                "status", "NEW",
                "severity", "HIGH",
                "timestamp", OffsetDateTime.now().toString());

        kafkaTemplate.send("ExceptionCaptured", kafkaEvent);

        // Then - Verify WebSocket message received
        Map<String, Object> receivedMessage = receivedMessages.poll(10, TimeUnit.SECONDS);
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.get("transactionId")).isEqualTo("TXN-WS-001");
        assertThat(receivedMessage.get("eventType")).isEqualTo("ExceptionCaptured");
    }

    @Test
    @Order(2)
    @DisplayName("Should filter subscription updates based on criteria")
    void testFilteredSubscription() throws Exception {
        // Given - Subscribe with filters
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/topic/exceptions/filtered");
        headers.set("interfaceTypes", "ORDER,COLLECTION");
        headers.set("severities", "HIGH,CRITICAL");

        stompSession.subscribe(headers, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedMessages.offer((Map<String, Object>) payload);
            }
        });

        // When - Send events that match and don't match filters
        // This should match (ORDER + HIGH)
        Map<String, Object> matchingEvent = Map.of(
                "eventType", "ExceptionCaptured",
                "transactionId", "TXN-MATCH-001",
                "interfaceType", "ORDER",
                "severity", "HIGH");
        kafkaTemplate.send("ExceptionCaptured", matchingEvent);

        // This should not match (DISTRIBUTION + HIGH)
        Map<String, Object> nonMatchingEvent = Map.of(
                "eventType", "ExceptionCaptured",
                "transactionId", "TXN-NO-MATCH-001",
                "interfaceType", "DISTRIBUTION",
                "severity", "HIGH");
        kafkaTemplate.send("ExceptionCaptured", nonMatchingEvent);

        // Then - Only matching event should be received
        Map<String, Object> receivedMessage = receivedMessages.poll(10, TimeUnit.SECONDS);
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.get("transactionId")).isEqualTo("TXN-MATCH-001");

        // Verify no additional messages received
        Map<String, Object> additionalMessage = receivedMessages.poll(2, TimeUnit.SECONDS);
        assertThat(additionalMessage).isNull();
    }

    @Test
    @Order(3)
    @DisplayName("Should handle multiple concurrent WebSocket connections")
    void testMultipleConnections() throws Exception {
        // Given - Create additional WebSocket connections
        WebSocketStompClient client2 = new WebSocketStompClient(new StandardWebSocketClient());
        client2.setMessageConverter(new MappingJackson2MessageConverter());

        WebSocketStompClient client3 = new WebSocketStompClient(new StandardWebSocketClient());
        client3.setMessageConverter(new MappingJackson2MessageConverter());

        String url = "ws://localhost:" + port + "/subscriptions";
        StompSession session2 = client2.connect(url, new TestStompSessionHandler()).get(10, TimeUnit.SECONDS);
        StompSession session3 = client3.connect(url, new TestStompSessionHandler()).get(10, TimeUnit.SECONDS);

        BlockingQueue<Map<String, Object>> messages2 = new LinkedBlockingQueue<>();
        BlockingQueue<Map<String, Object>> messages3 = new LinkedBlockingQueue<>();

        // Subscribe all sessions
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/topic/exceptions");

        StompFrameHandler handler2 = createMessageHandler(messages2);
        StompFrameHandler handler3 = createMessageHandler(messages3);

        stompSession.subscribe(headers, createMessageHandler(receivedMessages));
        session2.subscribe(headers, handler2);
        session3.subscribe(headers, handler3);

        // When - Send broadcast message
        Map<String, Object> broadcastEvent = Map.of(
                "eventType", "ExceptionResolved",
                "transactionId", "TXN-BROADCAST-001",
                "interfaceType", "ORDER",
                "status", "RESOLVED");
        kafkaTemplate.send("ExceptionResolved", broadcastEvent);

        // Then - All sessions should receive the message
        Map<String, Object> message1 = receivedMessages.poll(10, TimeUnit.SECONDS);
        Map<String, Object> message2 = messages2.poll(10, TimeUnit.SECONDS);
        Map<String, Object> message3 = messages3.poll(10, TimeUnit.SECONDS);

        assertThat(message1).isNotNull();
        assertThat(message2).isNotNull();
        assertThat(message3).isNotNull();

        assertThat(message1.get("transactionId")).isEqualTo("TXN-BROADCAST-001");
        assertThat(message2.get("transactionId")).isEqualTo("TXN-BROADCAST-001");
        assertThat(message3.get("transactionId")).isEqualTo("TXN-BROADCAST-001");

        // Cleanup
        session2.disconnect();
        session3.disconnect();
    }

    @Test
    @Order(4)
    @DisplayName("Should handle WebSocket connection failures gracefully")
    void testConnectionFailureHandling() throws Exception {
        // Given - Subscribe to updates
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/topic/exceptions");

        stompSession.subscribe(headers, createMessageHandler(receivedMessages));

        // When - Disconnect and reconnect
        stompSession.disconnect();

        // Reconnect
        StompSessionHandler sessionHandler = new TestStompSessionHandler();
        String url = "ws://localhost:" + port + "/subscriptions";
        stompSession = stompClient.connect(url, sessionHandler).get(10, TimeUnit.SECONDS);

        // Resubscribe
        stompSession.subscribe(headers, createMessageHandler(receivedMessages));

        // Send event after reconnection
        Map<String, Object> event = Map.of(
                "eventType", "ExceptionCaptured",
                "transactionId", "TXN-RECONNECT-001",
                "interfaceType", "COLLECTION");
        kafkaTemplate.send("ExceptionCaptured", event);

        // Then - Should receive message after reconnection
        Map<String, Object> receivedMessage = receivedMessages.poll(10, TimeUnit.SECONDS);
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.get("transactionId")).isEqualTo("TXN-RECONNECT-001");
    }

    @Test
    @Order(5)
    @DisplayName("Should validate subscription latency requirements")
    void testSubscriptionLatency() throws Exception {
        // Given - Subscribe to updates
        StompHeaders headers = new StompHeaders();
        headers.setDestination("/topic/exceptions");

        long subscriptionStartTime = System.currentTimeMillis();

        stompSession.subscribe(headers, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                Map<String, Object> message = (Map<String, Object>) payload;
                message.put("receivedAt", System.currentTimeMillis());
                receivedMessages.offer(message);
            }
        });

        // When - Send event with timestamp
        long eventSentTime = System.currentTimeMillis();
        Map<String, Object> event = Map.of(
                "eventType", "ExceptionCaptured",
                "transactionId", "TXN-LATENCY-001",
                "interfaceType", "ORDER",
                "sentAt", eventSentTime);
        kafkaTemplate.send("ExceptionCaptured", event);

        // Then - Verify latency is within requirements (< 2 seconds)
        Map<String, Object> receivedMessage = receivedMessages.poll(10, TimeUnit.SECONDS);
        assertThat(receivedMessage).isNotNull();

        long receivedAt = (Long) receivedMessage.get("receivedAt");
        long latency = receivedAt - eventSentTime;

        assertThat(latency).isLessThan(2000); // Less than 2 seconds as per requirement 2.3
    }

    private StompFrameHandler createMessageHandler(BlockingQueue<Map<String, Object>> messageQueue) {
        return new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Map.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.offer((Map<String, Object>) payload);
            }
        };
    }

    private InterfaceException createTestException(String transactionId, InterfaceType interfaceType,
            ExceptionStatus status) {
        return InterfaceException.builder()
                .transactionId(transactionId)
                .externalId("EXT-" + transactionId)
                .interfaceType(interfaceType)
                .exceptionReason("Test exception for WebSocket")
                .operation("CREATE_" + interfaceType.name())
                .status(status)
                .severity(ExceptionSeverity.HIGH)
                .category(ExceptionCategory.VALIDATION)
                .customerId("CUST-001")
                .locationCode("LOC-001")
                .timestamp(OffsetDateTime.now())
                .processedAt(OffsetDateTime.now())
                .retryable(true)
                .retryCount(0)
                .maxRetries(3)
                .build();
    }

    private static class TestStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            // Connection established
        }

        @Override
        public void handleException(StompSession session, StompCommand command,
                StompHeaders headers, byte[] payload, Throwable exception) {
            exception.printStackTrace();
        }
    }
}