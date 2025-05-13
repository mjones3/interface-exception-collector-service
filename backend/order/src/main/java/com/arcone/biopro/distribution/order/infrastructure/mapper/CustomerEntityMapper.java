package com.arcone.biopro.distribution.order.infrastructure.mapper;


import com.arcone.biopro.distribution.order.domain.model.Customer;
import com.arcone.biopro.distribution.order.domain.model.vo.CustomerAddress;
import com.arcone.biopro.distribution.order.domain.model.vo.CustomerAddressType;
import com.arcone.biopro.distribution.order.domain.model.vo.CustomerCode;
import com.arcone.biopro.distribution.order.infrastructure.persistence.CustomerAddressEntity;
import com.arcone.biopro.distribution.order.infrastructure.persistence.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerEntityMapper {
    @Mapping(target = "code", expression = "java(map(entity.getCode()))")
    Customer entityToModel(CustomerEntity entity);

    default CustomerCode map(String code) {
        return new CustomerCode(code);
    }

    @Mapping(target = "addresses" , expression = "java(toModelList(customerAddressEntities))")
    Customer entityToModel(CustomerEntity entity , List<CustomerAddressEntity> customerAddressEntities);

    default List<CustomerAddress> toModelList(List<CustomerAddressEntity> customerAddressEntityList){
        if(customerAddressEntityList == null){
            return null;
        }
        return customerAddressEntityList.stream().map(this::entityToModel).toList();
    }

    @Mapping(target = "addressType", expression = "java(mapAddressType(customerAddressEntity.getAddressType()))")
    CustomerAddress entityToModel(CustomerAddressEntity customerAddressEntity);

    default CustomerAddressType mapAddressType(String addressType) {
        return new CustomerAddressType(addressType);
    }
}



