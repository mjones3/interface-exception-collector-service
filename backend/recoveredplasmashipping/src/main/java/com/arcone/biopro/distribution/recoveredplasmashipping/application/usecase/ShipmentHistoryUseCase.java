package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ShipmentHistoryOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.ShipmentHistoryOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShippingRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.ShipmentHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentHistoryUseCase implements ShipmentHistoryService {
    private final RecoveredPlasmaShippingRepository recoveredPlasmaShippingRepository;
    private final ShipmentHistoryOutputMapper shipmentHistoryOutputMapper;


    @Override
    public Flux<ShipmentHistoryOutput> findAllByShipmentId(Long shipmentId) {
        return recoveredPlasmaShippingRepository.findAllByShipmentId(shipmentId)
            .switchIfEmpty(Mono.error(NoResultsFoundException::new))
            .map(shipmentHistoryOutputMapper::toOutput);
    }
}
