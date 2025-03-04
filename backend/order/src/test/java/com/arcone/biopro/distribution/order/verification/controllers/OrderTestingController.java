package com.arcone.biopro.distribution.order.verification.controllers;

import com.arcone.biopro.distribution.order.application.dto.CancelOrderReceivedDTO;
import com.arcone.biopro.distribution.order.verification.support.*;
import com.arcone.biopro.distribution.order.verification.support.graphql.GraphQLMutationMapper;
import com.arcone.biopro.distribution.order.verification.support.graphql.GraphQLQueryMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class OrderTestingController {
    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    private SharedContext context;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private KafkaHelper kafkaHelper;

    public Map<String, Integer> priorities = new LinkedHashMap<>();
    public Map<String, String> colors = new HashMap<>();

    // Constructor
    public OrderTestingController() {
        this.priorities.put("STAT", 1);
        this.priorities.put("ASAP", 2);
        this.priorities.put("ROUTINE", 3);
        this.priorities.put("SCHEDULED", 4);
        this.priorities.put("DATE_TIME", 5);

        this.colors.put("RED", "#ff3333");
        this.colors.put("GREY", "#d7d6d3");
        this.colors.put("ORANGE", "#ffb833");
        this.colors.put("BLUE", "#0930f6");
        this.colors.put("VIOLET", "#97a6f2");
        this.colors.put("GREEN", "#00ff00");
        this.colors.put("YELLOW", "#ffff00");
        this.colors.put("WHITE", "#ffffff");
    }


    public Map.Entry<String, Integer> getRandomPriority() {
        Random random = new Random();
        int randomIndex = random.nextInt(priorities.size());
        return priorities.entrySet().stream().skip(randomIndex).findFirst().orElse(null);
    }

    public Integer getPriorityValue(String priority) {
        return priorities.get(priority);
    }

    public String getColorHex(String colorName) {
        return colors.get(colorName.toUpperCase());
    }

    /**
     * Checks if the given set of priorities is ordered according to the predefined priority values.
     *
     * @param priorityList a set of priority strings to be checked
     * @return true if the priorities are in the correct order, false otherwise
     */
    public boolean isPriorityOrdered(Set<String> priorityList) {
        var lastPriorityIndex = 1;
        for (int i = 0; i < priorityList.size(); i++) {
            var priority = priorityList.toArray()[i].toString().replace("-", "_");
            log.info("Priority: {} found at position: {}", priority, i+1);


            if (this.priorities.get(priority) < lastPriorityIndex) {
                return false;
            }
            lastPriorityIndex = this.priorities.get(priority);
        }
        return true;
    }

    public void listOrdersByExternalId() {
        var response = apiHelper.graphQlPageRequest(GraphQLQueryMapper.listOrdersByUniqueIdentifier(context.getLocationCode(),context.getExternalId()), "searchOrders");
        context.setOrdersPage(response);
    }

    public void listOrdersByOrderId() {
        var response = apiHelper.graphQlPageRequest(GraphQLQueryMapper.listOrdersByUniqueIdentifier(context.getLocationCode(),context.getOrderId().toString()), "searchOrders");
        context.setOrdersPage(response);
    }

    public void cancelOrder(String externalId, String cancelDate, String payload) throws Exception {
        var jsonContent = testUtils.getResource(payload).replace("{EXTERNAL_ID}", externalId).replace("{EVENT_ID}", UUID.randomUUID().toString()).replace("{CANCEL_DATE}", cancelDate);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        var eventPayload = objectMapper.readValue(jsonContent, CancelOrderReceivedDTO.class);
        var event = kafkaHelper.sendEvent(eventPayload.eventId().toString(), eventPayload, Topics.CANCEL_ORDER_RECEIVED).block();
        Assert.assertNotNull(event);
    }

    @Getter
    @RequiredArgsConstructor
    public
    enum OrderStatusMap {
        ALL("All"),
        OPEN("Open"),
        CREATED("Created"),
        SHIPPED("Shipped"),
        IN_PROGRESS("In Progress"),
        ;

        private final String description;
    }

    public Map completeOrder(Integer orderId, boolean createBackOrder) {
        String employeeId = context.getEmployeeId();
        var response =  apiHelper.graphQlRequest(GraphQLMutationMapper.completeOrderMutation(orderId, employeeId, "Order completed comment", createBackOrder), "completeOrder");
        log.debug("Order completed response: {}", response);
        return response;
    }

    public void getOrderDetails(Integer orderId) {
        var response =  apiHelper.graphQlRequest(GraphQLQueryMapper.findOrderById(orderId), "findOrderById");
        context.setOrderDetails((Map) response.get("data"));
        log.debug("Order details: {}", context.getOrderDetails());
    }

}
