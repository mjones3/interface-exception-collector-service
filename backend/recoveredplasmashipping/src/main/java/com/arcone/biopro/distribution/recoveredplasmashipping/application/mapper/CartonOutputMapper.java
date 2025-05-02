package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonPackingSlipOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonPackingSlip;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.InventoryValidation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {CartonItemOutputMapper.class, PackingSlipProductMapper.class} , unmappedTargetPolicy = ReportingPolicy.IGNORE)
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

    @Mapping(source ="cartonPackingSlip.cartonId" , target = "cartonId")
    @Mapping(source ="cartonPackingSlip.cartonNumber" , target = "cartonNumber")
    @Mapping(source ="cartonPackingSlip.cartonSequence" , target = "cartonSequence")
    @Mapping(source ="cartonPackingSlip.totalProducts" , target = "totalProducts")
    @Mapping(source ="cartonPackingSlip.dateTimePacked" , target = "dateTimePacked")
    @Mapping(source ="cartonPackingSlip.packedByEmployeeId" , target = "packedByEmployeeId")
    @Mapping(source ="cartonPackingSlip.testingStatement" , target = "testingStatement")
    @Mapping(target = "displaySignature", expression = "java(cartonPackingSlip.isDisplaySignature())")
    @Mapping(target = "displayTransportationReferenceNumber", expression = "java(cartonPackingSlip.isDisplayTransportationReferenceNumber())")
    @Mapping(target = "displayTestingStatement", expression = "java(cartonPackingSlip.isDisplayTestingStatement())")
    @Mapping(target = "displayLicenceNumber", expression = "java(cartonPackingSlip.isDisplayLicenceNumber())")
    @Mapping(source ="cartonPackingSlip.shipFrom.bloodCenterName" , target = "shipFromBloodCenterName")
    @Mapping(source ="cartonPackingSlip.shipFrom.licenseNumber" , target = "shipFromLicenseNumber")
    @Mapping(source ="cartonPackingSlip.shipFrom.locationAddressFormatted" , target = "shipFromLocationAddress")
    @Mapping(source ="cartonPackingSlip.shipTo.formattedAddress" , target = "shipToAddress")
    @Mapping(source ="cartonPackingSlip.shipTo.customerName" , target = "shipToCustomerName")
    @Mapping(source ="cartonPackingSlip.packingSlipShipment.shipmentNumber" , target = "shipmentNumber")
    @Mapping(source ="cartonPackingSlip.packingSlipShipment.productType" , target = "shipmentProductType")
    @Mapping(source ="cartonPackingSlip.packingSlipShipment.productDescription" , target = "shipmentProductDescription")
    @Mapping(source ="cartonPackingSlip.packingSlipShipment.transportationReferenceNumber" , target = "shipmentTransportationReferenceNumber")
    @Mapping(source ="cartonPackingSlip.packedProducts", target = "products")
    CartonPackingSlipOutput toOutPut(CartonPackingSlip cartonPackingSlip);

}
