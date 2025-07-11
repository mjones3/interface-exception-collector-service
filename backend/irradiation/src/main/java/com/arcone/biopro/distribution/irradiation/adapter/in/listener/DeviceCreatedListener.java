package com.arcone.biopro.distribution.irradiation.adapter.in.listener;

import com.arcone.biopro.distribution.irradiation.application.usecase.CreateDeviceUseCase;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeviceCreatedListener extends AbstractListener<CreateDeviceUseCase.Input, Device, CheckInCompleted> {

    public DeviceCreatedListener(ReactiveKafkaConsumerTemplate<String, String> consumer,
                                ObjectMapper objectMapper,
                                ReactiveKafkaProducerTemplate<String, String> producerDLQTemplate,
                                CreateDeviceUseCase useCase,
                                DeviceCreatedMapper mapper) {
        super(consumer, objectMapper, producerDLQTemplate, useCase, mapper);
    }

    @Override
    protected TypeReference<EventMessage<CheckInCompleted>> getMessageTypeReference() {
        return new TypeReference<EventMessage<CheckInCompleted>>() {};
    }
}
