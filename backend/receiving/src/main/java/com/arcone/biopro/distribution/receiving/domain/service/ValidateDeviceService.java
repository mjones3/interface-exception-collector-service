package com.arcone.biopro.distribution.receiving.domain.service;

import com.arcone.biopro.distribution.receiving.application.dto.DeviceOutput;
import com.arcone.biopro.distribution.receiving.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.receiving.application.dto.ValidateDeviceInput;
import reactor.core.publisher.Mono;

public interface ValidateDeviceService {
    Mono<UseCaseOutput<DeviceOutput>> validateDevice(ValidateDeviceInput validateDeviceInput);
}
