package com.arcone.biopro.distribution.order.unit.application.mapper;

import com.arcone.biopro.distribution.order.application.mapper.PickListCommandMapper;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.OrderItem;
import com.arcone.biopro.distribution.order.domain.model.PickList;
import com.arcone.biopro.distribution.order.domain.model.PickListItem;
import com.arcone.biopro.distribution.order.domain.model.vo.BloodType;
import com.arcone.biopro.distribution.order.domain.model.vo.ProductCategory;
import com.arcone.biopro.distribution.order.domain.model.vo.ProductFamily;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class PickListCommandMapperTest {

    @Test
    public void shouldMapGeneratePickListCommandToDomain(){
        var target = new PickListCommandMapper();
        var pickList = Mockito.mock(PickList.class);
        Mockito.when(pickList.getLocationCode()).thenReturn("LOCATION_CODE");

        var pickListItem = Mockito.mock(PickListItem.class);
        Mockito.when(pickListItem.getBloodType()).thenReturn("BLOOD_TYPE");
        Mockito.when(pickListItem.getProductFamily()).thenReturn("PRODUCT_FAMILY");

        Mockito.when(pickList.getPickListItems()).thenReturn(List.of(pickListItem));

        var response = target.mapToDomain(pickList);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("LOCATION_CODE", response.getLocationCode());

        Assertions.assertEquals("BLOOD_TYPE", response.getProductCriteria().getFirst().getBloodType());
        Assertions.assertEquals("PRODUCT_FAMILY", response.getProductCriteria().getFirst().getProductFamily());
    }

    @Test
    public void shouldMapOrderGeneratePickListCommandToDomain(){
        var target = new PickListCommandMapper();
        var order = Mockito.mock(Order.class);
        Mockito.when(order.getLocationCode()).thenReturn("LOCATION_CODE");

        var productCategory = Mockito.mock(ProductCategory.class);
        Mockito.when(productCategory.getProductCategory()).thenReturn("FROZEN");
        Mockito.when(order.getProductCategory()).thenReturn(productCategory);


        var orderItem = Mockito.mock(OrderItem.class);

        var bloodType = Mockito.mock(BloodType.class);
        Mockito.when(bloodType.getBloodType()).thenReturn("BLOOD_TYPE");

        var productFamily = Mockito.mock(ProductFamily.class);
        Mockito.when(productFamily.getProductFamily()).thenReturn("PRODUCT_FAMILY");

        Mockito.when(orderItem.getProductFamily()).thenReturn(productFamily);
        Mockito.when(orderItem.getBloodType()).thenReturn(bloodType);

        Mockito.when(order.getOrderItems()).thenReturn(List.of(orderItem));

        var response = target.mapToDomain(order);

        Assertions.assertNotNull(response);
        Assertions.assertEquals("LOCATION_CODE", response.getLocationCode());

        Assertions.assertEquals("BLOOD_TYPE", response.getProductCriteria().getFirst().getBloodType());
        Assertions.assertEquals("PRODUCT_FAMILY", response.getProductCriteria().getFirst().getProductFamily());
        Assertions.assertEquals("FROZEN", response.getProductCriteria().getFirst().getTemperatureCategory());
    }

}
