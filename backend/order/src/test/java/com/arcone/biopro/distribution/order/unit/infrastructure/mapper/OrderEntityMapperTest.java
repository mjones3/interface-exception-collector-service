package com.arcone.biopro.distribution.order.unit.infrastructure.mapper;

import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderEntityMapper;
import com.arcone.biopro.distribution.order.infrastructure.mapper.OrderItemEntityMapper;
import com.arcone.biopro.distribution.order.infrastructure.persistence.OrderEntity;
import com.arcone.biopro.distribution.order.infrastructure.persistence.OrderItemEntity;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringJUnitConfig(classes = { OrderEntityMapper.class, OrderItemEntityMapper.class})
class OrderEntityMapperTest {

    @Autowired
    OrderEntityMapper mapper;

    @MockBean
    CustomerService customerService;

    @MockBean
    OrderConfigService orderConfigService;
    @MockBean
    LookupService lookupService;

    @MockBean
    LocationRepository locationRepository;

    @BeforeEach
    void beforeEach() {
        given(customerService.getCustomerByCode(anyString()))
            .willReturn(Mono.just(
                CustomerDTO.builder()
                    .code("code")
                    .name("name")
                    .build()
            ));
    }

    @Test
    @Disabled("Disabled until Manual Order Creation is implemented")
    void testMapToEntity() {
        var order = new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode"
            , "shipmentType", "shippingMethod"
            , "code", "code", LocalDate.now().toString(), TRUE, "phoneNumber"
            , "productCategory", "comments", "status"
            , "priority", "createEmployeeId", "2023-04-25 20:09:01", ZonedDateTime.now(), null,null,"LABELED",locationRepository);
        order.setTransactionId(java.util.UUID.randomUUID());

        var orderEntity = mapper.mapToEntity(order);

