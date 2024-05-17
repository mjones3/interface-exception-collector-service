package com.arcone.biopro.distribution.shippingservice.verification.support;

import org.springframework.stereotype.Component;

@Component
public final class Endpoints {

    private Endpoints() {
        // Private constructor to prevent instantiation
    }

    public static final String LIST_ORDER = "/v1/orders";
    public static final String GET_ORDER = "/v1/orders/{id}";
    // Add more endpoints as needed
}
