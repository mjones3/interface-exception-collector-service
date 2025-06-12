package com.arcone.biopro.distribution.receiving.infrastructure.listener;

import com.arcone.biopro.distribution.receiving.domain.event.ImportCompletedDomainEvent;
import com.arcone.biopro.distribution.receiving.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.ProductsImportedOutputMessage;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.ProductsImportedOutputMapper;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncMessage;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Component
@Slf4j
@Profile("prod")
public class ImportCompletedDomainEventListener {

    private final ReactiveKafkaProducerTemplate<String, ProductsImportedOutputMessage> producerTemplate;
    private final String topicName;
    private final ProductsImportedOutputMapper productsImportedOutputMapper;

    public ImportCompletedDomainEventListener(@Qualifier(KafkaConfiguration.IMPORT_COMPLETED_PRODUCER) ReactiveKafkaProducerTemplate<String, ProductsImportedOutputMessage> producerTemplate,
                                              @Value("${topics.receiving.products-imported.topic-name:ProductsImported}") String topicName , ProductsImportedOutputMapper productsImportedOutputMapper) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.productsImportedOutputMapper = productsImportedOutputMapper;

    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "ProductsImported",
        description = "Products Imported Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.receiving.infrastructure.dto.ProductsImportedOutputMessage"
        )),
        message = @AsyncMessage(
            name = "ProductsImported",
            title = "ProductsImported",
            description = "Products Imported Event Payload"
        ),payloadType = ProductsImportedOutputMessage.class
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public Mono<Void> handleImportCompletedEvent(ImportCompletedDomainEvent event) {
        log.debug("Import Completed event trigger Event ID {}", event);
        var payload = event.getPayload();
        return Mono.just(new ProductsImportedOutputMessage(productsImportedOutputMapper.toOutput(payload)))
            .publishOn(Schedulers.boundedElastic())
            .map(eventPayload -> {
                log.debug("Products Imported event sent {}", eventPayload);
                var producerRecord = new ProducerRecord<>(topicName, String.format("%s", eventPayload.getEventId()), eventPayload);
                return producerTemplate.send(producerRecord)
                    .log()
                    .subscribe();
            })
            .then();
    }
}
