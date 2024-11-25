package com.arcone.biopro.distribution.shipping.adapter.in.web.dto;

import com.arcone.biopro.distribution.shipping.application.dto.ShipmentItemPackedDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShipmentItemRemovedDTO;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record RemoveProductResponseDTO(
    Long shipmentId,
    List<ShipmentItemRemovedDTO> removedItems,
    List<ShipmentItemPackedDTO> toBeRemovedItems,
    ShipmentItemPackedDTO removedItem

) implements Serializable {

}
