package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CreateDeviceUseCase implements UseCase<Mono<Device>, CreateDeviceUseCase.Input> {

    private final DeviceRepository deviceRepository;

    public CreateDeviceUseCase(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public Mono<Device> execute(Input input) {
        if (!"IRRADIATOR".equals(input.deviceCategory())) {
            log.info("Device type is not irradiation, skipping device creation for ID: {}", input.deviceId());
            return Mono.empty();
        }
        
        Device device = new Device(
            DeviceId.of(input.deviceId()),
            Location.of(input.location()),
            input.status()
        );
        return deviceRepository.save(device);
    }

    public record Input(String deviceId, String location, String status, String deviceCategory) {}
}
