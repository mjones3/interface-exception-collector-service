package com.arcone.biopro.distribution.shippingservice.verification.support.Controllers;

import com.arcone.biopro.distribution.shippingservice.verification.support.ApiHelper;
import com.arcone.biopro.distribution.shippingservice.verification.support.Endpoints;
import com.arcone.biopro.distribution.shippingservice.verification.support.TestUtils;
import com.arcone.biopro.distribution.shippingservice.verification.support.Topics;
import com.arcone.biopro.distribution.shippingservice.verification.support.Types.ListOrdersResponseType;
import com.arcone.biopro.distribution.shippingservice.verification.support.Types.OrderDetailsResponseType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.util.List;
import java.util.Random;


@Component
@Slf4j
public class OrderTestingController {

    @Autowired
    protected TestUtils utils;

    @Autowired
    protected ApiHelper apiHelper;

    @Autowired
    ObjectMapper objectMapper;

    public long createOrderRequest() throws Exception {
        long orderId = new Random().nextInt(10000);
        var resource = utils.getResource("order-fulfilled.json")
            .replace("{order.number}", String.valueOf(orderId));

        utils.kafkaSender(resource, Topics.ORDER_FULFILLED);
        // Add sleep to wait for the message to be consumed.
        Thread.sleep(2000);

        log.info("Message sent to create the order: {}", orderId);
        return orderId;
    }

    public EntityExchangeResult<String> listOrders() {
        log.info("Listing orders.");
        return apiHelper.getRequest(Endpoints.LIST_SHIPMENTS);
    }

    public EntityExchangeResult<String> getOrderDetails(long orderNumber) {
        var endpoint = Endpoints.GET_SHIPMENT.replace("{shipment.id}", String.valueOf(orderNumber));
        log.info("Getting order details for order: {}", orderNumber);
        return apiHelper.getRequest(endpoint);
    }

    public List<ListOrdersResponseType> parseOrderList(EntityExchangeResult<String> result) throws Exception {
        var object = List.of(objectMapper.readValue(result.getResponseBody(), ListOrdersResponseType[].class));
        log.debug("Order list: {}", object);
        return object;
    }

    public OrderDetailsResponseType parseOrderDetail(EntityExchangeResult<String> result) throws Exception {
        var object = objectMapper.readValue(result.getResponseBody(), OrderDetailsResponseType.class);
        log.debug("Order details: {}", object);
        return object;
    }
}
