package com.arcone.biopro.distribution.irradiation.adapter.in.listener;

import com.arcone.biopro.distribution.irradiation.application.usecase.CreateDeviceUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeviceCreatedMapper implements MessageMapper<CreateDeviceUseCase.Input, DeviceCreatedPayload> {

    @Override
    public CreateDeviceUseCase.Input toInput(DeviceCreatedPayload payload) {
        return new CreateDeviceUseCase.Input(payload.id(), payload.location(), payload.status(), payload.deviceCategory());
    }
}
