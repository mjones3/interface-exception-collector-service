package com.arcone.biopro.distribution.shipping.infrastructure.mapper;

import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransfer;
import com.arcone.biopro.distribution.shipping.domain.model.ExternalTransferItem;
import com.arcone.biopro.distribution.shipping.domain.model.enumeration.ExternalTransferStatus;
import com.arcone.biopro.distribution.shipping.domain.model.vo.Customer;
import com.arcone.biopro.distribution.shipping.domain.service.CustomerService;
import com.arcone.biopro.distribution.shipping.infrastructure.persistence.ExternalTransferEntity;
import com.arcone.biopro.distribution.shipping.infrastructure.persistence.ExternalTransferItemEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExternalTransferMapper {

    private final CustomerService customerService;

    public ExternalTransfer toDomain(ExternalTransferEntity externalTransferEntity) {

        return new ExternalTransfer(externalTransferEntity.getId(), externalTransferEntity.getCustomerCodeTo()
            , externalTransferEntity.getCustomerCodeFrom(), externalTransferEntity.getHospitalTransferId()
            , externalTransferEntity.getTransferDate() , externalTransferEntity.getCreatedByEmployeeId()
            , ExternalTransferStatus.valueOf(externalTransferEntity.getStatus()), customerService );
    }

    public ExternalTransfer toDomain(ExternalTransferEntity externalTransferEntity , List<ExternalTransferItemEntity> externalTransferItemEntities) {

        return new ExternalTransfer(externalTransferEntity.getId(), externalTransferEntity.getCustomerCodeTo()
            , externalTransferEntity.getCustomerCodeFrom(), externalTransferEntity.getHospitalTransferId()
            , externalTransferEntity.getTransferDate() , externalTransferEntity.getCreatedByEmployeeId()
            , ExternalTransferStatus.valueOf(externalTransferEntity.getStatus()) , Optional.ofNullable(externalTransferItemEntities)
            .filter(list -> !list.isEmpty())
            .orElseGet(Collections::emptyList)
            .stream()
            .map(item -> new ExternalTransferItem(item.getId(), item.getExternalTransferId(), item.getUnitNumber()
                , item.getProductCode() , item.getProductFamily()
                , item.getCreatedByEmployeeId()))
            .toList()  , customerService );
    }

    public ExternalTransferEntity toEntity(ExternalTransfer externalTransfer) {

        return ExternalTransferEntity.builder()
            .id(externalTransfer.getId())
            .customerCodeFrom(Optional.ofNullable(externalTransfer.getCustomerFrom()).map(Customer::getCode).orElse(null))
            .customerNameFrom(Optional.ofNullable(externalTransfer.getCustomerFrom()).map(Customer::getName).orElse(null))
            .customerCodeTo(externalTransfer.getCustomerTo().getCode())
            .customerNameTo(externalTransfer.getCustomerTo().getName())
            .createdByEmployeeId(externalTransfer.getCreateEmployeeId())
            .hospitalTransferId(externalTransfer.getHospitalTransferId())
            .status(externalTransfer.getStatus().name())
            .transferDate(externalTransfer.getTransferDate())
            .build();

    }

}
