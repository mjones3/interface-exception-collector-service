package com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RecoveredPlasmaShipmentOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring" , uses = CartonOutputMapper.class)
public interface RecoveredPlasmaShipmentOutputMapper {

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
    @Mapping(target = "canClose", expression = "java(recoveredPlasmaShipment.canClose())")
    @Mapping(target = "canModify", expression = "java(recoveredPlasmaShipment.canModify())")
    RecoveredPlasmaShipmentOutput toRecoveredPlasmaShipmentOutput(RecoveredPlasmaShipment recoveredPlasmaShipment);
}
