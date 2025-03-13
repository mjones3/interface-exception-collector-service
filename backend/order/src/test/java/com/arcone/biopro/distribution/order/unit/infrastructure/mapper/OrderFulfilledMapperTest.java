package com.arcone.biopro.distribution.order.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.OrderItem;
import com.arcone.biopro.distribution.order.domain.model.PickList;
import com.arcone.biopro.distribution.order.domain.model.PickListItem;
import com.arcone.biopro.distribution.order.domain.model.PickListItemShortDate;
import com.arcone.biopro.distribution.order.domain.model.vo.BloodType;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomer;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderExternalId;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderItemOrderId;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderNumber;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriority;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderStatus;
import com.arcone.biopro.distribution.order.domain.model.vo.ProductCategory;
import com.arcone.biopro.distribution.order.domain.model.vo.ProductFamily;
import com.arcone.biopro.distribution.order.domain.model.vo.ShippingMethod;
import com.arcone.biopro.distribution.order.infrastructure.dto.OrderFulfilledDTO;
import com.arcone.biopro.distribution.order.infrastructure.event.OrderFulfilledEventDTO;
import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderFulfilledMapper;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerAddressDTO;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.LocalDate;
import java.util.List;

class OrderFulfilledMapperTest {

    private Order orderMock;
    private PickList pickListMock;
    private PickListItem pickListItemMock;

