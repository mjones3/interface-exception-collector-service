package com.arcone.biopro.distribution.shippingservice.verification.support;

public interface Endpoints {


    String CHECK_HEALTH = "/management/health";

    // Order endpoints
    String LIST_SHIPMENTS = "/v1/shipments";
    String GET_SHIPMENT = "/v1/shipments/{shipment.id}";

    // Shipping endpoints
    String LIST_SHIPPING = "/v1/shipping";
}
