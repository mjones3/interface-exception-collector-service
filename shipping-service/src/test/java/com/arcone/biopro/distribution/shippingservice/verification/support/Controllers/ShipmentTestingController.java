package com.arcone.biopro.distribution.shippingservice.verification.support.Controllers;

import com.arcone.biopro.distribution.shippingservice.verification.support.*;
import com.arcone.biopro.distribution.shippingservice.verification.support.Types.ListShipmentsResponseType;
import com.arcone.biopro.distribution.shippingservice.verification.support.Types.ShipmentRequestDetailsResponseType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.util.List;
import java.util.Random;


@Component
@Slf4j
public class ShipmentTestingController {

    @Autowired
    protected TestUtils utils;

    @Autowired
    protected ApiHelper apiHelper;

    @Autowired
    ObjectMapper objectMapper;

    public long createShippingRequest() throws Exception {
        long orderId = new Random().nextInt(10000);
        var resource = utils.getResource("order-fulfilled.json")
            .replace("{order.number}", String.valueOf(orderId));

        utils.kafkaSender(resource, Topics.ORDER_FULFILLED);
        // Add sleep to wait for the message to be consumed.
        Thread.sleep(2000);

        log.info("Message sent to create the order: {}", orderId);
        return orderId;
    }

    public EntityExchangeResult<String> listShipments() {
        log.info("Listing orders.");
        return apiHelper.getRequest(Endpoints.LIST_SHIPMENTS);
    }

    public EntityExchangeResult<String> getShipmentRequestDetails(long shipmentId) {
        var endpoint = Endpoints.GET_SHIPMENT.replace("{shipment.id}", String.valueOf(shipmentId));
        log.info("Getting order details for order: {}", shipmentId);
        return apiHelper.getRequest(endpoint);
    }

    public List<ListShipmentsResponseType> parseShipmentList(EntityExchangeResult<String> result) throws Exception {
        var object = List.of(objectMapper.readValue(result.getResponseBody(), ListShipmentsResponseType[].class));
        log.debug("Order list: {}", object);
        return object;
    }

    public ShipmentRequestDetailsResponseType parseShipmentRequestDetail(EntityExchangeResult<String> result) throws Exception {
        var object = objectMapper.readValue(result.getResponseBody(), ShipmentRequestDetailsResponseType.class);
        log.debug("Order details: {}", object);
        return object;
    }
}
