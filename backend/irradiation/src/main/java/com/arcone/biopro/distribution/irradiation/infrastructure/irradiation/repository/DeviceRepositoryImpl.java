package com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.repository;

import com.arcone.biopro.distribution.irradiation.domain.irradiation.entity.Device;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.port.DeviceRepository;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.DeviceId;
import com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject.Location;
import com.arcone.biopro.distribution.irradiation.infrastructure.irradiation.entity.DeviceEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

interface DeviceEntityRepository extends ReactiveCrudRepository<DeviceEntity, Long> {
    Mono<DeviceEntity> findByDeviceId(String deviceId);
    Mono<DeviceEntity> findByDeviceIdAndLocation(String deviceId, String location);
}

@Repository
public class DeviceRepositoryImpl implements DeviceRepository {
    private final DeviceEntityRepository repository;

    public DeviceRepositoryImpl(DeviceEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Device> findByDeviceId(DeviceId deviceId) {
        return repository.findByDeviceId(deviceId.getValue())
                .map(entity -> new Device(
                        DeviceId.of(entity.getDeviceId()),
                        new Location(entity.getLocation()),
                        entity.getStatus()
                ));
    }

    @Override
    public Mono<Device> findByDeviceIdAndLocation(String deviceId, String location) {
        return repository.findByDeviceIdAndLocation(deviceId, location)
                .map(entity -> new Device(
                        DeviceId.of(entity.getDeviceId()),
                        new Location(entity.getLocation()),
                        entity.getStatus()
                ));
    }

    @Override
    public Mono<Device> save(Device device) {
        DeviceEntity entity = new DeviceEntity(device.getDeviceId().getValue(), device.getLocation().value(), device.getStatus());
        return repository.save(entity)
                .map(savedEntity -> new Device(
                        DeviceId.of(savedEntity.getDeviceId()),
                        new Location(savedEntity.getLocation()),
                        savedEntity.getStatus()
                ));
    }
}
