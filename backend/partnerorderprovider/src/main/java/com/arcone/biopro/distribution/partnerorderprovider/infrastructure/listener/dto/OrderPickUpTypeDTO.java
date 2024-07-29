package com.arcone.biopro.distribution.partnerorderprovider.infrastructure.listener.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record OrderPickUpTypeDTO (
    boolean willCallPickUp,
    String phoneNumber
) implements Serializable {


}
