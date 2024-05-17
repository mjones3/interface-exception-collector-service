package com.arcone.biopro.distribution.shippingservice.verification.support;

public interface Endpoints {


    String CHECK_HEALTH = "/management/health";

    // Order endpoints
    String LIST_ORDER = "/v1/orders";
    String GET_ORDER = "/v1/orders/{id}";

    // Shipping endpoints
    String LIST_SHIPPING = "/v1/shipping";
}
