package com.arcone.biopro.distribution.receiving.unit.infrastructure.listener;

import com.arcone.biopro.distribution.receiving.domain.event.ImportCompletedDomainEvent;
import com.arcone.biopro.distribution.receiving.domain.model.Import;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.ProductsImportedOutputMessage;
import com.arcone.biopro.distribution.receiving.infrastructure.listener.ImportCompletedDomainEventListener;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.ProductsImportedOutputMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

class ImportCompletedDomainEventListenerTest {

    private ImportCompletedDomainEventListener target;
    private ProductsImportedOutputMapper productsImportedOutputMapper;

    private ReactiveKafkaProducerTemplate<String, ProductsImportedOutputMessage> producerTemplate;


    @BeforeEach
    public void setUp() {
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        productsImportedOutputMapper = Mappers.getMapper(ProductsImportedOutputMapper.class);
        target = new ImportCompletedDomainEventListener(producerTemplate, "TestTopic", productsImportedOutputMapper);
    }

    @Test
    public void shouldHandleImportCompletedDomainEvents() {

        RecordMetadata meta = new RecordMetadata(new TopicPartition("TestTopic", 0), 0L, 0L, 0L, 0L, 0, 2);
        SenderResult senderResult = Mockito.mock(SenderResult.class);
        Mockito.when(senderResult.recordMetadata()).thenReturn(meta);
        Mockito.when(producerTemplate.send(Mockito.any(ProducerRecord.class))).thenReturn(Mono.just(senderResult));

        var domainImport = Mockito.mock(Import.class);

        StepVerifier.create(target.handleImportCompletedEvent(new ImportCompletedDomainEvent(domainImport)))
            .verifyComplete();
        Mockito.verify(producerTemplate).send(Mockito.any(ProducerRecord.class));
    }
}
