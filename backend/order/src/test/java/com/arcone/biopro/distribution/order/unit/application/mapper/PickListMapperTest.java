package com.arcone.biopro.distribution.order.unit.application.mapper;

import com.arcone.biopro.distribution.order.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.order.application.dto.UseCaseNotificationDTO;
import com.arcone.biopro.distribution.order.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.order.application.dto.UseCaseResponseDTO;
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

        var useCaseResponse = Mockito.mock(UseCaseResponseDTO.class);

        var picklistMock = Mockito.mock(PickList.class);

        Mockito.when(useCaseResponse.data()).thenReturn(picklistMock);

        Mockito.when(picklistMock.getOrderNumber()).thenReturn(2L);

        var pickListCustomer = Mockito.mock(PickListCustomer.class);

        var pickListItem = Mockito.mock(PickListItem.class);
        Mockito.when(pickListItem.getBloodType()).thenReturn("ABP");

        var pickListShortDate = Mockito.mock(PickListItemShortDate.class);
        Mockito.when(pickListShortDate.getUnitNumber()).thenReturn("UNIT_NUMBER");

        Mockito.when(pickListItem.getShortDateList()).thenReturn(List.of(pickListShortDate));

        Mockito.when(picklistMock.getCustomer()).thenReturn(pickListCustomer);

        Mockito.when(picklistMock.getPickListItems()).thenReturn(List.of(pickListItem));

        Mockito.when(useCaseResponse.notifications()).thenReturn(List.of(UseCaseNotificationDTO
            .builder()
                .useCaseMessageType(UseCaseMessageType.INVENTORY_SERVICE_IS_DOWN)
            .build()));

        var target = new PickListMapper();

        var dto = target.mapToDTO(useCaseResponse);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(2L,dto.data().orderNumber());

        Assertions.assertNotNull(dto.data().pickListItems());
        Assertions.assertEquals("ABP",dto.data().pickListItems().getFirst().bloodType());


        Assertions.assertNotNull(dto.data().pickListItems().getFirst().shortDateList());
        Assertions.assertEquals("UNIT_NUMBER",dto.data().pickListItems().getFirst().shortDateList().getFirst().unitNumber());

        Assertions.assertNotNull(dto.notifications());
        Assertions.assertEquals("Inventory Service is down.",dto.notifications().getFirst().notificationMessage());
        Assertions.assertEquals(UseCaseNotificationType.ERROR.name(),dto.notifications().getFirst().notificationType());

    }

    @Test
    public void shouldMapToDtoWhenDataIsNull(){

        var useCaseResponse = Mockito.mock(UseCaseResponseDTO.class);

        Mockito.when(useCaseResponse.data()).thenReturn(null);
        Mockito.when(useCaseResponse.notifications()).thenReturn(List.of(UseCaseNotificationDTO
            .builder()
            .useCaseMessageType(UseCaseMessageType.INVENTORY_SERVICE_IS_DOWN)
            .build()));

        var target = new PickListMapper();

        var dto = target.mapToDTO(useCaseResponse);

        Assertions.assertNotNull(dto);

        Assertions.assertNotNull(dto.notifications());
        Assertions.assertEquals("Inventory Service is down.",dto.notifications().getFirst().notificationMessage());
        Assertions.assertEquals(UseCaseNotificationType.ERROR.name(),dto.notifications().getFirst().notificationType());

    }

}
