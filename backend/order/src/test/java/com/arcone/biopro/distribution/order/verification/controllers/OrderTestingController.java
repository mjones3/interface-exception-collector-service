package com.arcone.biopro.distribution.order.verification.controllers;

import com.arcone.biopro.distribution.order.application.dto.CancelOrderReceivedDTO;
import com.arcone.biopro.distribution.order.application.dto.ModifyOrderReceivedDTO;
import com.arcone.biopro.distribution.order.application.dto.OrderReceivedEventDTO;
import com.arcone.biopro.distribution.order.verification.support.ApiHelper;
import com.arcone.biopro.distribution.order.verification.support.KafkaHelper;
import com.arcone.biopro.distribution.order.verification.support.SharedContext;
import com.arcone.biopro.distribution.order.verification.support.TestUtils;
import com.arcone.biopro.distribution.order.verification.support.Topics;
import com.arcone.biopro.distribution.order.verification.support.graphql.GraphQLMutationMapper;
import com.arcone.biopro.distribution.order.verification.support.graphql.GraphQLQueryMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

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
            log.info("Priority: {} found at position: {}", priority, i + 1);


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

    public void listOrdersByPage(Integer page) {
        var response = apiHelper.graphQlPageRequest(GraphQLQueryMapper.listOrdersByPage(context.getLocationCode(),page), "searchOrders");
        context.setOrdersPage(response);
    }

    public void sortOrdersByPage(Integer page , String sortingColumn , String sortingOrder) {
        var response = apiHelper.graphQlPageRequest(GraphQLQueryMapper.sortOrdersByPage(context.getLocationCode(),page, sortingColumn, sortingOrder), "searchOrders");
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
        var response = apiHelper.graphQlRequest(GraphQLMutationMapper.completeOrderMutation(orderId, employeeId, "Order completed comment", createBackOrder), "completeOrder");
        log.debug("Order completed response: {}", response);
        return response;
    }

    public Map getOrderDetailsMap(Integer orderId) {
        var response = apiHelper.graphQlRequest(GraphQLQueryMapper.findOrderById(orderId), "findOrderById");
        log.debug("Order details: {}", context.getOrderDetails());
        return (Map) response.get("data");
    }

    public void getOrderDetails(Integer orderId) {
        var response = apiHelper.graphQlRequest(GraphQLQueryMapper.findOrderById(orderId), "findOrderById");
        context.setOrderDetails((Map) response.get("data"));
        log.debug("Order details: {}", context.getOrderDetails());
    }

    public void createOrderInboundRequest(String jsonContent, OrderReceivedEventDTO eventPayload) throws JSONException {
        context.setPartnerCreateOrder(new JSONObject(jsonContent));
        log.info("JSON PAYLOAD :{}", context.getPartnerCreateOrder());
        Assert.assertNotNull(context.getExternalId());
        Assert.assertNotNull(context.getPartnerCreateOrder());
        var event = kafkaHelper.sendEvent(eventPayload.payload().id().toString(), eventPayload, Topics.ORDER_RECEIVED).block();
        Assert.assertNotNull(event);
    }

    public void modifyOrderRequest(
        String externalId,
        String locationCode,
        String employeeModificationCode,
        String deliveryType,
        String shippingMethod,
        String productCategory,
        String modifyReason,
        String modifyDate,
        String[] productFamilyList,
        String[] bloodTypeList,
        String[] quantityList,
        String modifyPayload,
        String modifyItemsPayload) throws Exception {

        // Prepare items string object
        StringBuilder orderItems = new StringBuilder();
        for (var j = 0; j < productFamilyList.length; j++) {
            if (j > 0) {
                orderItems.append(",");
            }
            var ordeItemJson = testUtils.getResource(modifyItemsPayload);
            ordeItemJson = ordeItemJson.replace("{PRODUCT_FAMILY}", productFamilyList[j])
                .replace("{BLOOD_TYPE}", bloodTypeList[j])
                .replace("{QUANTITY}", quantityList[j])
                .replace("{COMMENTS}", "Comments");
            orderItems.append(ordeItemJson);
        }

        // Prepare request JSON
        var jsonContent = testUtils.getResource(modifyPayload);
        jsonContent = jsonContent.replace("{EXTERNAL_ID}", externalId)
            .replace("{LOCATION_CODE}", locationCode)
            .replace("{MODIFY_EMPLOYEE_CODE}", employeeModificationCode)
            .replace("{DELIVERY_TYPE}", deliveryType)
            .replace("{SHIPPING_METHOD}", shippingMethod)
            .replace("{PRODUCT_CATEGORY}", productCategory)
            .replace("{MODIFY_REASON}", modifyReason)
            .replace("{MODIFY_DATE}", modifyDate)
            .replace("{ORDER_ITEMS}", orderItems.toString())
            .replace("{EVENT_ID}", UUID.randomUUID().toString());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        var eventPayload = objectMapper.readValue(jsonContent, ModifyOrderReceivedDTO.class);

        var event = kafkaHelper.sendEvent(eventPayload.eventId().toString(), eventPayload, Topics.MODIFY_ORDER_RECEIVED).block();
        Assert.assertNotNull(event);
    }

}
