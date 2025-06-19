package com.arcone.biopro.distribution.shipping.adapter.in.web.controller;

import com.arcone.biopro.distribution.shipping.adapter.in.web.dto.RemoveProductResponseDTO;
import com.arcone.biopro.distribution.shipping.application.dto.RemoveItemRequest;
import com.arcone.biopro.distribution.shipping.application.dto.RuleResponseDTO;
import com.arcone.biopro.distribution.shipping.domain.service.RemoveShipmentItemService;
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
public class RemoveItemController {

    private final RemoveShipmentItemService removeShipmentItemService;

    @QueryMapping("getNotificationDetailsByShipmentId")
    public Mono<RemoveProductResponseDTO> getNotificationDetailsByShipmentId(@Argument("shipmentId") long shipmentId) {
        log.info("Requesting Notification Details for shipment {}.....",shipmentId);
        return removeShipmentItemService.getNotificationDetailsByShipmentId(shipmentId);
    }

    @MutationMapping("removeItem")
    public Mono<RuleResponseDTO> removeItem(@Argument("removeItemRequest") RemoveItemRequest removeItemRequest) {
        log.info("Request to remove a product {}", removeItemRequest);
        return removeShipmentItemService.removeItem(removeItemRequest);
    }

}
