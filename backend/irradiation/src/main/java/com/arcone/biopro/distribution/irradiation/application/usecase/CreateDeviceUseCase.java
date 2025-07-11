package com.arcone.biopro.distribution.irradiation.application.usecase;

import com.arcone.biopro.distribution.irradiation.application.mapper.DeviceMapper;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CreateDeviceUseCase implements UseCase<Mono<Device>, CreateDeviceUseCase.Input> {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    public CreateDeviceUseCase(DeviceRepository deviceRepository, DeviceMapper deviceMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceMapper = deviceMapper;
    }

    @Override
    public Mono<Device> execute(Input input) {
        if (!"IRRADIATOR".equals(input.deviceCategory())) {
            log.info("Device type is not irradiation, skipping device creation for ID: {}", input.deviceId());
            return Mono.empty();
        }

        Device device = deviceMapper.toDevice(input);
        return deviceRepository.save(device);
    }

    public record Input(String deviceId, String location, String status, String deviceCategory) {}
}
