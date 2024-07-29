package com.arcone.biopro.distribution.shipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.shipping.application.dto.PackingListLabelDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShippingLabelDTO;
import com.arcone.biopro.distribution.shipping.domain.service.ShipmentLabelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ShipmentLabelController {

    private final ShipmentLabelService shipmentLabelService;

    @QueryMapping("generatePackingListLabel")
    public Mono<PackingListLabelDTO> generatePackingListLabel(@Argument("shipmentId") long shipmentId){
        return shipmentLabelService.generatePackingListLabel(shipmentId);
    }
    @QueryMapping("generateShippingLabel")
    public Mono<ShippingLabelDTO> generateShippingLabel(@Argument("shipmentId") long shipmentId){
        return shipmentLabelService.generateShippingLabel(shipmentId);
    }
}
