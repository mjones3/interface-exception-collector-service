package com.arcone.biopro.distribution.receiving.unit.infrastructure.listener;

import com.arcone.biopro.distribution.receiving.application.usecase.DeviceService;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.DeviceUpdatedMessage;
import com.arcone.biopro.distribution.receiving.infrastructure.listener.DeviceUpdatedListener;
import com.arcone.biopro.distribution.receiving.unit.util.TestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
class DeviceUpdatedListenerTest {

    private ReactiveKafkaConsumerTemplate<String, String> consumer;
    private ReactiveKafkaProducerTemplate<String, String> producerTemplate;
    private ObjectMapper objectMapper;
    private ReceiverRecord<String, String> receiverRecord;
    private DeviceService deviceService;

    @BeforeEach
    public void setUp(){

        consumer = Mockito.mock(ReactiveKafkaConsumerTemplate.class);
        producerTemplate = Mockito.mock(ReactiveKafkaProducerTemplate.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        receiverRecord = Mockito.mock(ReceiverRecord.class);
        deviceService = Mockito.mock(DeviceService.class);
    }



    @Test
    public void shouldHandleMessage() throws Exception{

        Mockito.when(deviceService.updateDevice(Mockito.any(DeviceUpdatedMessage.class))).thenReturn(Mono.empty());

        var message = Mockito.mock(DeviceUpdatedMessage.class);

        Mockito.when(message.getEventId()).thenReturn(UUID.randomUUID().toString());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("device-updated-event.json").replace("{DEVICE_ID}", "W035625205983"));
        Mockito.when(receiverRecord.topic()).thenReturn("DeviceUpdated");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));


        var listener = new DeviceUpdatedListener(consumer,objectMapper,deviceService,producerTemplate,"TEST");

        listener.run(new String[]{""});

        Mockito.verify(deviceService).updateDevice(Mockito.any(DeviceUpdatedMessage.class));
    }

    @Test
    public void shouldNotHandleMessageWhenMessageIsInvalid() throws Exception {

        Mockito.when(deviceService.updateDevice(Mockito.any(DeviceUpdatedMessage.class))).thenReturn(Mono.empty());

        var message = Mockito.mock(DeviceUpdatedMessage.class);

        Mockito.when(message.getEventId()).thenReturn(UUID.randomUUID().toString());

        Mockito.when(receiverRecord.key()).thenReturn("test");
        Mockito.when(receiverRecord.value()).thenReturn(TestUtil.resource("device-updated-event.json").replace("\"location\": \"234567891\"", ""));
        Mockito.when(receiverRecord.topic()).thenReturn("DeviceUpdated");
        Mockito.when(receiverRecord.offset()).thenReturn(1L);

        Mockito.when(consumer.receive()).thenReturn(Flux.just(receiverRecord));

        var listener = new DeviceUpdatedListener(consumer,objectMapper,deviceService,producerTemplate,"TEST");

        try{
            listener.run(new String[]{""});
        }catch (Exception e){
            Mockito.verifyNoInteractions(deviceService);
        }
    }

}

