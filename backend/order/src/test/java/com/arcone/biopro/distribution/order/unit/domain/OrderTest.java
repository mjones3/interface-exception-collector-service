package com.arcone.biopro.distribution.order.unit.domain;

import com.arcone.biopro.distribution.order.domain.model.CompleteOrderCommand;
import com.arcone.biopro.distribution.order.domain.model.Lookup;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.OrderShipment;
import com.arcone.biopro.distribution.order.domain.model.vo.LookupId;
import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
import com.arcone.biopro.distribution.order.domain.service.OrderShipmentService;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SpringJUnitConfig
class OrderTest {

    @MockBean
    OrderRepository orderRepository;
    @MockBean
    CustomerService customerService;
    @MockBean
    OrderConfigService orderConfigService;
    @MockBean
    LookupService lookupService;

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
    void testValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null), "orderNumber cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null), "locationCode cannot be null or blank");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null), "shipmentType cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", null, null, null, null, null, null, null, null, null, null, null, null, null, null), "shippingMethod cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", null, null, null, null, null, null, null, null, null, null, null, null, null), "shippingCustomer could not be found or it is null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", null, null, null, null, null, null, null, null, null, null, null, null), "billingCustomer could not be found or it is null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", null, null, null, null, null, null, null, null, null, null, null), "desiredShippingDate cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.parse("2025-01-01").toString(), null, null, null, null, null, null, null, null, null, null), "desiredShippingDate cannot be in the past");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now().toString(), TRUE, "phoneNumber", null, null, null, null, null, null, null, null), "productCategory cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now().toString(), TRUE, "phoneNumber", "productCategory", "comments", null, null, null, null, null, null), "orderStatus cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now().toString(), TRUE, "phoneNumber", "productCategory", "comments", "status", null, null, null, null, null), "orderPriority cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now().toString(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", null, null, null, null), "createEmployeeId cannot be null or blank");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now().toString(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", "createEmployeeId", null, null, null), "orderItems cannot be null or empty");
        assertDoesNotThrow(() -> new Order(customerService,lookupService, null, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now().toString(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", "createEmployeeId", null, null, null));
    }

    @Test
    @Disabled("Disabled until Manual Order Creation is implemented")
    void testExists() {
        var order = new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now().toString(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", "createEmployeeId", null, null, null);

        given(orderRepository.existsById(order.getId(), TRUE))
            .willReturn(Mono.just(true));

        StepVerifier.create(order.exists(orderRepository))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void testCanCompleteOrder() {

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("IN_PROGRESS","IN_PROGRESS"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)
            , new Lookup(new LookupId("TYPE","TYPE"),"description",3,true)));

        var orderShipmentServiceMock = Mockito.mock(OrderShipmentService.class);

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "TYPE", "TYPE", "123", "123","2025-01-31"
            , null, null, "TYPE", null, "TYPE", "TYPE", "CREATE_EMPLOYEE"
            , null, null, null);

        Assertions.assertFalse(order.canBeCompleted(orderShipmentServiceMock));

        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TEST"));
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TEST"));


        var order2 = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "TYPE", "TYPE", "123", "123","2025-01-31"
            , null, null, "TYPE", null, "IN_PROGRESS", "TYPE", "CREATE_EMPLOYEE"
            , null, null, null);

        order2.addItem(1L,"TYPE","TYPE",10,1,"", ZonedDateTime.now(),ZonedDateTime.now(),orderConfigService);



        var orderShipment = Mockito.mock(OrderShipment.class);
        Mockito.when(orderShipment.getShipmentStatus()).thenReturn("COMPLETED");

        Mockito.when(orderShipmentServiceMock.findOneByOrderId(Mockito.anyLong())).thenReturn(Mono.just(orderShipment));


        Assertions.assertTrue(order2.canBeCompleted(orderShipmentServiceMock));


        Mockito.when(orderShipment.getShipmentStatus()).thenReturn("OPEN");

        Mockito.when(orderShipmentServiceMock.findOneByOrderId(Mockito.anyLong())).thenReturn(Mono.just(orderShipment));


        Assertions.assertFalse(order2.canBeCompleted(orderShipmentServiceMock));

    }

    @Test
    void shouldNotCompleteOrderWhenItsCompleted(){

        var lookupMock = Mockito.mock(Lookup.class);
        var lookupId = Mockito.mock(LookupId.class);
        Mockito.when(lookupMock.getId()).thenReturn(lookupId);
        Mockito.when(lookupId.getOptionValue()).thenReturn("COMPLETED");

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(lookupMock));

        var orderShipmentServiceMock = Mockito.mock(OrderShipmentService.class);

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "COMPLETED", "COMPLETED", "123", "123","2025-01-31"
            , null, null, "COMPLETED", null, "COMPLETED", "COMPLETED", "CREATE_EMPLOYEE"
            , null, null, null);

        assertThrows(IllegalArgumentException.class, () -> order.completeOrder(new CompleteOrderCommand(1L,"employeeid","comments"),lookupService,orderShipmentServiceMock) , "Order is already closed");

    }

    @Test
    void shouldCompleteOrder(){

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("IN_PROGRESS","IN_PROGRESS"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)));

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "IN_PROGRESS", "IN_PROGRESS", "123", "123","2025-01-31"
            , null, null, "IN_PROGRESS", null, "IN_PROGRESS", "IN_PROGRESS", "CREATE_EMPLOYEE"
            , null, null, null);

        var orderShipmentServiceMock = Mockito.mock(OrderShipmentService.class);

        var orderShipment = Mockito.mock(OrderShipment.class);
        Mockito.when(orderShipment.getShipmentStatus()).thenReturn("COMPLETED");

        Mockito.when(orderShipmentServiceMock.findOneByOrderId(Mockito.anyLong())).thenReturn(Mono.just(orderShipment));


        assertDoesNotThrow(() -> order.completeOrder(new CompleteOrderCommand(1L,"close-employeeid","comments"),lookupService,orderShipmentServiceMock));

        Assertions.assertNotNull(order.getCompleteDate());
        Assertions.assertEquals("comments",order.getCompleteComments());
        Assertions.assertEquals("close-employeeid",order.getCompleteEmployeeId());


    }
}
