package com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.dto.ProductTypeDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.adapter.in.web.mapper.RecoveredPlasmaShipmentCriteriaDtoMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.RecoveredPlasmaShipmentCriteriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@Slf4j
@RequiredArgsConstructor
public class RecoveredPlasmaShipmentCriteriaController {

    private final RecoveredPlasmaShipmentCriteriaService recoveredPlasmaShipmentCriteriaService;
    private final RecoveredPlasmaShipmentCriteriaDtoMapper recoveredPlasmaShipmentCriteriaDtoMapper;

    @QueryMapping("findAllProductTypeByCustomer")
    public Flux<ProductTypeDTO> findAllProductTypeByCustomer(@Argument("customerCode") String customerCode) {
        log.debug("Request to find all Product Type by Customer: {}", customerCode);
        return recoveredPlasmaShipmentCriteriaService.findAllProductTypeByCustomer(customerCode).map(recoveredPlasmaShipmentCriteriaDtoMapper::toDto);
    }

}
