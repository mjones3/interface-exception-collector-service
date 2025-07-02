package com.arcone.biopro.distribution.irradiation.verification.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

import java.io.IOException;
import java.nio.file.Files;


@Component
@Slf4j
public class KafkaHelper {

    private final ReactiveKafkaProducerTemplate<String, Object> template;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LogMonitor logMonitor;

    public KafkaHelper(ReactiveKafkaProducerTemplate<String, Object> template) {
        this.template = template;
    }

    public Mono<SenderResult<Void>> sendEvent(String topic, String key, Object message) {
        log.info("Sending Kafka Message {} {}", topic, message);
        var producerRecord = new ProducerRecord<>(topic, key, message);
        return template.send(producerRecord);
    }

    /**
     * Publishes an event from a JSON file to a Kafka topic and waits for processing confirmation.
     *
     * @param path Path to the JSON file containing the event payload
     * @param topic Kafka topic to publish the event to
     * @return The JSON payload that was published
     * @throws IOException If there's an error reading the JSON file
     * @throws InterruptedException If the waiting process is interrupted
     */
    // The try-with-resources statement ensures that the InputStream is properly closed after use
    public JsonNode publishEvent(String path, String topic) throws IOException, InterruptedException {
        var resource = new ClassPathResource(path).getFile().toPath();
        JsonNode payloadJson;
        try (var inputStream = Files.newInputStream(resource)) {
            payloadJson = objectMapper.readTree(inputStream);
        }
        sendEvent(topic, topic + "test-key", payloadJson).block();
        logMonitor.await("Processed message.*");
        return payloadJson;
    }
}

