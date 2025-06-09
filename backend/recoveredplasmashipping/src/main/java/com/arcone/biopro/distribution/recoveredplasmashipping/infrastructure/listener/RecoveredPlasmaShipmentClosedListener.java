package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.listener;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentClosedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.config.KafkaConfiguration;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonClosedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaShipmentClosedOutputEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentClosedEventMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonEntityRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonItemEntityRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LocationPropertyEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.LocationPropertyEntityRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentEntityRepository;
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

import java.util.List;

import static org.springframework.kafka.support.mapping.AbstractJavaTypeMapper.DEFAULT_CLASSID_FIELD_NAME;

@Component
@Slf4j
@Profile("prod")
public class RecoveredPlasmaShipmentClosedListener {

    private final ReactiveKafkaProducerTemplate<String, RecoveredPlasmaShipmentClosedOutputEvent> producerTemplate;
    private final String topicName;
    private final RecoveredPlasmaShipmentClosedEventMapper recoveredPlasmaShipmentEventMapper;
    private final CartonItemEntityRepository cartonItemEntityRepository;
    private final CartonEntityRepository cartonEntityRepository;
    private final RecoveredPlasmaShipmentEntityRepository recoveredPlasmaShippingRepository;
    private final LocationPropertyEntityRepository locationPropertyEntityRepository;


    public RecoveredPlasmaShipmentClosedListener(@Qualifier(KafkaConfiguration.RPS_SHIPMENT_CLOSED_PRODUCER) ReactiveKafkaProducerTemplate<String, RecoveredPlasmaShipmentClosedOutputEvent> producerTemplate,
                                                 @Value("${topics.recovered-plasma-shipment.shipment-closed.topic-name:RecoveredPlasmaShipmentClosed}") String topicName
        , RecoveredPlasmaShipmentClosedEventMapper recoveredPlasmaShipmentEventMapper
        , CartonItemEntityRepository cartonItemEntityRepository
        , RecoveredPlasmaShipmentEntityRepository recoveredPlasmaShippingRepository
        , CartonEntityRepository cartonEntityRepository , LocationPropertyEntityRepository locationPropertyEntityRepository) {
        this.producerTemplate = producerTemplate;
        this.topicName = topicName;
        this.recoveredPlasmaShipmentEventMapper = recoveredPlasmaShipmentEventMapper;
        this.cartonItemEntityRepository = cartonItemEntityRepository;
        this.recoveredPlasmaShippingRepository = recoveredPlasmaShippingRepository;
        this.cartonEntityRepository = cartonEntityRepository;
        this.locationPropertyEntityRepository = locationPropertyEntityRepository;
    }

    @AsyncPublisher(operation = @AsyncOperation(
        channelName = "RecoveredPlasmaShipmentClosed",
        description = "Recovered Plasma Shipment Closed Event",
        headers = @AsyncOperation.Headers(values = @AsyncOperation.Headers.Header(
            name = DEFAULT_CLASSID_FIELD_NAME,
            description = "Spring Type Id Header",
            value = "com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaShipmentClosedOutputEvent"
        )),
        message = @AsyncMessage(
            name = "RecoveredPlasmaShipmentClosed",
            title = "RecoveredPlasmaShipmentClosed",
            description = "Recovered Plasma Shipment Closed Payload"
        ), payloadType = RecoveredPlasmaShipmentClosedOutputEvent.class
    ))
    @KafkaAsyncOperationBinding
    @EventListener
    public Mono<Void> handleShipmentClosedEvent(RecoveredPlasmaShipmentClosedEvent event) {
        return recoveredPlasmaShippingRepository.findById(event.getPayload().getId())
            .zipWith(locationPropertyEntityRepository.findAllByLocationCode(event.getPayload().getLocationCode()).collectList())
            .zipWhen(recoveredPlasmaShipmentEntity ->  getCartonList(event.getPayload()))
            .map(tuple -> recoveredPlasmaShipmentEventMapper.entityToCloseEventDTO(tuple.getT1().getT1() , tuple.getT2() , getLocationProperty(tuple.getT1().getT2(),"RPS_LOCATION_SHIPMENT_CODE") , getLocationProperty(tuple.getT1().getT2(),"RPS_LOCATION_CARTON_CODE") ))
            .publishOn(Schedulers.boundedElastic())
            .map(recoveredPlasmaShipmentClosedOutputDTO -> {
                RecoveredPlasmaShipmentClosedOutputEvent recoveredPlasmaShipmentClosedOutputEvent = new RecoveredPlasmaShipmentClosedOutputEvent(recoveredPlasmaShipmentClosedOutputDTO);
                return producerTemplate.send(new ProducerRecord<>(topicName, String.format("%s", recoveredPlasmaShipmentClosedOutputEvent.getEventId()), recoveredPlasmaShipmentClosedOutputEvent))
                    .subscribe();

            })
            .onErrorResume(error -> {
                log.error("Not able to process Recovered Plasma Shipment Closed event {}", error.getMessage());
                return Mono.empty();
            })
            .then(Mono.empty());
    }

    private Mono<List<RecoveredPlasmaCartonClosedOutputDTO>> getCartonList(RecoveredPlasmaShipment recoveredPlasmaShipment) {

        return cartonEntityRepository.findAllByShipmentIdAndDeleteDateIsNullOrderByCartonSequenceNumberAsc(recoveredPlasmaShipment.getId())
            .flatMap(cartonEntity -> {
                return cartonItemEntityRepository.findAllByCartonIdOrderByCreateDateAsc(cartonEntity.getId()).collectList()
                    .map(cartonItemEntityList -> recoveredPlasmaShipmentEventMapper.cartonModelToEventDTO(cartonEntity, recoveredPlasmaShipment, cartonItemEntityList));
            }).collectList();
    }

    private String getLocationProperty(List<LocationPropertyEntity> propertyEntities , String key){
        if(propertyEntities == null || propertyEntities.isEmpty()){
            return null;
        }
        return propertyEntities.stream().filter(property -> property.getPropertyKey().equals(key)).findAny()
            .map(LocationPropertyEntity::getPropertyValue)
            .orElse(null);
    }
}
