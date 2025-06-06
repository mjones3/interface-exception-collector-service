package com.arcone.biopro.distribution.receiving.domain.repository;

import com.arcone.biopro.distribution.receiving.domain.model.vo.Barcode;
import com.arcone.biopro.distribution.receiving.domain.model.vo.Device;
import com.arcone.biopro.distribution.receiving.domain.model.vo.DeviceType;
import reactor.core.publisher.Mono;

public interface DeviceRepository {

    Mono<Device> findFirstByBloodCenterId(Barcode barcode);

    Mono<Device> findFirstByBloodCenterIdAndActiveIsTrue(Barcode barcode);

    Mono<Device> findFirstByBloodCenterIdAndType(Barcode barcode, DeviceType type);

    Mono<Device> save(Device device);

    Mono<Void> deleteById(Long deviceId);
}

