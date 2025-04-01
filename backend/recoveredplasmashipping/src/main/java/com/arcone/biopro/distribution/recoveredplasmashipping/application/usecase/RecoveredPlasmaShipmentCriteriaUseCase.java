package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ProductTypeOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.RecoveredPlasmaShipmentCriteriaOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.RecoveredPlasmaShipmentCriteriaRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.RecoveredPlasmaShipmentCriteriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoveredPlasmaShipmentCriteriaUseCase implements RecoveredPlasmaShipmentCriteriaService {

    private final RecoveredPlasmaShipmentCriteriaRepository recoveredPlasmaShipmentCriteriaRepository;
    private final RecoveredPlasmaShipmentCriteriaOutputMapper recoveredPlasmaShipmentCriteriaMapper;


    @Override
    public Flux<ProductTypeOutput> findAllProductTypeByCustomer(String customerCode) {
        return recoveredPlasmaShipmentCriteriaRepository.findAllProductTypeByByCustomerCode(customerCode)
            .switchIfEmpty(Flux.error(NoResultsFoundException::new)).map(recoveredPlasmaShipmentCriteriaMapper::toOutput);

    }
}
