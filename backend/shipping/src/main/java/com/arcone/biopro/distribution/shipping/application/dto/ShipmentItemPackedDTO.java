package com.arcone.biopro.distribution.shipping.application.dto;

import com.arcone.biopro.distribution.shipping.domain.model.enumeration.IneligibleStatus;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.SecondVerification;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.VisualInspection;
import lombok.Builder;
import org.springframework.data.relational.core.mapping.Column;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Builder
public record ShipmentItemPackedDTO(
    Long id,
    Long shipmentItemId,
    Integer inventoryId,
    String unitNumber,
    String productCode,
    String aboRh,
    String productDescription,
    String productFamily,
    LocalDateTime expirationDate,
    ZonedDateTime collectionDate,
    String packedByEmployeeId,
    VisualInspection visualInspection,
    SecondVerification secondVerification,
    String verifiedByEmployeeId,
    ZonedDateTime verifiedDate,
    String ineligibleStatus,
    String ineligibleAction,
    String ineligibleReason,
    String ineligibleMessage
) implements Serializable {

}
