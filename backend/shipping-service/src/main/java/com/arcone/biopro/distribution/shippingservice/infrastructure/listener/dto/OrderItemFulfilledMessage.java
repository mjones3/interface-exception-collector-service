package com.arcone.biopro.distribution.shippingservice.infrastructure.listener.dto;


import java.io.Serializable;
import java.util.List;

public record OrderItemFulfilledMessage(
    Long id,

    Long orderId,
    String productFamily,

    String bloodType,
    Integer quantity,
    String comments,
    Integer totalAvailable,
    List<ShortDateItem> shortDateProducts

) implements Serializable {}
