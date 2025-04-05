package com.arcone.biopro.distribution.recoveredplasmashipping.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Carton;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.CartonEntityMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence.CartonEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

@ExtendWith(MockitoExtension.class)
class CartonEntityMapperTest {

    private CartonEntityMapper mapper;

    @BeforeEach
    public void setup(){
        mapper = Mappers.getMapper(CartonEntityMapper.class);
    }

    @Test
    public void toEntity(){

        var cartonModel = Mockito.mock(Carton.class);
        Mockito.when(cartonModel.getCartonNumber()).thenReturn("number");
        Mockito.when(cartonModel.getShipmentId()).thenReturn(1L);
        Mockito.when(cartonModel.getCartonSequence()).thenReturn(1);
        Mockito.when(cartonModel.getStatus()).thenReturn("shippingStatus");
        Mockito.when(cartonModel.getId()).thenReturn(1L);
        Mockito.when(cartonModel.getCloseDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(cartonModel.getCreateDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(cartonModel.getModificationDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(cartonModel.getCreateEmployeeId()).thenReturn("create-id");
        Mockito.when(cartonModel.getCloseEmployeeId()).thenReturn("close-id");

        var entity = mapper.toEntity(cartonModel);

        Assertions.assertNotNull(entity);
        Assertions.assertEquals(cartonModel.getCartonNumber(), entity.getCartonNumber());
        Assertions.assertEquals(cartonModel.getShipmentId(), entity.getShipmentId());
        Assertions.assertEquals(cartonModel.getCartonSequence(), entity.getCartonSequenceNumber());
        Assertions.assertEquals(cartonModel.getStatus(), entity.getStatus());
        Assertions.assertEquals(cartonModel.getId(), entity.getId());
        Assertions.assertEquals(cartonModel.getCloseDate(), entity.getCloseDate());
        Assertions.assertEquals(cartonModel.getCreateDate(), entity.getCreateDate());
        Assertions.assertEquals(cartonModel.getModificationDate(), entity.getModificationDate());
        Assertions.assertEquals(cartonModel.getCreateEmployeeId(), entity.getCreateEmployeeId());
        Assertions.assertEquals(cartonModel.getCloseEmployeeId(), entity.getCloseEmployeeId());

    }

    @Test
    public void toModel(){

        var entity = Mockito.mock(CartonEntity.class);
        Mockito.when(entity.getCartonNumber()).thenReturn("number");
        Mockito.when(entity.getShipmentId()).thenReturn(1L);
        Mockito.when(entity.getCartonSequenceNumber()).thenReturn(1);
        Mockito.when(entity.getStatus()).thenReturn("shippingStatus");
        Mockito.when(entity.getId()).thenReturn(1L);
        Mockito.when(entity.getCloseDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(entity.getCreateDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(entity.getModificationDate()).thenReturn(ZonedDateTime.now());
        Mockito.when(entity.getCreateEmployeeId()).thenReturn("create-id");
        Mockito.when(entity.getCloseEmployeeId()).thenReturn("close-id");

        var cartonModel = mapper.entityToModel(entity);

        Assertions.assertNotNull(cartonModel);
        Assertions.assertEquals(entity.getCartonNumber(), cartonModel.getCartonNumber());
        Assertions.assertEquals(entity.getShipmentId(), cartonModel.getShipmentId());
        Assertions.assertEquals(entity.getCartonSequenceNumber(), cartonModel.getCartonSequence());
        Assertions.assertEquals(entity.getStatus(), cartonModel.getStatus());
        Assertions.assertEquals(entity.getId(), cartonModel.getId());
        Assertions.assertEquals(entity.getCloseDate(), cartonModel.getCloseDate());
        Assertions.assertEquals(entity.getCreateDate(), cartonModel.getCreateDate());
        Assertions.assertEquals(entity.getModificationDate(), cartonModel.getModificationDate());
        Assertions.assertEquals(entity.getCreateEmployeeId(), cartonModel.getCreateEmployeeId());
        Assertions.assertEquals(entity.getCloseEmployeeId(), cartonModel.getCloseEmployeeId());

    }

}
