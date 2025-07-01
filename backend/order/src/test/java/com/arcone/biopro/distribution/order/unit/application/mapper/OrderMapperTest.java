package com.arcone.biopro.distribution.order.unit.application.mapper;

import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderDTO;
import com.arcone.biopro.distribution.order.adapter.in.web.dto.OrderItemDTO;
import com.arcone.biopro.distribution.order.application.mapper.OrderItemMapper;
import com.arcone.biopro.distribution.order.application.mapper.OrderMapper;
import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
import com.arcone.biopro.distribution.order.domain.service.OrderShipmentService;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringJUnitConfig(classes = { OrderMapper.class, OrderItemMapper.class , OrderShipmentService.class})
class OrderMapperTest {

    @MockBean
    CustomerService customerService;
    @Autowired
    OrderMapper orderMapper;
    @MockBean
    OrderConfigService orderConfigService;
    @MockBean
    LookupService lookupService;
    @MockBean
    OrderShipmentService orderShipmentService;

    @BeforeEach
    void beforeEach() {
        given(customerService.getCustomerByCode(anyString()))
            .willReturn(Mono.just(
                CustomerDTO.builder()
                    .code("code")
                    .name("name")
                    .build()
            ));

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("shipmentType","shipmentType"),"description",1,true)
                , new Lookup(new LookupId("shippingMethod","shippingMethod"),"description",2,true)
                , new Lookup(new LookupId("productCategory","productCategory"),"description",2,true)
                , new Lookup(new LookupId("status","status"),"description",2,true)
                , new Lookup(new LookupId("priority","priority"),"description",2,true)
            )
        );

        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("productFamily"));
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("bloodType"));

        Mockito.when(orderConfigService.findBackOrderConfiguration()).thenReturn(Mono.just(Boolean.FALSE));


    }

    @Test
    void testMapToDTO() {

        // Setup
        var order = new Order(
            customerService,
            lookupService,
            1L,
            1L,
            "externalId",
            "locationCode",
            "shipmentType",
            "shippingMethod",
            "code",
            "code",
            LocalDate.now().toString(),
            Boolean.TRUE,
            "phoneNumber",
            "productCategory",
            "comments",
            "status",
            "priority",
            "createEmployeeId",
            "2023-04-25 20:09:01",
            ZonedDateTime.now(),
            ZonedDateTime.now(),
            false
            ,"LABELED"
        );
        order.addItem(
                1L,
                "productFamily",
                "bloodType",
                3,
                0,
                null,
                ZonedDateTime.now(),
                ZonedDateTime.now(),
                orderConfigService
        );

        // Execute
        var result = orderMapper.mapToDTO(order);

        // Verify
        assertEquals(order.getId(), result.id());
        assertEquals(order.getOrderNumber().getOrderNumber(), result.orderNumber());
        assertEquals(order.getOrderExternalId().getOrderExternalId(), result.externalId());
        assertEquals(order.getLocationCode(), result.locationCode());
        assertEquals(order.getShipmentType().getShipmentType(), result.shipmentType());
        assertEquals(order.getShippingMethod().getShippingMethod(), result.shippingMethod());
        assertEquals(order.getShippingCustomer().getCode(), result.shippingCustomerCode());
        assertEquals(order.getBillingCustomer().getCode(), result.billingCustomerCode());
        assertEquals(order.getDesiredShippingDate(), result.desiredShippingDate());
        assertEquals(order.getWillCallPickup(), result.willCallPickup());
        assertEquals(order.getPhoneNumber(), result.phoneNumber());
        assertEquals(order.getProductCategory().getProductCategory(), result.productCategory());
        assertEquals(order.getComments(), result.comments());
        assertEquals(order.getOrderStatus().getOrderStatus(), result.status());
        assertEquals(order.getOrderPriority().getDeliveryType(), result.priority());
        assertEquals(order.getCreateEmployeeId(), result.createEmployeeId());
        assertEquals(order.getCreateDate(), result.createDate());
        assertEquals(order.getModificationDate(), result.modificationDate());
        assertEquals(order.getDeleteDate(), result.deleteDate());
        assertEquals(order.getOrderItems().size(), result.orderItems().size());
        result.orderItems().forEach(orderItemDTO -> {
            var orderItem = order.getOrderItems().stream()
                .filter(i -> Objects.equals(i.getId(), orderItemDTO.id()))
                .findFirst()
                .orElseThrow();

            assertEquals(orderItem.getId(), orderItemDTO.id());
            assertEquals(orderItem.getOrderId().getOrderId(), orderItemDTO.orderId());
            assertEquals(orderItem.getProductFamily().getProductFamily(), orderItemDTO.productFamily());
            assertEquals(orderItem.getBloodType().getBloodType(), orderItemDTO.bloodType());
            assertEquals(orderItem.getQuantity(), orderItemDTO.quantity());
            assertEquals(orderItem.getComments(), orderItemDTO.comments());
            assertEquals(orderItem.getCreateDate(), orderItemDTO.createDate());
            assertEquals(orderItem.getModificationDate(), orderItemDTO.modificationDate());
        });
        assertFalse(result.canBeCompleted());
        assertFalse(result.backOrderCreationActive());
        assertEquals(order.getQuarantinedProducts(), result.quarantinedProducts());
        assertEquals(order.getLabelStatus().value(), result.labelStatus());
    }

    @Test
    void testMapToDomain() {
        // Setup
        var orderDTO = OrderDTO.builder()
            .id(1L)
            .orderNumber(1L)
            .externalId("externalId")
            .locationCode("locationCode")
            .shipmentType("shipmentType")
            .shippingMethod("shippingMethod")
            .shippingCustomerName("name")
            .shippingCustomerCode("code")
            .billingCustomerName("name")
            .billingCustomerCode("code")
            .desiredShippingDate(LocalDate.now())
            .willCallPickup(Boolean.TRUE)
            .phoneNumber("phoneNumber")
            .productCategory("productCategory")
            .comments("comments")
            .status("status")
            .priority("priority")
            .createEmployeeId("createEmployeeId")
            .createDate(ZonedDateTime.now())
            .modificationDate(ZonedDateTime.now())
            .deleteDate(ZonedDateTime.now())
            .orderItems(
                List.of(
                    OrderItemDTO.builder()
                        .id(1L)
                        .orderId(1L)
                        .productFamily("productFamily1")
                        .bloodType("bloodType1")
                        .quantity(3)
                        .comments("comments1")
                        .createDate(ZonedDateTime.now())
                        .modificationDate(ZonedDateTime.now())
                        .build(),
                    OrderItemDTO.builder()
                        .id(2L)
                        .orderId(1L)
                        .productFamily("productFamily2")
                        .bloodType("bloodType2")
                        .quantity(5)
                        .comments("comments2")
                        .createDate(ZonedDateTime.now())
                        .modificationDate(ZonedDateTime.now())
                        .build()
                )
            )
            .quarantinedProducts(true)
            .labelStatus("LABELED")
            .build();

        // Execute
        var result = orderMapper.mapToDomain(orderDTO);

        // Verify
        assertEquals(orderDTO.id(), result.getId());
        assertEquals(orderDTO.orderNumber(), result.getOrderNumber().getOrderNumber());
        assertEquals(orderDTO.externalId(), result.getOrderExternalId().getOrderExternalId());
        assertEquals(orderDTO.locationCode(), result.getLocationCode());
        assertEquals(orderDTO.shipmentType(), result.getShipmentType().getShipmentType());
        assertEquals(orderDTO.shippingMethod(), result.getShippingMethod().getShippingMethod());
        assertEquals(orderDTO.shippingCustomerCode(), result.getShippingCustomer().getCode());
        assertEquals(orderDTO.billingCustomerCode(), result.getBillingCustomer().getCode());
        assertEquals(orderDTO.desiredShippingDate(), result.getDesiredShippingDate());
        assertEquals(orderDTO.willCallPickup(), result.getWillCallPickup());
        assertEquals(orderDTO.phoneNumber(), result.getPhoneNumber());
        assertEquals(orderDTO.productCategory(), result.getProductCategory().getProductCategory());
        assertEquals(orderDTO.comments(), result.getComments());
        assertEquals(orderDTO.status(), result.getOrderStatus().getOrderStatus());
        assertEquals(orderDTO.priority(), result.getOrderPriority().getDeliveryType());
        assertEquals(orderDTO.createEmployeeId(), result.getCreateEmployeeId());
        assertNotNull(result.getCreateDate());
        assertEquals(orderDTO.modificationDate(), result.getModificationDate());
        assertEquals(orderDTO.deleteDate(), result.getDeleteDate());
        assertEquals(orderDTO.orderItems().size(), result.getOrderItems().size());
        result.getOrderItems().forEach(orderItem -> {
            var orderItemDTO = orderDTO.orderItems().stream()
                .filter(i -> Objects.equals(i.id(), orderItem.getId()))
                .findFirst()
                .orElseThrow();

            assertEquals(orderItemDTO.id(), orderItem.getId());
            assertEquals(orderItemDTO.orderId(), orderItem.getOrderId().getOrderId());
            assertEquals(orderItemDTO.productFamily(), orderItem.getProductFamily().getProductFamily());
            assertEquals(orderItemDTO.bloodType(), orderItem.getBloodType().getBloodType());
            assertEquals(orderItemDTO.quantity(), orderItem.getQuantity());
            assertEquals(orderItemDTO.comments(), orderItem.getComments());
            assertEquals(orderItemDTO.createDate(), orderItem.getCreateDate());
            assertEquals(orderItemDTO.modificationDate(), orderItem.getModificationDate());
        });
        assertEquals(orderDTO.quarantinedProducts(), result.getQuarantinedProducts());
        assertEquals(orderDTO.labelStatus(), result.getLabelStatus().value());
    }

}
