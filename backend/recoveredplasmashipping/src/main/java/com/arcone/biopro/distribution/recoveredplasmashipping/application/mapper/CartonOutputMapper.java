package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = CartonItemOutputMapper.class , unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartonOutputMapper {
    @Mapping(source = "products", target = "packedProducts")
    @Mapping(target = "canVerify", expression = "java(carton.canVerify())")
    @Mapping(target = "canClose", expression = "java(carton.canClose())")
    CartonOutput toOutput(Carton carton);

    @Mapping(target = "failedCartonItem.id" , ignore = true)
    @Mapping(source ="inventoryValidation.inventory.unitNumber" , target = "failedCartonItem.unitNumber")
    @Mapping(source ="inventoryValidation.inventory.productCode" , target = "failedCartonItem.productCode")
    @Mapping(source ="inventoryValidation.inventory.productDescription" , target = "failedCartonItem.productDescription")
    @Mapping(source ="inventoryValidation.inventory.productFamily" , target = "failedCartonItem.productType")
    @Mapping(source ="inventoryValidation.inventory.weight" , target = "failedCartonItem.weight")
    @Mapping(source ="inventoryValidation.inventory.aboRh" , target = "failedCartonItem.aboRh")
    @Mapping(source ="inventoryValidation.inventory.expirationDate" , target = "failedCartonItem.expirationDate")
    @Mapping(source ="inventoryValidation.inventory.collectionDate" , target = "failedCartonItem.collectionDate")
    @Mapping(source ="inventoryValidation.inventory.createDate" , target = "failedCartonItem.createDate")
    @Mapping(source ="inventoryValidation.inventory.modificationDate" , target = "failedCartonItem.modificationDate")
    @Mapping(source ="carton.products" , target = "packedProducts")
    @Mapping(target = "canVerify", expression = "java(carton.canVerify())")
    @Mapping(target = "canClose", expression = "java(carton.canClose())")
    CartonOutput toOutput(Carton carton,InventoryValidation inventoryValidation);

}
