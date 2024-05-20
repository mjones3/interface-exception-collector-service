package com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto;


import java.io.Serializable;

public record OrderItemFulfilledMessage(
    Long id,

    Long orderId,
    String productFamily,

    String bloodType,
    Integer quantity,
    String comments
) implements Serializable {}
