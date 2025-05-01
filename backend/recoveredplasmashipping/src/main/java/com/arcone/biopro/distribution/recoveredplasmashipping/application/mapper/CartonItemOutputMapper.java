package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonItemOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Inventory;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.vo.InventoryVolume;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = ReportingPolicy.IGNORE )
public interface CartonItemOutputMapper {

    @Mapping(source ="verifiedByEmployeeId" , target = "verifiedByEmployeeId")
    @Mapping(source ="verifyDate" , target = "verifyDate")
    CartonItemOutput toOutput(CartonItem cartonItem);

    @Mapping(source ="inventory.unitNumber" , target = "unitNumber")
    @Mapping(source ="inventory.productCode" , target = "productCode")
    @Mapping(source ="inventory.productDescription" , target = "productDescription")
    @Mapping(source ="inventory.productFamily" , target = "productType")
    @Mapping(source ="inventory.weight" , target = "weight")
    @Mapping(source ="inventory.aboRh" , target = "aboRh")
    @Mapping(source ="inventory.expirationDate" , target = "expirationDate")
    @Mapping(source ="inventory.collectionDate" , target = "collectionDate")
    @Mapping(source ="inventory.createDate" , target = "createDate")
    @Mapping(source ="inventory.modificationDate" , target = "modificationDate")
    CartonItemOutput toOutput(InventoryValidation inventoryValidation);
}