    @BeforeEach
    public void setup() {

        orderMock = Mockito.mock(Order.class);

        var orderNumber = Mockito.mock(OrderNumber.class);
        Mockito.when(orderNumber.getOrderNumber()).thenReturn(1L);
        Mockito.when(orderMock.getOrderNumber()).thenReturn(orderNumber);

        var externalId = Mockito.mock(OrderExternalId.class);
        Mockito.when(externalId.getOrderExternalId()).thenReturn("EXTERNAL_ID");
        Mockito.when(orderMock.getOrderExternalId()).thenReturn(externalId);

        var customer = Mockito.mock(OrderCustomer.class);
        Mockito.when(customer.getCode()).thenReturn("1");
        Mockito.when(customer.getName()).thenReturn("NAME");

        var orderStatus = Mockito.mock(OrderStatus.class);
        Mockito.when(orderStatus.getOrderStatus()).thenReturn("STATUS");

        var deliveryType = Mockito.mock(OrderPriority.class);
        Mockito.when(deliveryType.getDeliveryType()).thenReturn("DELIVERY");

        var shippingMethod = Mockito.mock(ShippingMethod.class);
        Mockito.when(shippingMethod.getShippingMethod()).thenReturn("SHIPPING_METHOD");

        var productCategory = Mockito.mock(ProductCategory.class);
        Mockito.when(productCategory.getProductCategory()).thenReturn("PRODUCT_CATEGORY");

        Mockito.when(orderMock.getBillingCustomer()).thenReturn(customer);
        Mockito.when(orderMock.getShippingCustomer()).thenReturn(customer);
        Mockito.when(orderMock.getOrderStatus()).thenReturn(orderStatus);
        Mockito.when(orderMock.getOrderPriority()).thenReturn(deliveryType);
        Mockito.when(orderMock.getLocationCode()).thenReturn("LOCATION_CODE");
        Mockito.when(orderMock.getDesiredShippingDate()).thenReturn(LocalDate.now());
        Mockito.when(orderMock.getShippingMethod()).thenReturn(shippingMethod);
        Mockito.when(orderMock.getProductCategory()).thenReturn(productCategory);
        Mockito.when(orderMock.getComments()).thenReturn("COMMENTS");

        var orderId = Mockito.mock(OrderItemOrderId.class);
        Mockito.when(orderId.getOrderId()).thenReturn(1L);

        var orderItem = Mockito.mock(OrderItem.class);
        Mockito.when(orderItem.getOrderId()).thenReturn(orderId);
        Mockito.when(orderItem.getQuantity()).thenReturn(1);

        var productFamily = Mockito.mock(ProductFamily.class);
        Mockito.when(productFamily.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(orderItem.getProductFamily()).thenReturn(productFamily);

        var bloodType = Mockito.mock(BloodType.class);
        Mockito.when(bloodType.getBloodType()).thenReturn("BLOOD_TYPE");
        Mockito.when(orderItem.getBloodType()).thenReturn(bloodType);

        Mockito.when(orderItem.getComments()).thenReturn("COMMENTS");

        Mockito.when(orderMock.getOrderItems()).thenReturn(List.of(orderItem));

        pickListMock = Mockito.mock(PickList.class);

        pickListItemMock = Mockito.mock(PickListItem.class);
        Mockito.when(pickListItemMock.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(pickListItemMock.getBloodType()).thenReturn("BLOOD_TYPE");
    }

    @Test
    public void shouldMapToOrderFulfilled() {

        var shortDatePickList = Mockito.mock(PickListItemShortDate.class);
        Mockito.when(shortDatePickList.getProductCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(shortDatePickList.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(shortDatePickList.getStorageLocation()).thenReturn("STORAGE_LOCATION");

        Mockito.when(pickListItemMock.getShortDateList()).thenReturn(List.of(shortDatePickList));

        Mockito.when(pickListMock.getPickListItems()).thenReturn(List.of(pickListItemMock));


        var mapper = new OrderFulfilledMapper();

        var dto = mapper.buildOrderDetails(orderMock,pickListMock).getPayload();

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(1L, dto.getOrderNumber());
        Assertions.assertEquals("EXTERNAL_ID", dto.getExternalId());
        Assertions.assertEquals("DELIVERY", dto.getDeliveryType());
        Assertions.assertEquals("NAME", dto.getBillingCustomerName());
        Assertions.assertEquals("NAME", dto.getShippingCustomerName());
        Assertions.assertEquals("1", dto.getBillingCustomerCode());
        Assertions.assertEquals("1", dto.getShippingCustomerCode());
        Assertions.assertEquals("DELIVERY", dto.getPriority());
        Assertions.assertEquals("STATUS", dto.getStatus());
        Assertions.assertEquals("LOCATION_CODE", dto.getLocationCode());
        Assertions.assertEquals(LocalDate.now(), dto.getShippingDate());
        Assertions.assertEquals("LOCATION_CODE", dto.getLocationCode());
        Assertions.assertEquals("SHIPPING_METHOD", dto.getShippingMethod());
        Assertions.assertEquals("PRODUCT_CATEGORY", dto.getProductCategory());
        Assertions.assertEquals("COMMENTS", dto.getComments());

        // assert items
        Assertions.assertNotNull(dto.getItems());
        Assertions.assertNotNull(dto.getItems().getFirst());
        Assertions.assertEquals(1L, dto.getItems().getFirst().orderId());
        Assertions.assertEquals("COMMENTS", dto.getItems().getFirst().comments());
        Assertions.assertEquals("PRODUCT_FAMILY", dto.getItems().getFirst().productFamily());
        Assertions.assertEquals(1, dto.getItems().getFirst().quantity());
        Assertions.assertEquals("BLOOD_TYPE", dto.getItems().getFirst().bloodType());

        // assert short date
        var shortDate = dto.getItems().getFirst().shortDateProducts().getFirst();
        Assertions.assertNotNull(shortDate);
        Assertions.assertEquals("PRODUCT_CODE", shortDate.productCode());
        Assertions.assertEquals("STORAGE_LOCATION", shortDate.storageLocation());
        Assertions.assertEquals("UNIT_NUMBER", shortDate.unitNumber());

    }

    @Test
    public void shouldShortDateEmptyWhenPickListIsEmpty() {

        var mapper = new OrderFulfilledMapper();

        var dto = mapper.buildOrderDetails(orderMock,pickListMock).getPayload();
        var shortDate = dto.getItems().getFirst().shortDateProducts();
        Assertions.assertNotNull(shortDate);
        Assertions.assertEquals(0,shortDate.size());
    }

    @Test
    public void shouldPopulateShortDateBasedOnFamilyAndBloodType() {

        var pickListItemMockDifferentFamily = Mockito.mock(PickListItem.class);
        Mockito.when(pickListItemMockDifferentFamily.getProductFamily()).thenReturn("PRODUCT_FAMILY_2");
        Mockito.when(pickListItemMockDifferentFamily.getBloodType()).thenReturn("BLOOD_TYPE_2");

        var shortDatePickList = Mockito.mock(PickListItemShortDate.class);
        Mockito.when(shortDatePickList.getProductCode()).thenReturn("PRODUCT_CODE");
        Mockito.when(shortDatePickList.getUnitNumber()).thenReturn("UNIT_NUMBER");
        Mockito.when(shortDatePickList.getStorageLocation()).thenReturn("STORAGE_LOCATION");

        Mockito.when(pickListItemMockDifferentFamily.getShortDateList()).thenReturn(List.of(shortDatePickList));

        Mockito.when(pickListMock.getPickListItems()).thenReturn(List.of(pickListItemMock,pickListItemMockDifferentFamily));


        var orderId = Mockito.mock(OrderItemOrderId.class);
        Mockito.when(orderId.getOrderId()).thenReturn(1L);

        var orderItem = Mockito.mock(OrderItem.class);
        Mockito.when(orderItem.getOrderId()).thenReturn(orderId);
        Mockito.when(orderItem.getQuantity()).thenReturn(1);

        var productFamily = Mockito.mock(ProductFamily.class);
        Mockito.when(productFamily.getProductFamily()).thenReturn("PRODUCT_FAMILY");
        Mockito.when(orderItem.getProductFamily()).thenReturn(productFamily);

        var bloodType = Mockito.mock(BloodType.class);
        Mockito.when(bloodType.getBloodType()).thenReturn("BLOOD_TYPE");
        Mockito.when(orderItem.getBloodType()).thenReturn(bloodType);



        var orderIdSecond = Mockito.mock(OrderItemOrderId.class);
        Mockito.when(orderId.getOrderId()).thenReturn(1L);

        var orderItemSecond = Mockito.mock(OrderItem.class);
        Mockito.when(orderItemSecond.getOrderId()).thenReturn(orderIdSecond);
        Mockito.when(orderItemSecond.getQuantity()).thenReturn(2);

        var productFamilySecond = Mockito.mock(ProductFamily.class);
        Mockito.when(productFamilySecond.getProductFamily()).thenReturn("PRODUCT_FAMILY_2");
        Mockito.when(orderItemSecond.getProductFamily()).thenReturn(productFamilySecond);

        var bloodTypeSecond = Mockito.mock(BloodType.class);
        Mockito.when(bloodTypeSecond.getBloodType()).thenReturn("BLOOD_TYPE_2");
        Mockito.when(orderItemSecond.getBloodType()).thenReturn(bloodTypeSecond);


        Mockito.when(orderMock.getOrderItems()).thenReturn(List.of(orderItem, orderItemSecond));



        var mapper = new OrderFulfilledMapper();

        var dto = mapper.buildOrderDetails(orderMock,pickListMock).getPayload();


        // assert items
        Assertions.assertNotNull(dto.getItems());
        Assertions.assertEquals(2, dto.getItems().size());
        Assertions.assertTrue(dto.getItems().stream().anyMatch(orderFulfilledItemDTO -> orderFulfilledItemDTO.productFamily().equals("PRODUCT_FAMILY_2") && orderFulfilledItemDTO.bloodType().equals("BLOOD_TYPE_2")));

        var orderItemDto  = dto.getItems().stream().filter(orderFulfilledItemDTO ->
            orderFulfilledItemDTO.productFamily().equals("PRODUCT_FAMILY_2")
                && orderFulfilledItemDTO.bloodType().equals("BLOOD_TYPE_2") && !orderFulfilledItemDTO.shortDateProducts().isEmpty()).findFirst();

        Assertions.assertTrue(orderItemDto.isPresent());

        // assert short date
        var shortDate = orderItemDto.get().shortDateProducts().getFirst();
        Assertions.assertNotNull(shortDate);
        Assertions.assertEquals("PRODUCT_CODE", shortDate.productCode());
        Assertions.assertEquals("STORAGE_LOCATION", shortDate.storageLocation());
        Assertions.assertEquals("UNIT_NUMBER", shortDate.unitNumber());
    }

    @Test
    public void shouldNotBuildShippingCustomerWhenAddressMissing(){

        var tupleMock = Mockito.mock(Tuple2.class);

        var orderFulfilledDTO = Mockito.mock(OrderFulfilledEventDTO.class);

        var customerDTO = Mockito.mock(CustomerDTO.class);

        Mockito.when(tupleMock.getT1()).thenReturn(orderFulfilledDTO);
        Mockito.when(tupleMock.getT2()).thenReturn(customerDTO);

        var mapper = new OrderFulfilledMapper();

        StepVerifier.create(mapper.buildShippingCustomerDetails(tupleMock))
            .expectError(IllegalArgumentException.class)
            .verify();

    }

    @Test
    public void shouldBuildShippingCustomer(){

        var tupleMock = Mockito.mock(Tuple2.class);

        var addressMock = Mockito.mock(CustomerAddressDTO.class);
        Mockito.when(addressMock.addressType()).thenReturn("SHIPPING");
        Mockito.when(addressMock.state()).thenReturn("CustomerAddressState");
        Mockito.when(addressMock.postalCode()).thenReturn("CustomerAddressPostalCode");
        Mockito.when(addressMock.countryCode()).thenReturn("CustomerAddressCountryCode");
        Mockito.when(addressMock.city()).thenReturn("CustomerAddressCity");
        Mockito.when(addressMock.district()).thenReturn("CustomerAddressDistrict");
        Mockito.when(addressMock.addressLine2()).thenReturn("CustomerAddressAddressLine2");
        Mockito.when(addressMock.addressLine1()).thenReturn("CustomerAddressAddressLine1");

        var customerDTO = Mockito.mock(CustomerDTO.class);
        Mockito.when(customerDTO.addresses()).thenReturn(List.of(addressMock));
        Mockito.when(customerDTO.phoneNumber()).thenReturn("PHONE_NUMBER");

        Mockito.when(tupleMock.getT1()).thenReturn(new OrderFulfilledEventDTO(new OrderFulfilledDTO()));
        Mockito.when(tupleMock.getT2()).thenReturn(customerDTO);

        var mapper = new OrderFulfilledMapper();

        Mono<OrderFulfilledEventDTO> dto = mapper.buildShippingCustomerDetails(tupleMock);

        StepVerifier.create(dto)
            .consumeNextWith( response  -> {
                Assertions.assertNotNull(response);
                Assertions.assertEquals("PHONE_NUMBER",response.getPayload().getCustomerPhoneNumber());
                Assertions.assertEquals("CustomerAddressState",response.getPayload().getCustomerAddressState());
                Assertions.assertEquals("CustomerAddressPostalCode",response.getPayload().getCustomerAddressPostalCode());
                Assertions.assertEquals("CustomerAddressCountryCode",response.getPayload().getCustomerAddressCountry());
                Assertions.assertEquals("CustomerAddressCountryCode",response.getPayload().getCustomerAddressCountryCode());
                Assertions.assertEquals("CustomerAddressCity",response.getPayload().getCustomerAddressCity());
                Assertions.assertEquals("CustomerAddressDistrict",response.getPayload().getCustomerAddressDistrict());
                Assertions.assertEquals("CustomerAddressAddressLine1",response.getPayload().getCustomerAddressAddressLine1());
                Assertions.assertEquals("CustomerAddressAddressLine2",response.getPayload().getCustomerAddressAddressLine2());

            })
            .verifyComplete();

    }




}
