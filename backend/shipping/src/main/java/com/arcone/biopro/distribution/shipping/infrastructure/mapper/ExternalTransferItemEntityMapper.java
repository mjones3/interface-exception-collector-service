package com.arcone.biopro.distribution.shipping.infrastructure.mapper;

import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransferItem;
import com.arcone.biopro.distribution.shipping.infrastructure.persistence.ExternalTransferItemEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExternalTransferItemEntityMapper {

    public ExternalTransferItemEntity toEntity(ExternalTransferItem externalTransferItem) {
        return ExternalTransferItemEntity
            .builder()
            .id(externalTransferItem.getId())
            .externalTransferId(externalTransferItem.getExternalTransferId())
            .productCode(externalTransferItem.getProduct().getProductCode())
            .unitNumber(externalTransferItem.getProduct().getUnitNumber())
            .productFamily(externalTransferItem.getProduct().getProductFamily())
            .createdByEmployeeId(externalTransferItem.getCreatedByEmployeeId())
            .build();

    }
}
