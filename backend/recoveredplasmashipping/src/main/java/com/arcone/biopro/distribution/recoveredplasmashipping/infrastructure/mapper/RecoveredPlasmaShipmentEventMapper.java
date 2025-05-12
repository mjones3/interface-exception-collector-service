package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.CartonItem;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonClosedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaCartonItemClosedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaShipmentClosedOutputDTO;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.dto.RecoveredPlasmaShipmentCreatedOutputDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring" , unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RecoveredPlasmaShipmentEventMapper {

    @Mapping(target = "customerCode", source = "shipmentCustomer.customerCode")
    @Mapping(target = "customerName", source = "shipmentCustomer.customerName")
    @Mapping(target = "customerState", source = "shipmentCustomer.customerState")
    @Mapping(target = "customerPostalCode", source = "shipmentCustomer.customerPostalCode")
    @Mapping(target = "customerCountry", source = "shipmentCustomer.customerCountry")
    @Mapping(target = "customerCountryCode", source = "shipmentCustomer.customerCountryCode")
    @Mapping(target = "customerCity", source = "shipmentCustomer.customerCity")
    @Mapping(target = "customerDistrict", source = "shipmentCustomer.customerDistrict")
    @Mapping(target = "customerAddressLine1", source = "shipmentCustomer.customerAddressLine1")
    @Mapping(target = "customerAddressLine2", source = "shipmentCustomer.customerAddressLine2")
    @Mapping(target = "customerAddressContactName", source = "shipmentCustomer.customerAddressContactName")
    @Mapping(target = "customerAddressPhoneNumber", source = "shipmentCustomer.customerAddressPhoneNumber")
    @Mapping(target = "customerAddressDepartmentName", source = "shipmentCustomer.customerAddressDepartmentName")
    RecoveredPlasmaShipmentCreatedOutputDTO modelToEventDTO(RecoveredPlasmaShipment recoveredPlasmaShipment);

    @Mapping(target = "customerCode", source = "shipmentCustomer.customerCode")
    @Mapping(target = "customerName", source = "shipmentCustomer.customerName")
    @Mapping(target = "customerState", source = "shipmentCustomer.customerState")
    @Mapping(target = "customerPostalCode", source = "shipmentCustomer.customerPostalCode")
    @Mapping(target = "customerCountry", source = "shipmentCustomer.customerCountry")
    @Mapping(target = "customerCountryCode", source = "shipmentCustomer.customerCountryCode")
    @Mapping(target = "customerCity", source = "shipmentCustomer.customerCity")
    @Mapping(target = "customerDistrict", source = "shipmentCustomer.customerDistrict")
    @Mapping(target = "customerAddressLine1", source = "shipmentCustomer.customerAddressLine1")
    @Mapping(target = "customerAddressLine2", source = "shipmentCustomer.customerAddressLine2")
    @Mapping(target = "customerAddressContactName", source = "shipmentCustomer.customerAddressContactName")
    @Mapping(target = "customerAddressPhoneNumber", source = "shipmentCustomer.customerAddressPhoneNumber")
    @Mapping(target = "customerAddressDepartmentName", source = "shipmentCustomer.customerAddressDepartmentName")
    @Mapping(target = "totalCartons", expression = "java(recoveredPlasmaShipment.getTotalCartons())")
    @Mapping(target = "closedEmployeeId", source = "closeEmployeeId")
    RecoveredPlasmaShipmentClosedOutputDTO modelToCloseEventDTO(RecoveredPlasmaShipment recoveredPlasmaShipment);

    @Mapping(target = "productType", source = "recoveredPlasmaShipment.productType")
    @Mapping(target = "locationCode", source = "recoveredPlasmaShipment.locationCode")
    @Mapping(target = "closeEmployeeId", source = "carton.closeEmployeeId")
    @Mapping(target = "closeDate", source = "carton.closeDate")
    @Mapping(target = "status", source = "carton.status")
    @Mapping(target = "totalProducts", source = "carton.totalProducts")
    RecoveredPlasmaCartonClosedOutputDTO cartonModelToEventDTO (RecoveredPlasmaShipment recoveredPlasmaShipment , Carton carton);

    @Mapping(target = "donationDate", source = "collectionDate")
    RecoveredPlasmaCartonItemClosedOutputDTO itemModelToEventDTO(CartonItem cartonItem);

}
