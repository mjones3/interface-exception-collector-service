package com.arcone.biopro.distribution.receiving.infrastructure.persistence;

import com.arcone.biopro.distribution.receiving.domain.model.vo.Barcode;
import com.arcone.biopro.distribution.receiving.domain.model.vo.Device;
import com.arcone.biopro.distribution.receiving.domain.model.vo.DeviceType;
import com.arcone.biopro.distribution.receiving.domain.repository.DeviceRepository;
import com.arcone.biopro.distribution.receiving.infrastructure.mapper.DeviceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DeviceRepositoryImpl implements DeviceRepository {

    private final DeviceEntityRepository deviceEntityRepository;

    @Override
    public Mono<Device> findFirstByBloodCenterIdAndActiveIsTrue(Barcode barcode) {
        return deviceEntityRepository.findFirstByBloodCenterIdAndActiveIsTrue(barcode.bloodCenterId())
            .map(DeviceMapper.INSTANCE::toDomain);
    }

    @Override
    public Mono<Device> findFirstByBloodCenterId(Barcode barcode) {
        return deviceEntityRepository.findFirstByBloodCenterId(barcode.bloodCenterId())
            .map(DeviceMapper.INSTANCE::toDomain);
    }

    public Mono<Device> save(Device device) {
        return deviceEntityRepository.save(DeviceMapper.INSTANCE.toEntity(device))
            .map(DeviceMapper.INSTANCE::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return deviceEntityRepository.deleteById(id);
    }

    @Override
    public Mono<Device> findFirstByBloodCenterIdAndType(Barcode barcode, DeviceType type) {
        return deviceEntityRepository.findFirstByBloodCenterIdAndTypeAndActiveIsTrue(barcode.bloodCenterId(),
            type.value()).map(DeviceMapper.INSTANCE::toDomain);
    }


}
