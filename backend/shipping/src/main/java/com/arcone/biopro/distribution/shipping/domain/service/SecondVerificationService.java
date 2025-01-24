package com.arcone.biopro.distribution.shipping.domain.service;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.VerifyProductResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.VerifyItemRequest;
import com.arcone.biopro.distribution.shipping.domain.model.ShipmentItemPacked;
import reactor.core.publisher.Mono;

public interface SecondVerificationService {

    Mono<RuleResponseDTO> verifyItem(VerifyItemRequest verifyItemRequest);
    Mono<VerifyProductResponseDTO> getVerificationDetailsByShipmentId(Long shipmentId);
    Mono<ShipmentItemPacked> resetVerification(Long shipmentId , String rootCause);
    Mono<ShipmentItemPacked> markAsVerificationPending(ShipmentItemPacked itemPacked);
}
