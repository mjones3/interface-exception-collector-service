package com.arcone.biopro.distribution.receiving.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.SenderOptions;

@EnableKafka
@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaConfiguration {

    public static final String DLQ_PRODUCER = "dlq-producer";


    @Bean
    NewTopic productsImportedTopic(
        @Value("${topics.receiving.products-imported.partitions:1}") Integer partitions,
        @Value("${topics.receiving.products-imported.replicas:1}") Integer replicas,
        @Value("${topics.receiving.products-imported.topic-name:ProductsImported}") String topicName
    ) {
        return TopicBuilder.name(topicName).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    SenderOptions<String, String> senderOptions(
        KafkaProperties kafkaProperties,
        ObjectMapper objectMapper) {
        var props = kafkaProperties.buildProducerProperties(null);
        return SenderOptions.<String, String>create(props)
            .withValueSerializer(new JsonSerializer<>(objectMapper))
            .maxInFlight(1); // to keep ordering, prevent duplicate messages (and avoid data loss)
    }
}
