package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper;


import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Customer;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CustomerEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerEntityMapper {
    Customer entityToModel(CustomerEntity entity);
}



