package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import com.arcone.biopro.distribution.shipping.application.dto.ShipmentItemPackedDTO;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record VerifyProductResponseDTO(
    Long shipmentId,
    List<ShipmentItemPackedDTO> packedItems,
    List<ShipmentItemPackedDTO> verifiedItems
) implements Serializable {

}
