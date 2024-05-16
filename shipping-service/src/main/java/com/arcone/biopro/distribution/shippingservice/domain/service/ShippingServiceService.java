package com.arcone.biopro.distribution.shippingservice.domain.service;

import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShippingServiceRequestDTO;
import com.arcone.biopro.distribution.shippingservice.adapter.in.web.dto.ShippingServiceResponseDTO;
import reactor.core.publisher.Mono;

public interface ShippingServiceService {

    Mono<ShippingServiceResponseDTO> create(ShippingServiceRequestDTO request);

}
