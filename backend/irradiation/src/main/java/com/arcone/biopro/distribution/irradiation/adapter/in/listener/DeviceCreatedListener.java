package com.arcone.biopro.distribution.irradiation.adapter.in.listener;

import com.arcone.biopro.distribution.irradiation.adapter.common.EventMessage;
import com.arcone.biopro.distribution.irradiation.application.usecase.CreateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeviceCreatedListener extends AbstractListener<CreateDeviceUseCase.Input, Device, DeviceCreated> {

    public DeviceCreatedListener(@Qualifier("deviceCreatedTopic")ReactiveKafkaConsumerTemplate<String, String> consumer,
                                ObjectMapper objectMapper,
                                ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                CreateDeviceUseCase useCase,
                                DeviceCreatedMapper mapper) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);
    }

    @Override
    protected TypeReference<EventMessage<DeviceCreated>> getMessageTypeReference() {
        return new TypeReference<EventMessage<DeviceCreated>>() {};
    }
}
