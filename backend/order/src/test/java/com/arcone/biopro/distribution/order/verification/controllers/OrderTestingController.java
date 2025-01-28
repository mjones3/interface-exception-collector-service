package com.arcone.biopro.distribution.order.verification.controllers;

import com.arcone.biopro.distribution.order.verification.support.ApiHelper;
import com.arcone.biopro.distribution.order.verification.support.SharedContext;
import com.arcone.biopro.distribution.order.verification.support.graphql.GraphQLMutationMapper;
import com.arcone.biopro.distribution.order.verification.support.graphql.GraphQLQueryMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Slf4j
@Component
public class OrderTestingController {
    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    private SharedContext context;

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
        return apiHelper.graphQlRequest(GraphQLMutationMapper.completeOrderMutation(orderId, employeeId, "Order completed comment", createBackOrder), "completeOrder");
    }

    public void getOrderDetails(Integer orderId) {
        var response =  apiHelper.graphQlRequest(GraphQLQueryMapper.findOrderById(orderId), "findOrderById");
        context.setOrderDetails((Map) response.get("data"));
    }

}