        assertEquals(order.getId(), orderEntity.getId());
        assertEquals(order.getOrderNumber().getOrderNumber(), orderEntity.getOrderNumber());
        assertEquals(order.getOrderExternalId().getOrderExternalId(), orderEntity.getExternalId());
        assertEquals(order.getLocationFrom().getCode(), orderEntity.getLocationCode());
        assertEquals(order.getShipmentType().getShipmentType(), orderEntity.getShipmentType());
        assertEquals(order.getShippingMethod().getShippingMethod(), orderEntity.getShippingMethod());
        assertEquals(order.getShippingCustomer().getCode(), orderEntity.getShippingCustomerCode());
        assertEquals(order.getBillingCustomer().getCode(), orderEntity.getBillingCustomerCode());
        assertEquals(order.getDesiredShippingDate(), orderEntity.getDesiredShippingDate());
        assertEquals(order.getWillCallPickup(), orderEntity.getWillCallPickup());
        assertEquals(order.getPhoneNumber(), orderEntity.getPhoneNumber());
        assertEquals(order.getProductCategory().getProductCategory(), orderEntity.getProductCategory());
        assertEquals(order.getComments(), orderEntity.getComments());
        assertEquals(order.getOrderStatus().getOrderStatus(), orderEntity.getStatus());
        assertEquals(order.getOrderPriority().getPriority(), orderEntity.getPriority());
        assertEquals(order.getOrderPriority().getDeliveryType(), orderEntity.getDeliveryType());
        assertEquals(order.getCreateEmployeeId(), orderEntity.getCreateEmployeeId());
        assertEquals(order.getCreateDate(), orderEntity.getCreateDate());
        assertEquals(order.getModificationDate(), orderEntity.getModificationDate());
        assertEquals(order.getDeleteDate(), orderEntity.getDeleteDate());
        assertEquals(order.getTransactionId(), orderEntity.getTransactionId());
    }

    @Test
    @Disabled("Disabled until Manual Order Creation is implemented")
    void testMapToDomain() {
        var orderItemEntities = List.of(
            OrderItemEntity.builder()
                .id(1L)
                .orderId(1L)
                .productFamily("productFamily1")
                .bloodType("bloodType1")
                .quantity(3)
                .comments("comments1")
                .createDate(ZonedDateTime.now())
                .modificationDate(ZonedDateTime.now())
                .build(),
            OrderItemEntity.builder()
                .id(2L)
                .orderId(1L)
                .productFamily("productFamily2")
                .bloodType("bloodType2")
                .quantity(5)
                .comments("comments2")
                .createDate(ZonedDateTime.now())
                .modificationDate(ZonedDateTime.now())
                .build()
        );
        var orderEntity = OrderEntity.builder()
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
            .priority(1)
            .deliveryType("deliveryType")
            .createEmployeeId("createEmployeeId")
            .createDate(ZonedDateTime.now())
            .modificationDate(ZonedDateTime.now())
            .deleteDate(ZonedDateTime.now())
            .transactionId(UUID.randomUUID())
            .build();

        var domain = mapper.mapToDomain(orderEntity, orderItemEntities);

        // Order
        assertEquals(orderEntity.getId(), domain.getId());
        assertEquals(orderEntity.getOrderNumber(), domain.getOrderNumber().getOrderNumber());
        assertEquals(orderEntity.getExternalId(), domain.getOrderExternalId().getOrderExternalId());
        assertEquals(orderEntity.getLocationCode(), domain.getLocationFrom().getCode());
        assertEquals(orderEntity.getShipmentType(), domain.getShipmentType().getShipmentType());
        assertEquals(orderEntity.getShippingMethod(), domain.getShippingMethod().getShippingMethod());
        assertEquals(orderEntity.getShippingCustomerCode(), domain.getShippingCustomer().getCode());
        assertEquals(orderEntity.getBillingCustomerCode(), domain.getBillingCustomer().getCode());
        assertEquals(orderEntity.getDesiredShippingDate(), domain.getDesiredShippingDate());
        assertEquals(orderEntity.getWillCallPickup(), domain.getWillCallPickup());
        assertEquals(orderEntity.getPhoneNumber(), domain.getPhoneNumber());
        assertEquals(orderEntity.getProductCategory(), domain.getProductCategory().getProductCategory());
        assertEquals(orderEntity.getComments(), domain.getComments());
        assertEquals(orderEntity.getStatus(), domain.getOrderStatus().getOrderStatus());
        assertEquals(orderEntity.getPriority(), domain.getOrderPriority().getPriority());
        assertEquals(orderEntity.getDeliveryType(), domain.getOrderPriority().getDeliveryType());
        assertEquals(orderEntity.getCreateEmployeeId(), domain.getCreateEmployeeId());
        assertEquals(orderEntity.getCreateDate(), domain.getCreateDate());
        assertEquals(orderEntity.getModificationDate(), domain.getModificationDate());
        assertEquals(orderEntity.getDeleteDate(), domain.getDeleteDate());
        assertEquals(orderEntity.getTransactionId(), domain.getTransactionId());

        // OrderItem
        assertEquals(orderItemEntities.size(), domain.getOrderItems().size());
        domain.getOrderItems().forEach(orderItem -> {
            var orderItemDTO = orderItemEntities.stream()
                .filter(i -> Objects.equals(i.getId(), orderItem.getId()))
                .findFirst()
                .orElseThrow();

            assertEquals(orderItemDTO.getId(), orderItem.getId());
            assertEquals(orderItemDTO.getOrderId(), orderItem.getOrderId().getOrderId());
            assertEquals(orderItemDTO.getProductFamily(), orderItem.getProductFamily().getProductFamily());
            assertEquals(orderItemDTO.getBloodType(), orderItem.getBloodType().getBloodType());
            assertEquals(orderItemDTO.getQuantity(), orderItem.getQuantity());
            assertEquals(orderItemDTO.getComments(), orderItem.getComments());
            assertEquals(orderItemDTO.getCreateDate(), orderItem.getCreateDate());
            assertEquals(orderItemDTO.getModificationDate(), orderItem.getModificationDate());
        });
    }

}
