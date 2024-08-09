package com.arcone.biopro.distribution.order.verification.controllers;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class OrderController {
    public Map<String, Integer> priorities = new LinkedHashMap<>();
    public Map<String, String> colors = new HashMap<>();

    // Constructor
    public OrderController() {
        this.priorities.put("STAT", 1);
        this.priorities.put("ASAP", 2);
        this.priorities.put("ROUTINE", 3);
        this.priorities.put("SCHEDULED", 4);
        this.priorities.put("DATE-TIME", 5);

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
            var priority = priorityList.toArray()[i].toString();
            log.info("Priority: {} found at position: {}", priority, i+1);


            if (this.priorities.get(priority) < lastPriorityIndex) {
                return false;
            }
            lastPriorityIndex = this.priorities.get(priority);
        }
        return true;
    }
}
