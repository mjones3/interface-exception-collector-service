package com.arcone.biopro.distribution.shippingservice.domain.service;

import com.arcone.biopro.distribution.shippingservice.application.dto.PackingListLabelDTO;
import reactor.core.publisher.Mono;

public interface ShipmentLabelService {
    Mono<PackingListLabelDTO> generatePackingListLabel(Long shipmentId);

}
