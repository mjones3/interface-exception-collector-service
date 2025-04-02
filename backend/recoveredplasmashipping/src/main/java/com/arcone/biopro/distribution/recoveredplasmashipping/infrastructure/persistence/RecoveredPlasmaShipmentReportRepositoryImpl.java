package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Page;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentQueryCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipmentReport;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentReportRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.RecoveredPlasmaShipmentReportEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static org.springframework.data.relational.core.query.Criteria.where;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RecoveredPlasmaShipmentReportRepositoryImpl implements RecoveredPlasmaShipmentReportRepository , FilterAndSortRepository<RecoveredPlasmaShipmentReportEntity> {

    private final R2dbcEntityTemplate entityTemplate;
    private final RecoveredPlasmaShipmentReportEntityMapper recoveredPlasmaShipmentReportEntityMapper;

    @Override
    public Mono<Page<RecoveredPlasmaShipmentReport>> search(RecoveredPlasmaShipmentQueryCommand recoveredPlasmaShipmentQueryCommand) {
        var criteria = where("id").isNotNull();

        if (Objects.nonNull(recoveredPlasmaShipmentQueryCommand.getLocationCode()) && !recoveredPlasmaShipmentQueryCommand.getLocationCode().isEmpty()) {
            criteria = criteria.and(where("locationCode").in(recoveredPlasmaShipmentQueryCommand.getLocationCode()));
        }

        if (recoveredPlasmaShipmentQueryCommand.getShipmentNumber() != null ) {
            criteria = criteria.and(where("shipmentNumber").is(recoveredPlasmaShipmentQueryCommand.getShipmentNumber()));
        }

        if (Objects.nonNull(recoveredPlasmaShipmentQueryCommand.getShipmentStatus()) && !recoveredPlasmaShipmentQueryCommand.getShipmentStatus().isEmpty()) {
            criteria = criteria.and(where("status").in(recoveredPlasmaShipmentQueryCommand.getShipmentStatus()));
        }

        if (Objects.nonNull(recoveredPlasmaShipmentQueryCommand.getCustomers()) && !recoveredPlasmaShipmentQueryCommand.getCustomers().isEmpty()) {
            criteria = criteria.and(where("customerCode").in(recoveredPlasmaShipmentQueryCommand.getCustomers()));
        }

        if (Objects.nonNull(recoveredPlasmaShipmentQueryCommand.getProductTypes()) && !recoveredPlasmaShipmentQueryCommand.getProductTypes().isEmpty()) {
            criteria = criteria.and(where("productType").in(recoveredPlasmaShipmentQueryCommand.getProductTypes()));
        }

        if (Objects.nonNull(recoveredPlasmaShipmentQueryCommand.getShipmentDateFrom()) && Objects.nonNull(recoveredPlasmaShipmentQueryCommand.getShipmentDateTo())) {
            criteria = criteria.and(
                where("shipmentDate").greaterThanOrEquals(recoveredPlasmaShipmentQueryCommand.getShipmentDateFrom())
                    .and("shipmentDate").lessThanOrEquals(recoveredPlasmaShipmentQueryCommand.getShipmentDateTo())
            );
        }

        var count = this.count(RecoveredPlasmaShipmentReportEntity.class, entityTemplate, criteria);

        return this.filter(RecoveredPlasmaShipmentReportEntity.class, entityTemplate, criteria, recoveredPlasmaShipmentQueryCommand)
            .map(recoveredPlasmaShipmentReportEntityMapper::toModel)
                .collectList()
                    .zipWith(count)
            .map(tuple -> this.buildPage(tuple.getT1(), tuple.getT2(), recoveredPlasmaShipmentQueryCommand));




    }
}
