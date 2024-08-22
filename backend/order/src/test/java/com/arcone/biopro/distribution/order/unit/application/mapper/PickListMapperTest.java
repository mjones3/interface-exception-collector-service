package com.arcone.biopro.distribution.order.unit.application.mapper;

import com.arcone.biopro.distribution.order.application.mapper.PickListMapper;
import com.arcone.biopro.distribution.order.domain.model.PickList;
import com.arcone.biopro.distribution.order.domain.model.PickListItem;
import com.arcone.biopro.distribution.order.domain.model.PickListItemShortDate;
import com.arcone.biopro.distribution.order.domain.model.vo.PickListCustomer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class PickListMapperTest {

    @Test
    public void shouldMapToDto(){

        var picklistMock = Mockito.mock(PickList.class);
        Mockito.when(picklistMock.getOrderNumber()).thenReturn(2L);

        var pickListCustomer = Mockito.mock(PickListCustomer.class);

        var pickListItem = Mockito.mock(PickListItem.class);
        Mockito.when(pickListItem.getBloodType()).thenReturn("ABP");

        var pickListShortDate = Mockito.mock(PickListItemShortDate.class);
        Mockito.when(pickListShortDate.getUnitNumber()).thenReturn("UNIT_NUMBER");

        Mockito.when(pickListItem.getShortDateList()).thenReturn(List.of(pickListShortDate));

        Mockito.when(picklistMock.getCustomer()).thenReturn(pickListCustomer);

        Mockito.when(picklistMock.getPickListItems()).thenReturn(List.of(pickListItem));

        var target = new PickListMapper();

        var dto = target.mapToDTO(picklistMock);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(2L,dto.orderNumber());

        Assertions.assertNotNull(dto.pickListItems());
        Assertions.assertEquals("ABP",dto.pickListItems().getFirst().bloodType());


        Assertions.assertNotNull(dto.pickListItems().getFirst().shortDateList());
        Assertions.assertEquals("UNIT_NUMBER",dto.pickListItems().getFirst().shortDateList().getFirst().unitNumber());

    }

}
