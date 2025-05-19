package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.listener;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.event.RecoveredPlasmaShipmentClosedEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonClosedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaShipmentClosedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.event.RecoveredPlasmaShipmentClosedOutputEvent;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.listener.RecoveredPlasmaShipmentClosedListener;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentClosedEventMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonEntityRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonItemEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonItemEntityRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentEntity;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentEntityRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecoveredPlasmaShipmentClosedListenerTest {

    @Mock
    private ReactiveKafkaProducerTemplate<String, RecoveredPlasmaShipmentClosedOutputEvent> producerTemplate;

    @Mock
    private RecoveredPlasmaShipmentClosedEventMapper recoveredPlasmaShipmentEventMapper;

    @Mock
    private CartonItemEntityRepository cartonItemEntityRepository;

    @Mock
    private CartonEntityRepository cartonEntityRepository;

    @Mock
    private RecoveredPlasmaShipmentEntityRepository recoveredPlasmaShippingRepository;

    private RecoveredPlasmaShipmentClosedListener listener;
    private final String topicName = "test-topic";

    @BeforeEach
    void setUp() {
        listener = new RecoveredPlasmaShipmentClosedListener(
            producerTemplate,
            topicName,
            recoveredPlasmaShipmentEventMapper,
            cartonItemEntityRepository,
            recoveredPlasmaShippingRepository,
            cartonEntityRepository
        );
    }

    @Test
    void handleShipmentClosedEvent_Success() {
        // Arrange
        Long shipmentId = 1L;
        RecoveredPlasmaShipment shipment = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(shipment.getId()).thenReturn(shipmentId);


        RecoveredPlasmaShipmentClosedEvent event = new RecoveredPlasmaShipmentClosedEvent(shipment);

        RecoveredPlasmaShipmentEntity shipmentEntity = RecoveredPlasmaShipmentEntity.builder().build();
        CartonEntity cartonEntity = CartonEntity
            .builder()
            .id(1L)
            .build();


        List<CartonItemEntity> cartonItems = List.of(CartonItemEntity.builder().build());

        RecoveredPlasmaCartonClosedOutputDTO cartonDTO = RecoveredPlasmaCartonClosedOutputDTO.builder().build();

        List<RecoveredPlasmaCartonClosedOutputDTO> cartonDTOs = List.of(cartonDTO);

        RecoveredPlasmaShipmentClosedOutputDTO outputDTO = RecoveredPlasmaShipmentClosedOutputDTO.builder().build();
        RecoveredPlasmaShipmentClosedOutputEvent outputEvent = new RecoveredPlasmaShipmentClosedOutputEvent(outputDTO);

        // Mock repository calls
        when(recoveredPlasmaShippingRepository.findById(shipmentId))
            .thenReturn(Mono.just(shipmentEntity));

        when(cartonEntityRepository.findAllByShipmentIdAndDeleteDateIsNullOrderByCartonSequenceNumberAsc(shipmentId))
            .thenReturn(Flux.just(cartonEntity));

        when(cartonItemEntityRepository.findAllByCartonIdOrderByCreateDateAsc(cartonEntity.getId()))
            .thenReturn(Flux.fromIterable(cartonItems));

        when(recoveredPlasmaShipmentEventMapper.cartonModelToEventDTO(
            any(CartonEntity.class),
            any(RecoveredPlasmaShipment.class),
            anyList()))
            .thenReturn(cartonDTO);

        when(recoveredPlasmaShipmentEventMapper.entityToCloseEventDTO(
            any(RecoveredPlasmaShipmentEntity.class),
            anyList()))
            .thenReturn(outputDTO);

        when(producerTemplate.send(any(ProducerRecord.class)))
            .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(listener.handleShipmentClosedEvent(event))
            .verifyComplete();

        // Verify interactions
        verify(recoveredPlasmaShippingRepository).findById(shipmentId);
        verify(cartonEntityRepository)
            .findAllByShipmentIdAndDeleteDateIsNullOrderByCartonSequenceNumberAsc(shipmentId);
        verify(cartonItemEntityRepository)
            .findAllByCartonIdOrderByCreateDateAsc(cartonEntity.getId());
        verify(recoveredPlasmaShipmentEventMapper)
            .entityToCloseEventDTO(any(RecoveredPlasmaShipmentEntity.class), anyList());
        verify(producerTemplate).send(any(ProducerRecord.class));
    }

    @Test
    void handleShipmentClosedEvent_WhenError_ReturnsEmptyMono() {
        // Arrange
        Long shipmentId = 1L;
        RecoveredPlasmaShipment shipment = Mockito.mock(RecoveredPlasmaShipment.class);
        Mockito.when(shipment.getId()).thenReturn(shipmentId);


        RecoveredPlasmaShipmentClosedEvent event = new RecoveredPlasmaShipmentClosedEvent(shipment);

        when(recoveredPlasmaShippingRepository.findById(shipmentId)).thenReturn(Mono.error(new RuntimeException("Test error")));

        // Act & Assert
        StepVerifier.create(listener.handleShipmentClosedEvent(event))
            .verifyComplete();

        verify(recoveredPlasmaShippingRepository).findById(shipmentId);
        verifyNoInteractions(producerTemplate);
    }
}

