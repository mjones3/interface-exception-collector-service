package com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.controller;

import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.controller.util.HeaderUtil;
import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.CancelOrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.OrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.OrderInboundResponseDTO;
import com.arcone.biopro.distribution.partnerorderprovider.application.validation.JsonValidationFailedException;
import com.arcone.biopro.distribution.partnerorderprovider.application.validation.ValidJson;
import com.arcone.biopro.distribution.partnerorderprovider.domain.service.CancelOrderInboundService;
import com.arcone.biopro.distribution.partnerorderprovider.domain.service.OrderInboundService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderInboundController {

    private final OrderInboundService orderInboundService;
    private final CancelOrderInboundService cancelOrderInboundService;

    private static final String ORDER_INBOUND_DATA = "interface-schema/Order-Management-Inbound-1.0.0.json";
    private static final String CANCEL_ORDER_INBOUND_DATA = "interface-schema/Cancel-Order-Inbound-1.0.0.json";

    public static final String ORDER_DATA_INBOUND_STATUS_CREATE = "CREATED";
    public static final String ORDER_DATA_INBOUND_STATUS_ACCEPTED = "ACCEPTED";

    @Value("${application.clientApp.name}")
    private String applicationName;

    /**
     * {@code POST  /v1/orders} : Create an Order.
     *
     * @param orderInboundDTO  the Order Data DTO.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new OrderInboundDTO,
     * or with status {@code 400 (Bad Request)} if there is any broken validation.
     */
    @Operation(summary = "Creates a Order Data")
    @PostMapping("/v1/partner-order-provider/orders")
    public ResponseEntity<OrderInboundResponseDTO> createOrderInboundData(
        @ValidJson(ORDER_INBOUND_DATA) OrderInboundDTO orderInboundDTO) {

        log.debug("REST request to save Order Data : {}", orderInboundDTO);
        var result = orderInboundService.receiveOrderInbound(orderInboundDTO);
        log.info("Order Data Response: {}", result);

        var resultDto = OrderInboundResponseDTO.builder()
            .id(result.id().toString())
            .status(result.status())
            .timestamp(result.timestamp())
            .build();

        return resultDto.status().equals(ORDER_DATA_INBOUND_STATUS_CREATE)
            ? ResponseEntity.created(URI.create("/v1/partner-order-provider/orders/" + resultDto.id()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, "ENTITY_NAME", resultDto.id()))
            .body(resultDto)
            : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(resultDto);
    }

    /**
     * {@code PATCH  /v1/orders/{externalId}/cancel} : Cancel an Order.
     *
     * @param cancelOrderInboundDTO  the Cancel Order Data DTO.
     * @return the {@link ResponseEntity} with status {@code 202 (Accepted)} and with body the CancelOrderInboundDTO,
     * or with status {@code 400 (Bad Request)} if there is any broken validation.
     */
    @Operation(summary = "Cancel Order Data")
    @PatchMapping("/v1/partner-order-provider/orders/{externalId}/cancel")
    public ResponseEntity<OrderInboundResponseDTO> cancelOrderInbound(@PathVariable String externalId ,
        @ValidJson(CANCEL_ORDER_INBOUND_DATA) CancelOrderInboundDTO cancelOrderInboundDTO) {

        log.debug("REST request to cancel Order ID : {} {}" , externalId, cancelOrderInboundDTO);
        var result = cancelOrderInboundService.receiveCancelOrderInbound(cancelOrderInboundDTO);
        log.debug("Cancel Order Data Response: {}", result);

        var resultDto = OrderInboundResponseDTO.builder()
            .id(result.id().toString())
            .status(result.status())
            .timestamp(result.timestamp())
            .build();

        return resultDto.status().equals(ORDER_DATA_INBOUND_STATUS_ACCEPTED)
            ? ResponseEntity.accepted()
            .body(resultDto)
            : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(resultDto);
    }

    @ExceptionHandler(JsonValidationFailedException.class)
    public ResponseEntity<Map<String, Object>> onJsonValidationFailedException(JsonValidationFailedException ex) {
        return ResponseEntity.badRequest().body(Map.of("errors", ex.getErrorsList()));
    }

}
