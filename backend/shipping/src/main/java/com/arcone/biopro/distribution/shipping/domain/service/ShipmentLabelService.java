package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.application.dto.PackingListLabelDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShippingLabelDTO;
import reactor.core.publisher.Mono;

public interface ShipmentLabelService {
    Mono<PackingListLabelDTO> generatePackingListLabel(Long shipmentId);
    Mono<ShippingLabelDTO> generateShippingLabel(Long shipmentId);

}
