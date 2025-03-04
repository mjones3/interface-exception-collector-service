package com.arcone.biopro.distribution.inventory.integration;

import com.arcone.biopro.distribution.inventory.application.usecase.LabelAppliedUseCase;
import com.arcone.biopro.distribution.inventory.domain.model.Inventory;
import com.arcone.biopro.distribution.inventory.domain.model.InventoryAggregate;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.AboRhType;
import com.arcone.biopro.distribution.inventory.domain.model.enumeration.InventoryStatus;
import com.arcone.biopro.distribution.inventory.domain.model.vo.ProductCode;
import com.arcone.biopro.distribution.inventory.domain.model.vo.UnitNumber;
import com.arcone.biopro.distribution.inventory.domain.repository.InventoryAggregateRepository;
import com.arcone.biopro.distribution.inventory.verification.utils.KafkaHelper;
import com.arcone.biopro.distribution.inventory.verification.utils.LogMonitor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.arcone.biopro.distribution.inventory.BioProConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
    ,properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=inventory-updated-test-group",
        "default.location=TestLocation"
    }
    )
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9094", "port=9094"})
public class EventProducerIntegrationIT {

    @Autowired
    private KafkaHelper kafkaHelper;

    @MockBean
    private InventoryAggregateRepository inventoryAggregateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogMonitor logMonitor;

    private static final BlockingQueue<ConsumerRecord<String, String>> receivedRecords = new LinkedBlockingQueue<>();
    @Qualifier("InventoryOutputMapper")
    @Autowired
    private Object object;

    @BeforeEach
    void setUp() {
        var uuid = UUID.randomUUID();
        var inventory = Inventory.builder()
            .id(uuid)
            .unitNumber(new UnitNumber("W123456789012"))
            .productCode(new ProductCode("E123412"))
            .shortDescription("APH PLASMA 24H")
            .inventoryStatus(InventoryStatus.AVAILABLE)
            .expirationDate(LocalDateTime.parse("2025-01-08T02:05:45.231"))
            .collectionDate(ZonedDateTime.now())
            .location("LOCATION_1")
            .productFamily("PLASMA_TRANSFUSABLE")
            .aboRh(AboRhType.ABN)
            .isLabeled(false)
            .build();

        var aggregate = InventoryAggregate.builder()
            .inventory(inventory)
            .build();

        when(inventoryAggregateRepository.findByUnitNumberAndProductCode(anyString(), anyString())).thenReturn(Mono.just(aggregate));
        when(inventoryAggregateRepository.saveInventory(any())).thenReturn(Mono.just(aggregate));
    }

    @Test
    @DisplayName("Should receive label applied event (licensed inventory), map, call usecase and produce the event with the correct information (including the LICENSED property)")
    public void test1() throws InterruptedException, IOException {
        publishCreatedEvent("json/label_applied.json", LABEL_APPLIED_TOPIC);
        assertProducedMessageValues("W123456789012", "E0869V00", "LABEL_APPLIED", "LICENSED");
    }

    @Test
    @DisplayName("Should receive label applied event (unlicensed inventory), map, call usecase and produce the event with the correct information (including the UNLICENSED property)")
    public void test2() throws InterruptedException, IOException {
        publishCreatedEvent("json/label_applied_unlicensed.json", LABEL_APPLIED_TOPIC);
        assertProducedMessageValues("W123456789012", "E0869V00", "LABEL_APPLIED", "LICENSED");
    }

    @Test
    @DisplayName("Should receive shipment completed event, map, call usecase and not generate kafka event since the product is not labeled")
    public void test3() throws InterruptedException, IOException {
        publishCreatedEvent("json/shipment_completed.json", SHIPMENT_COMPLETED_TOPIC);
        assertNoMessageProduced();
    }

    @Test
    @DisplayName("Should receive shipment completed event, receive a label applied event, and produce one event with the correct information about the labeling event")
    public void test4() throws InterruptedException, IOException {
        publishCreatedEventWithTemplate("W123456789012", "E123412", "json/shipment_completed_template.json", SHIPMENT_COMPLETED_TOPIC);
        assertNoMessageProduced();
        publishCreatedEventWithTemplate("W123456789012", "E0869V00", "json/label_applied_template.json", LABEL_APPLIED_TOPIC);
        assertProducedMessageValues("W123456789012", "E0869V00", "LABEL_APPLIED", "LICENSED");
    }

    private JsonNode publishCreatedEvent(String path, String topic) throws IOException, InterruptedException {
        return publishCreatedEventWithTemplate("", "", path, topic);
    }

    private JsonNode publishCreatedEventWithTemplate(String unitNumber, String productCode, String path, String topic) throws IOException, InterruptedException {
        var resource = new ClassPathResource(path).getFile().toPath();
        var inputStream = Files.newInputStream(resource);
        var payload = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        payload = payload.replaceAll("UNIT_NUMBER", unitNumber).replaceAll("PRODUCT_CODE", productCode);
        var payloadJson = objectMapper.readTree(payload);

        kafkaHelper.sendEvent(topic, topic + "test-key", payloadJson).block();
        logMonitor.await("Processed message.*");
        return payloadJson;
    }

    @KafkaListener(topics = "${topic.inventory-updated.name}")
    public void listenCollectionReceived(ConsumerRecord<String, String> record) {
        receivedRecords.add(record);
        log.info("Inventory Updated listener received: {}", record);
    }

    private void assertProducedMessageValues(String unitNumber, String productCode, String updateType, String expectedLicensure) throws InterruptedException, JsonProcessingException {
        var receivedMessage = receivedRecords.poll(5, TimeUnit.SECONDS);

        assertThat(receivedMessage).isNotNull();
        var rootNode = objectMapper.readTree(receivedMessage.value());
        var payload = rootNode.path(PAYLOAD);
        assertThat(payload.path(UNIT_NUMBER).asText()).isEqualTo(unitNumber);
        assertThat(payload.path(PRODUCT_CODE).asText()).isEqualTo(productCode);
        assertThat(payload.path(UPDATE_TYPE).asText()).isEqualTo(updateType);
        assertThat(payload.path(PROPERTIES).path(LICENSURE).asText()).isEqualTo(expectedLicensure);
    }

    private void assertNoMessageProduced() throws InterruptedException {
        var receivedMessage = receivedRecords.poll(5, TimeUnit.SECONDS);

        assertThat(receivedMessage).isNull();
    }

}
