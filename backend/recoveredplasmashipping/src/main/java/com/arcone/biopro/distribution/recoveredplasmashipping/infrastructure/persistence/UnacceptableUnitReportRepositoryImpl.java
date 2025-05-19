package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.UnacceptableUnitReportItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.UnacceptableUnitReportRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.UnacceptableUnitReportEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class UnacceptableUnitReportRepositoryImpl implements UnacceptableUnitReportRepository {
    private final UnacceptableUnitReportEntityRepository unacceptableUnitReportEntityRepository;
    private final UnacceptableUnitReportEntityMapper unacceptableUnitReportMapper;


    @Override
    public Flux<UnacceptableUnitReportItem> findAllByShipmentId(Long shipmentId) {
        return unacceptableUnitReportEntityRepository.findAllByShipmentId(shipmentId)
            .map(unacceptableUnitReportMapper::toModel);
    }

    @Override
    public Mono<UnacceptableUnitReportItem> save(UnacceptableUnitReportItem unacceptableUnitReportItem) {
        return unacceptableUnitReportEntityRepository.save(unacceptableUnitReportMapper.toEntity(unacceptableUnitReportItem))
            .map(unacceptableUnitReportMapper::toModel);
    }

    @Override
    public Mono<Void> deleteAllByShipmentId(Long shipmentId) {
        return unacceptableUnitReportEntityRepository.deleteAllByShipmentId(shipmentId);
    }
}
