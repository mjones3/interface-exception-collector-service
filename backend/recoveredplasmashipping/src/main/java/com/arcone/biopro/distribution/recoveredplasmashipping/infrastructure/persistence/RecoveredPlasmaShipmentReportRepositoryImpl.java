package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Page;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentQueryCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentReport;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentReportRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentReportEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.data.relational.core.query.Criteria.where;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RecoveredPlasmaShipmentReportRepositoryImpl implements RecoveredPlasmaShipmentReportRepository , FilterAndSortRepository<RecoveredPlasmaShipmentReportEntity> {

    private final R2dbcEntityTemplate entityTemplate;
    private final RecoveredPlasmaShipmentReportEntityMapper recoveredPlasmaShipmentReportEntityMapper;

    @Override
    public Mono<Page<RecoveredPlasmaShipmentReport>> search(RecoveredPlasmaShipmentQueryCommand recoveredPlasmaShipmentQueryCommand) {
        var criteria = Criteria.empty();

        if (recoveredPlasmaShipmentQueryCommand.getShipmentNumber() != null ) {
            criteria = criteria.and(where("shipmentNumber").is(recoveredPlasmaShipmentQueryCommand.getShipmentNumber()));
        } else {
            if (nonNull(recoveredPlasmaShipmentQueryCommand.getCustomers()) && !recoveredPlasmaShipmentQueryCommand.getCustomers().isEmpty()) {
                criteria = criteria.and(where("customerCode").in(recoveredPlasmaShipmentQueryCommand.getCustomers()));
            }
            if (nonNull(recoveredPlasmaShipmentQueryCommand.getProductTypes()) && !recoveredPlasmaShipmentQueryCommand.getProductTypes().isEmpty()) {
                criteria = criteria.and(where("productType").in(recoveredPlasmaShipmentQueryCommand.getProductTypes()));
            }
            if (nonNull(recoveredPlasmaShipmentQueryCommand.getShipmentStatus()) && !recoveredPlasmaShipmentQueryCommand.getShipmentStatus().isEmpty()) {
                criteria = criteria.and(where("status").in(recoveredPlasmaShipmentQueryCommand.getShipmentStatus()));
            }

            if (nonNull(recoveredPlasmaShipmentQueryCommand.getShipmentDateFrom())) {
                var shipmentDate = where("shipmentDate");
                if (isNull(recoveredPlasmaShipmentQueryCommand.getShipmentDateTo())) {
                    criteria = criteria.and(
                        shipmentDate.greaterThanOrEquals(recoveredPlasmaShipmentQueryCommand.getShipmentDateFrom())
                            .or(shipmentDate.isNull())
                    );
                } else {
                    criteria = criteria.and(
                        shipmentDate.greaterThanOrEquals(recoveredPlasmaShipmentQueryCommand.getShipmentDateFrom())
                            .and(shipmentDate.lessThanOrEquals(recoveredPlasmaShipmentQueryCommand.getShipmentDateTo()))
                    );
                }
            }

            if (nonNull(recoveredPlasmaShipmentQueryCommand.getLocationCode()) && !recoveredPlasmaShipmentQueryCommand.getLocationCode().isEmpty()) {
                criteria = criteria.and(where("locationCode").in(recoveredPlasmaShipmentQueryCommand.getLocationCode()));
            }
            if (recoveredPlasmaShipmentQueryCommand.getTransportationReferenceNumber() != null ) {
                criteria = criteria.and(where("transportationReferenceNumber").is(recoveredPlasmaShipmentQueryCommand.getTransportationReferenceNumber()));
            }
        }

        log.debug("RecoveredPlasmaShipmentReport search criteria: {}", criteria);
        var count = this.count(RecoveredPlasmaShipmentReportEntity.class, entityTemplate, criteria);
        return this.filter(RecoveredPlasmaShipmentReportEntity.class, entityTemplate, criteria, recoveredPlasmaShipmentQueryCommand)
            .map(recoveredPlasmaShipmentReportEntityMapper::toModel)
            .collectList()
            .zipWith(count)
            .map(tuple -> this.buildPage(tuple.getT1(), tuple.getT2(), recoveredPlasmaShipmentQueryCommand));
    }
}
