package com.arcone.biopro.distribution.shipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.VerifyProductResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.VerifyItemRequest;
import com.arcone.biopro.distribution.shipping.domain.service.SecondVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SecondVerificationController {

    private final SecondVerificationService secondVerificationService;

    @QueryMapping("getVerificationDetailsById")
    public Mono<VerifyProductResponseDTO> getShipmentVerificationDetailsById(@Argument("shipmentId") long shipmentId) {
        log.info("Requesting Verification Details for shipment {}.....",shipmentId);
        return secondVerificationService.getVerificationDetailsByShipmentId(shipmentId);
    }

    @MutationMapping("verifyItem")
    public Mono<RuleResponseDTO> verifyItem(@Argument("verifyItemRequest") VerifyItemRequest verifyItemRequest) {
        log.info("Request to verify a product {}", verifyItemRequest);
        return secondVerificationService.verifyItem(verifyItemRequest);
    }

}
