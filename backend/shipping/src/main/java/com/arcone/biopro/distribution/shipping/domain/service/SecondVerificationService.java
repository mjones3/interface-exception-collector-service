package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.VerifyProductResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.VerifyItemRequest;
import reactor.core.publisher.Mono;

public interface SecondVerificationService {

    Mono<RuleResponseDTO> verifyItem(VerifyItemRequest verifyItemRequest);
    Mono<VerifyProductResponseDTO> getVerificationDetailsByShipmentId(Long shipmentId);
}
