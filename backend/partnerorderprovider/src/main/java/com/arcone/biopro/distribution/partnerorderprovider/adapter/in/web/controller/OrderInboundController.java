package com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.controller;

import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.controller.util.HeaderUtil;
import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.OrderInboundDTO;
import com.arcone.biopro.distribution.partnerorderprovider.adapter.in.web.dto.OrderInboundResponseDTO;
import com.arcone.biopro.distribution.partnerorderprovider.application.validation.JsonValidationFailedException;
import com.arcone.biopro.distribution.partnerorderprovider.application.validation.ValidJson;
import com.arcone.biopro.distribution.partnerorderprovider.domain.service.OrderInboundService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class OrderInboundController {

    private final OrderInboundService orderInboundService;

    private static final String ORDER_INBOUND_DATA = "interface-schema/Order-Management-Inbound-1.0.0.json";

    public static final String ORDER_DATA_INBOUND_STATUS_CREATE = "CREATED";

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
    @PostMapping("/v1/orders")
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
            ? ResponseEntity.created(URI.create("/v1/orders/" + resultDto.id()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, "ENTITY_NAME", resultDto.id()))
            .body(resultDto)
            : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(resultDto);
    }

    @ExceptionHandler(JsonValidationFailedException.class)
    public ResponseEntity<Map<String, Object>> onJsonValidationFailedException(JsonValidationFailedException ex) {
        return ResponseEntity.badRequest().body(Map.of("errors", ex.getErrorsList()));
    }

}
