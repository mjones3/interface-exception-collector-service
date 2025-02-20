package com.arcone.biopro.distribution.shipping.infrastructure.mapper;

import com.arcone.biopro.distribution.shipping.application.dto.ShipmentCompletedItemProductPayloadDTO;
import com.arcone.biopro.distribution.shipping.application.dto.ShipmentCompletedPayloadDTO;
import com.arcone.biopro.distribution.shipping.domain.model.ProductLocationHistory;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ProductLocationHistoryType;
import com.arcone.biopro.distribution.shipping.domain.service.CustomerService;
import com.arcone.biopro.distribution.shipping.infrastructure.persistence.ProductLocationHistoryEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductLocationHistoryMapper {

    private final CustomerService customerService;

    public ProductLocationHistory toDomain(ProductLocationHistoryEntity productLocationHistoryEntity) {
        return new ProductLocationHistory(productLocationHistoryEntity.getId(), productLocationHistoryEntity.getCustomerCodeTo()
            , productLocationHistoryEntity.getCustomerNameTo(), productLocationHistoryEntity.getCustomerCodeFrom()
            , productLocationHistoryEntity.getCustomerNameFrom(), productLocationHistoryEntity.getType()
            , productLocationHistoryEntity.getUnitNumber(), productLocationHistoryEntity.getProductCode()
            , productLocationHistoryEntity.getCreatedByEmployeeId(),productLocationHistoryEntity.getCreateDate() , customerService);
    }

    public ProductLocationHistoryEntity toEntity(ProductLocationHistory productLocationHistory) {
        return ProductLocationHistoryEntity.builder()
            .createdByEmployeeId(productLocationHistory.getCreatedByEmployeeId())
            .type(productLocationHistory.getType())
            .customerCodeTo(productLocationHistory.getCustomerTo().getCode())
            .customerNameTo(productLocationHistory.getCustomerTo().getName())
            .customerCodeFrom(productLocationHistory.getCustomerFrom().getCode())
            .customerNameFrom(productLocationHistory.getCustomerFrom().getName())
            .unitNumber(productLocationHistory.getProduct().getUnitNumber())
            .productCode(productLocationHistory.getProduct().getProductCode())
            .build();
    }

    public ProductLocationHistoryEntity toEntity(ShipmentCompletedPayloadDTO shipmentCompletedPayloadDTO, ShipmentCompletedItemProductPayloadDTO shipmentCompletedItemProductPayloadDTO) {
        return ProductLocationHistoryEntity.builder()
            .createdByEmployeeId(shipmentCompletedPayloadDTO.performedBy())
            .type(ProductLocationHistoryType.SHIPPING.name())
            .customerCodeTo(shipmentCompletedPayloadDTO.customerCode())
            .customerNameTo(shipmentCompletedPayloadDTO.customerName())
            .unitNumber(shipmentCompletedItemProductPayloadDTO.unitNumber())
            .productCode(shipmentCompletedItemProductPayloadDTO.productCode())
            .build();
    }
}
