package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;


import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RecoveredPlasmaShipment;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.RecoveredPlasmaShipmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecoveredPlasmaShipmentEntityMapper {


    default RecoveredPlasmaShipment entityToModel(RecoveredPlasmaShipmentEntity entity) {
        return RecoveredPlasmaShipment.fromRepository(
            entity.getId(), entity.getLocationCode(), entity.getProductType(), entity.getShipmentNumber(), entity.getStatus(), entity.getCreateEmployeeId(),
            entity.getCloseEmployeeId(), entity.getCloseDate(), entity.getTransportationReferenceNumber(),
            entity.getScheduleDate(), entity.getShipmentDate(), entity.getCartonTareWeight(), entity.getUnsuitableUnitReportDocumentStatus(),
            entity.getCustomerCode(), entity.getCustomerName(), entity.getCustomerState(), entity.getCustomerPostalCode(), entity.getCustomerCountry(),
            entity.getCustomerCountry(), entity.getCustomerCity(), entity.getCustomerDistrict(), entity.getCustomerAddressLine1(),
            entity.getCustomerAddressLine2(), entity.getCustomerAddressContactName(), entity.getCustomerAddressPhoneNumber(),
            entity.getCustomerAddressDepartmentName(),entity.getCreateDate(), entity.getModificationDate()
        );
    }


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
    RecoveredPlasmaShipmentEntity toEntity(RecoveredPlasmaShipment recoveredPlasmaShipment);
}



