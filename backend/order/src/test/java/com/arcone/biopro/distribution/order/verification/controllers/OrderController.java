package com.arcone.biopro.distribution.order.verification.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OrderController {
    Map<String, Integer> priorities = new HashMap<>();
    Map<String, String> colors = new HashMap<>();

    // Constructor
    public OrderController() {
        this.priorities.put("STAT", 1);
        this.priorities.put("ASAP", 2);
        this.priorities.put("ROUTINE", 3);
        this.priorities.put("SCHEDULED", 4);
        this.priorities.put("DATE - TIME", 5);

        this.colors.put("RED", "#FF0000");
        this.colors.put("YELLOW", "#FFFF00");
        this.colors.put("GREEN", "#008000");
        this.colors.put("ORANGE", "#FFA500");
        this.colors.put("BLUE", "#0000FF");
        this.colors.put("PINK", "#FFC0CB");
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
}
