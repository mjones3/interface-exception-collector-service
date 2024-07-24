package com.arcone.biopro.distribution.partnerorderproviderservice.infrastructure.listener.dto;


import lombok.Builder;

import java.io.Serializable;

@Builder
public record OrderItemDTO (
    String productFamily,
    String bloodType,
    Integer quantity,
    String comments

) implements Serializable {


}
