package com.arcone.biopro.distribution.irradiation.adapter.in.listener;

import com.arcone.biopro.distribution.irradiation.application.usecase.CreateDeviceUseCase;
import org.springframework.stereotype.Component;

@Component
public class DeviceCreatedMapper implements MessageMapper<CreateDeviceUseCase.Input, CheckInCompleted> {

    @Override
    public CreateDeviceUseCase.Input toInput(CheckInCompleted payload) {
        return new CreateDeviceUseCase.Input(payload.id(), payload.location(), payload.status(), payload.deviceCategory());
    }
}
