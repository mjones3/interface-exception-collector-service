package com.arcone.biopro.exception.collector.performance;

import com.arcone.biopro.exception.collector.domain.entity.InterfaceException;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionCategory;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionSeverity;
import com.arcone.biopro.exception.collector.domain.enums.ExceptionStatus;
import com.arcone.biopro.exception.collector.domain.enums.InterfaceType;
import com.arcone.biopro.exception.collector.domain.event.inbound.OrderRejectedEvent;
import com.arcone.biopro.exception.collector.infrastructure.repository.InterfaceExceptionRepository;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Performance tests for high-volume exception processing scenarios.
 * Tests system behavior under load with large numbers of concurrent exceptions.
 * 
 * Run with: -Dperformance.tests.enabled=true
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@EmbeddedKafka(partitions = 3, topics = { "OrderRejected", "CollectionRejected", "DistributionFailed",
        "ValidationError" })
@TestPropertySource(properties = {
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=performance-test-group",
        "spring.kafka.consumer.concurrency=3",
        "app.security.rate-limit.enabled=false",
        "logging.level.com.arcone.biopro=WARN"
})
@DirtiesContext
@EnabledIfSystemProperty(named = "performance.tests.enabled", matches = "true")
class HighVolumeExceptionPerformanceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("performance_test_db")
            .withUsername("perf_user")
            .withPassword("perf_pass")
            .withCommand("postgres", "-c", "max_connections=200", "-c", "shared_buffers=256MB");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withCommand("redis-server", "--maxmemory", "256mb", "--maxmemory-policy", "allkeys-lru");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "20");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private InterfaceExceptionRepository exceptionRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private Producer<String, Object> kafkaProducer;

    @BeforeEach
    void setUp() {
        exceptionRepository.deleteAll();

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producerProps.put("key.serializer", StringSerializer.class);
        producerProps.put("value.serializer", JsonSerializer.class);
        producerProps.put("batch.size", 16384);
        producerProps.put("linger.ms", 10);
        producerProps.put("buffer.memory", 33554432);
        kafkaProducer = new DefaultKafkaProducerFactory<String, Object>(producerProps).createProducer();
    }

    @Test
    @DisplayName("High Volume Exception Processing - 1000 Events")
    void shouldProcessHighVolumeExceptions() throws Exception {
        // Given
        int eventCount = 1000;
        long startTime = System.currentTimeMillis();

        // When - Send high volume of events
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < eventCount; i++) {
            final int eventIndex = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    String transactionId = "perf-test-" + eventIndex;
                    OrderRejectedEvent event = createOrderRejectedEvent(transactionId,
                            "Performance test exception " + eventIndex, "CREATE_ORDER");

                    kafkaProducer.send(new ProducerRecord<>("OrderRejected", transactionId, event));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all events to be sent
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        kafkaProducer.flush();

        long sendTime = System.currentTimeMillis() - startTime;
        System.out.println("Time to send " + eventCount + " events: " + sendTime + "ms");

        // Then - Wait for all events to be processed
        await().atMost(Duration.ofMinutes(2))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    long processedCount = exceptionRepository.count();
                    assertThat(processedCount).isEqualTo(eventCount);
                });

        long totalTime = System.currentTimeMillis() - startTime;
        double throughput = (double) eventCount / (totalTime / 1000.0);

        System.out.println("Total processing time: " + totalTime + "ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " events/second");

        // Verify data integrity
        List<InterfaceException> exceptions = exceptionRepository.findAll();
        assertThat(exceptions).hasSize(eventCount);
        assertThat(exceptions).allMatch(ex -> ex.getInterfaceType() == InterfaceType.ORDER);
        assertThat(exceptions).allMatch(ex -> ex.getStatus() == ExceptionStatus.NEW);

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Concurrent API Load Test - 500 Concurrent Requests")
    void shouldHandleConcurrentApiLoad() throws Exception {
        // Given - Create test data
        int testDataCount = 100;
        createTestExceptions(testDataCount);

        await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> {
                    long count = exceptionRepository.count();
                    assertThat(count).isEqualTo(testDataCount);
                });

        // When - Execute concurrent API requests
        int concurrentRequests = 500;
        ExecutorService executor = Executors.newFixedThreadPool(50);
        List<CompletableFuture<ResponseEntity<String>>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < concurrentRequests; i++) {
            CompletableFuture<ResponseEntity<String>> future = CompletableFuture.supplyAsync(() -> {
                String url = "http://localhost:" + port + "/api/v1/exceptions?page=0&size=10";
                return restTemplate.getForEntity(url, String.class);
            }, executor);
            futures.add(future);
        }

        // Wait for all requests to complete
        List<ResponseEntity<String>> responses = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long totalTime = System.currentTimeMillis() - startTime;
        double throughput = (double) concurrentRequests / (totalTime / 1000.0);

        System.out.println("API Load Test Results:");
        System.out.println("Concurrent requests: " + concurrentRequests);
        System.out.println("Total time: " + totalTime + "ms");
        System.out.println("Throughput: " + String.format("%.2f", throughput) + " requests/second");

        // Then - Verify all requests succeeded
        long successCount = responses.stream()
                .mapToLong(response -> response.getStatusCode() == HttpStatus.OK ? 1 : 0)
                .sum();

        assertThat(successCount).isEqualTo(concurrentRequests);

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Database Query Performance Under Load")
    void shouldMaintainQueryPerformanceUnderLoad() throws Exception {
        // Given - Create large dataset
        int datasetSize = 5000;
        createTestExceptions(datasetSize);

        await().atMost(Duration.ofMinutes(1))
                .untilAsserted(() -> {
                    long count = exceptionRepository.count();
                    assertThat(count).isEqualTo(datasetSize);
                });

        // When & Then - Test various query performance scenarios

        // Test pagination performance
        long startTime = System.currentTimeMillis();
        for (int page = 0; page < 10; page++) {
            String url = "http://localhost:" + port + "/api/v1/exceptions?page=" + page + "&size=50";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
        long paginationTime = System.currentTimeMillis() - startTime;
        System.out.println("Pagination query time (10 pages): " + paginationTime + "ms");

        // Test filtering performance
        startTime = System.currentTimeMillis();
        String filterUrl = "http://localhost:" + port
                + "/api/v1/exceptions?interfaceType=ORDER&status=NEW&severity=MEDIUM";
        ResponseEntity<String> filterResponse = restTemplate.getForEntity(filterUrl, String.class);
        assertThat(filterResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        long filterTime = System.currentTimeMillis() - startTime;
        System.out.println("Filter query time: " + filterTime + "ms");

        // Test search performance
        startTime = System.currentTimeMillis();
        String searchUrl = "http://localhost:" + port + "/api/v1/exceptions/search?query=test&fields=exceptionReason";
        ResponseEntity<String> searchResponse = restTemplate.getForEntity(searchUrl, String.class);
        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        long searchTime = System.currentTimeMillis() - startTime;
        System.out.println("Search query time: " + searchTime + "ms");

        // Performance assertions
        assertThat(paginationTime).isLessThan(5000); // Should complete within 5 seconds
        assertThat(filterTime).isLessThan(1000); // Should complete within 1 second
        assertThat(searchTime).isLessThan(2000); // Should complete within 2 seconds
    }

    @Test
    @DisplayName("Memory Usage Under High Load")
    void shouldMaintainMemoryUsageUnderHighLoad() throws Exception {
        // Given
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // When - Process large number of events in batches
        int batchSize = 200;
        int batchCount = 10;

        for (int batch = 0; batch < batchCount; batch++) {
            createTestExceptionsBatch(batchSize, batch * batchSize);

            // Wait for batch to be processed
            final int expectedCount = (batch + 1) * batchSize;
            await().atMost(Duration.ofSeconds(30))
                    .untilAsserted(() -> {
                        long count = exceptionRepository.count();
                        assertThat(count).isEqualTo(expectedCount);
                    });

            // Force garbage collection and check memory
            System.gc();
            Thread.sleep(1000);

            long currentMemory = runtime.totalMemory() - runtime.freeMemory();
            long memoryIncrease = currentMemory - initialMemory;

            System.out.println("Batch " + (batch + 1) + " - Memory increase: " +
                    (memoryIncrease / 1024 / 1024) + "MB");
        }

        // Then - Verify memory usage is reasonable
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long totalMemoryIncrease = finalMemory - initialMemory;

        System.out.println("Total memory increase: " + (totalMemoryIncrease / 1024 / 1024) + "MB");

        // Memory increase should be reasonable (less than 500MB for this test)
        assertThat(totalMemoryIncrease).isLessThan(500 * 1024 * 1024);
    }

    @Test
    @DisplayName("System Recovery After Overload")
    void shouldRecoverAfterSystemOverload() throws Exception {
        // Given - Overload the system
        int overloadEventCount = 2000;

        // Send events rapidly without waiting
        for (int i = 0; i < overloadEventCount; i++) {
            String transactionId = "overload-test-" + i;
            OrderRejectedEvent event = createOrderRejectedEvent(transactionId,
                    "Overload test exception " + i, "CREATE_ORDER");
            kafkaProducer.send(new ProducerRecord<>("OrderRejected", transactionId, event));
        }
        kafkaProducer.flush();

        // When - Wait for system to process all events
        await().atMost(Duration.ofMinutes(3))
                .pollInterval(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    long processedCount = exceptionRepository.count();
                    assertThat(processedCount).isEqualTo(overloadEventCount);
                });

        // Then - Verify system is still responsive
        String healthUrl = "http://localhost:" + port + "/actuator/health";
        ResponseEntity<String> healthResponse = restTemplate.getForEntity(healthUrl, String.class);
        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify API is still functional
        String apiUrl = "http://localhost:" + port + "/api/v1/exceptions?page=0&size=10";
        ResponseEntity<String> apiResponse = restTemplate.getForEntity(apiUrl, String.class);
        assertThat(apiResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify data integrity
        List<InterfaceException> exceptions = exceptionRepository.findAll();
        assertThat(exceptions).hasSize(overloadEventCount);
        assertThat(exceptions).allMatch(ex -> ex.getTransactionId().startsWith("overload-test-"));
    }

    private void createTestExceptions(int count) throws Exception {
        for (int i = 0; i < count; i++) {
            String transactionId = "load-test-" + i;
            OrderRejectedEvent event = createOrderRejectedEvent(transactionId,
                    "Load test exception " + i, "CREATE_ORDER");
            kafkaProducer.send(new ProducerRecord<>("OrderRejected", transactionId, event));
        }
        kafkaProducer.flush();
    }

    private void createTestExceptionsBatch(int batchSize, int startIndex) throws Exception {
        for (int i = 0; i < batchSize; i++) {
            String transactionId = "batch-test-" + (startIndex + i);
            OrderRejectedEvent event = createOrderRejectedEvent(transactionId,
                    "Batch test exception " + (startIndex + i), "CREATE_ORDER");
            kafkaProducer.send(new ProducerRecord<>("OrderRejected", transactionId, event));
        }
        kafkaProducer.flush();
    }

    private OrderRejectedEvent createOrderRejectedEvent(String transactionId, String rejectedReason, String operation) {
        return OrderRejectedEvent.builder()
                .eventId("event-" + transactionId)
                .eventType("OrderRejected")
                .eventVersion("1.0")
                .occurredOn(OffsetDateTime.now())
                .source("order-service")
                .correlationId("corr-" + transactionId)
                .payload(OrderRejectedEvent.OrderRejectedPayload.builder()
                        .transactionId(transactionId)
                        .externalId("EXT-" + transactionId)
                        .operation(operation)
                        .rejectedReason(rejectedReason)
                        .customerId("CUST-PERF-" + (transactionId.hashCode() % 100))
                        .locationCode("LOC-PERF-" + (transactionId.hashCode() % 10))
                        .build())
                .build();
    }
}