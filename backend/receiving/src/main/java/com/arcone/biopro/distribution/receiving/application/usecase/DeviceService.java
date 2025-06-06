package com.arcone.biopro.distribution.receiving.application.usecase;

import com.arcone.biopro.distribution.receiving.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.receiving.domain.model.Device;
import com.arcone.biopro.distribution.receiving.domain.model.vo.Barcode;
import com.arcone.biopro.distribution.receiving.domain.repository.DeviceRepository;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.DeviceCreatedMessage;
import com.arcone.biopro.distribution.receiving.infrastructure.dto.DeviceUpdatedMessage;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.DeviceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Transactional
    public Mono<Device> createDevice(DeviceCreatedMessage message) {
        log.debug("Processing DeviceCreatedMessage: {}", message.toString());
        return Mono.fromSupplier(() -> DeviceMapper.INSTANCE.toDomain(message))
            .flatMap(deviceRepository::save)
            .onErrorResume(error -> {
                log.error("Error on creating device {}", error.getMessage());
                return Mono.empty();
            });
    }

    @Transactional
    public Mono<Device> updateDevice(DeviceUpdatedMessage message) {

        log.debug("Processing DeviceUpdatedMessage: {}", message.toString());

        return deviceRepository.findFirstByBloodCenterId(new Barcode(message.getPayload().getId()))
            .map(existingDevice -> DeviceMapper.INSTANCE.toDomain(existingDevice.getId(), message))
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", message.getPayload().getId()))))
            .flatMap(deviceRepository::save);
    }
}
