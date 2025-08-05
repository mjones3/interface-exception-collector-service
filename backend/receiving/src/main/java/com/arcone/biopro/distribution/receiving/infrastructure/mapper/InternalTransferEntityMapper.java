package com.arcone.biopro.distribution.receiving.infrastructure.mapper;

import com.arcone.biopro.distribution.receiving.domain.model.InternalTransfer;
import com.arcone.biopro.distribution.receiving.domain.model.InternalTransferItem;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.InternalTransferEntity;
import com.arcone.biopro.distribution.receiving.infrastructure.persistence.InternalTransferItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InternalTransferEntityMapper {

    default InternalTransfer mapToDomain(final InternalTransferEntity entity , List<InternalTransferItemEntity> items) {
        return InternalTransfer.fromRepository(entity.getId(), entity.getOrderNumber()
            , entity.getExternalOrderId(), entity.getTemperatureCategory(), entity.getLocationCodeFrom(), entity.getLocationCodeTo()
            , entity.getLabelStatus(), entity.getQuarantinedProducts() , mapToDomainList(items) );
    }

    List<InternalTransferItem> mapToDomainList(List<InternalTransferItemEntity> itemEntities);

    InternalTransferItem mapToDomain(InternalTransferItemEntity entity);

    @Mapping(source = "createEmployeeId", target = "employeeId")
    InternalTransferEntity mapToEntity(InternalTransfer internalTransfer);

    InternalTransferItemEntity mapToEntity(InternalTransferItem internalTransferItem);
}
