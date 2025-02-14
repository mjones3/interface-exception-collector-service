package com.arcone.biopro.distribution.order.unit.domain;

import com.arcone.biopro.distribution.order.domain.exception.DomainException;
import com.arcone.biopro.distribution.order.domain.model.CancelOrderCommand;
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
import org.junit.Assert;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    void testValidation() {

        Mockito.when(customerService.getCustomerByCode(Mockito.anyString())).thenReturn(Mono.just(CustomerDTO.builder()
                .name("Name")
                .code("Code")
            .build()));
        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("shipmentType","shipmentType"),"shipmentType",1,true)
            , new Lookup(new LookupId("shippingMethod","shippingMethod"),"shippingMethod",2,true)
            , new Lookup(new LookupId("productCategory","productCategory"),"productCategory",2,true)
            , new Lookup(new LookupId("status","status"),"status",2,true)
            , new Lookup(new LookupId("priority","priority"),"priority",2,true)
        ));


        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null), "orderNumber cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null), "locationCode cannot be null or blank");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null), "shipmentType cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", null, null, null, null, null, null, null, null, null, null, null, null, null), "shippingCustomer could not be found or it is null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", null, null, null, null, null, null, null, null, null, null, null, null), "billingCustomer could not be found or it is null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.parse("2025-01-01").toString(), null, null, null, null, null, null, null, null, null, null), "desiredShippingDate cannot be in the past");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now().toString(), TRUE, "phoneNumber", null, null, null, null, null, null, null, null), "productCategory cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now().toString(), TRUE, "phoneNumber", "productCategory", "comments", null, null, null, null, null, null), "orderStatus cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now().toString(), TRUE, "phoneNumber", "productCategory", "comments", "status", null, null, null, null, null), "orderPriority cannot be null");
        assertThrows(IllegalArgumentException.class, () -> new Order(customerService,lookupService, 1L, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", LocalDate.now().toString(), TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", null, null, null, null), "createEmployeeId cannot be null or blank");


        assertDoesNotThrow(() -> new Order(customerService,lookupService, null, 1L, "externalId", "locationCode", "shipmentType", "shippingMethod", "code", "code", null, TRUE, "phoneNumber", "productCategory", "comments", "status", "priority", "createEmployeeId", null, null, null));

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
            , null, null,null);

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

        assertThrows(DomainException.class, () -> order.completeOrder(new CompleteOrderCommand(1L,"employeeid","comments",Boolean.FALSE),lookupService,orderShipmentServiceMock) , "Order is already closed");
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


        assertDoesNotThrow(() -> order.completeOrder(new CompleteOrderCommand(1L,"close-employeeid","comments",Boolean.FALSE),lookupService,orderShipmentServiceMock));

        Assertions.assertNotNull(order.getCompleteDate());
        Assertions.assertEquals("comments",order.getCompleteComments());
        Assertions.assertEquals("close-employeeid",order.getCompleteEmployeeId());
        Assertions.assertEquals("COMPLETED",order.getOrderStatus().getOrderStatus());


    }

    @Test
    void shouldNotCreateBackOrderWhenConfigIsInactive(){

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("OPEN","OPEN"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)));

        Mockito.when(orderConfigService.findBackOrderConfiguration()).thenReturn(Mono.just(Boolean.FALSE));

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "OPEN", "OPEN", "123", "123","2025-01-31"
            , null, null, "OPEN", null, "OPEN", "OPEN", "CREATE_EMPLOYEE"
            , null, null, null);

        assertThrows(IllegalArgumentException.class, () -> order.createBackOrder("CREATE-EMPLOYEE-ID",customerService,lookupService,orderConfigService) , "Back Order cannot be created, configuration is not active");
    }

    @Test
    void shouldNotCreateBackOrderWhenThereIsNoRemainingItems(){

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("OPEN","OPEN"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)));

        Mockito.when(orderConfigService.findBackOrderConfiguration()).thenReturn(Mono.just(TRUE));

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "OPEN", "OPEN", "123", "123","2025-01-31"
            , null, null, "OPEN", null, "OPEN", "OPEN", "CREATE_EMPLOYEE"
            , null, null, null);

        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TYPE"));
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TYPE"));


        order.addItem(1L,"TYPE","TYPE",1,1,"", ZonedDateTime.now(),ZonedDateTime.now(),orderConfigService);

        assertThrows(IllegalArgumentException.class, () -> order.createBackOrder("CREATE-EMPLOYEE-ID",customerService,lookupService,orderConfigService) , "Back Order cannot be created, there is no remaining items");
    }


    @Test
    void shouldCreateBackOrder(){

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("OPEN","OPEN"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)));

        Mockito.when(orderConfigService.findBackOrderConfiguration()).thenReturn(Mono.just(TRUE));

        var desireShipDate = LocalDate.now();

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "OPEN", "OPEN", "123", "123",desireShipDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            , null, null, "OPEN", null, "OPEN", "OPEN", "CREATE_EMPLOYEE"
            , null, null, null);

        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TYPE"));
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TYPE"));


        order.addItem(1L,"TYPE","TYPE",10,5,"", ZonedDateTime.now(),ZonedDateTime.now(),orderConfigService);

        var backOrder = order.createBackOrder("CREATE-EMPLOYEE-ID",customerService,lookupService,orderConfigService);

        Assertions.assertNotNull(backOrder);
        Assertions.assertEquals("OPEN",backOrder.getOrderStatus().getOrderStatus());
        Assertions.assertNull(backOrder.getOrderNumber().getOrderNumber());
        Assertions.assertNull(backOrder.getId());
        Assertions.assertEquals(order.getOrderExternalId().getOrderExternalId(),backOrder.getOrderExternalId().getOrderExternalId());
        Assertions.assertEquals(1,backOrder.getOrderItems().size());
        Assertions.assertEquals(5,backOrder.getOrderItems().getFirst().getQuantity());
        Assertions.assertEquals("TYPE",backOrder.getOrderItems().getFirst().getBloodType().getBloodType());
        Assertions.assertEquals("TYPE",backOrder.getOrderItems().getFirst().getProductFamily().getProductFamily());
        Assertions.assertEquals(desireShipDate.format(DateTimeFormatter.ISO_LOCAL_DATE),backOrder.getDesiredShippingDate().toString());
        Assertions.assertNull(backOrder.getCompleteDate());
        Assertions.assertNull(backOrder.getCompleteComments());
        Assertions.assertNull(backOrder.getCompleteEmployeeId());
        Assertions.assertEquals("CREATE-EMPLOYEE-ID",backOrder.getCreateEmployeeId());
        Assertions.assertTrue(backOrder.isBackOrder());
    }

    @Test
    void shouldCreateBackOrderWithRemainingItems(){

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("OPEN","OPEN"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)
            , new Lookup(new LookupId("CATEGORY","CATEGORY"),"description",2,true)
        ));

        Mockito.when(orderConfigService.findBackOrderConfiguration()).thenReturn(Mono.just(TRUE));

        var desireShipDate = LocalDate.now();

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "OPEN", "OPEN", "123", "123",desireShipDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            , null, null, "CATEGORY", null, "OPEN", "OPEN", "CREATE_EMPLOYEE"
            , null, null, null);

        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.eq("CATEGORY"),Mockito.eq("FAMILY"))).thenReturn(Mono.just("TYPE"));
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.eq("FAMILY"),Mockito.eq("TYPE"))).thenReturn(Mono.just("TYPE"));

        order.addItem(1L,"FAMILY","TYPE",5,5,"", ZonedDateTime.now(),ZonedDateTime.now(),orderConfigService);

        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.eq("CATEGORY"),Mockito.eq("FAMILY2"))).thenReturn(Mono.just("TYPE2"));
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.eq("FAMILY2"),Mockito.eq("TYPE2"))).thenReturn(Mono.just("TYPE2"));

        order.addItem(2L,"FAMILY2","TYPE2",10,5,"", ZonedDateTime.now(),ZonedDateTime.now(),orderConfigService);

        var backOrder = order.createBackOrder("CREATE-EMPLOYEE-ID",customerService,lookupService,orderConfigService);

        Assertions.assertNotNull(backOrder);
        Assertions.assertEquals("OPEN",backOrder.getOrderStatus().getOrderStatus());
        Assertions.assertNull(backOrder.getOrderNumber().getOrderNumber());
        Assertions.assertNull(backOrder.getId());
        Assertions.assertEquals(order.getOrderExternalId().getOrderExternalId(),backOrder.getOrderExternalId().getOrderExternalId());
        Assertions.assertEquals(1,backOrder.getOrderItems().size());
        Assertions.assertEquals(5,backOrder.getOrderItems().getFirst().getQuantity());
        Assertions.assertEquals("TYPE2",backOrder.getOrderItems().getFirst().getBloodType().getBloodType());
        Assertions.assertEquals("FAMILY2",backOrder.getOrderItems().getFirst().getProductFamily().getProductFamily());
        Assertions.assertEquals(desireShipDate.format(DateTimeFormatter.ISO_LOCAL_DATE),backOrder.getDesiredShippingDate().toString());
        Assertions.assertNull(backOrder.getCompleteDate());
        Assertions.assertNull(backOrder.getCompleteComments());
        Assertions.assertNull(backOrder.getCompleteEmployeeId());
        Assertions.assertEquals("CREATE-EMPLOYEE-ID",backOrder.getCreateEmployeeId());
        Assertions.assertTrue(backOrder.isBackOrder());

    }

    @Test
    void shouldCreateBackOrderWithDesireShipDate(){

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("OPEN","OPEN"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)));

        Mockito.when(orderConfigService.findBackOrderConfiguration()).thenReturn(Mono.just(TRUE));

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "OPEN", "OPEN", "123", "123",LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            , null, null, "OPEN", null, "OPEN", "OPEN", "CREATE_EMPLOYEE"
            , null, null, null);

        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TYPE"));
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TYPE"));


        order.addItem(1L,"TYPE","TYPE",10,5,"", ZonedDateTime.now(),ZonedDateTime.now(),orderConfigService);

        var backOrder = order.createBackOrder("CREATE-EMPLOYEE-ID",customerService,lookupService,orderConfigService);

        Assertions.assertNotNull(backOrder);
        Assertions.assertEquals(LocalDate.now(),backOrder.getDesiredShippingDate());
        Assertions.assertTrue(backOrder.isBackOrder());
    }

    @Test
    void shouldCreateBackOrderWithDesireShipDateAsNull(){

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("OPEN","OPEN"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)));

        Mockito.when(orderConfigService.findBackOrderConfiguration()).thenReturn(Mono.just(TRUE));

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "OPEN", "OPEN", "123", "123",LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
            , null, null, "OPEN", null, "OPEN", "OPEN", "CREATE_EMPLOYEE"
            , null, null, null);

        Mockito.when(orderConfigService.findProductFamilyByCategory(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TYPE"));
        Mockito.when(orderConfigService.findBloodTypeByFamilyAndType(Mockito.anyString(),Mockito.anyString())).thenReturn(Mono.just("TYPE"));


        order.addItem(1L,"TYPE","TYPE",10,5,"", ZonedDateTime.now(),ZonedDateTime.now(),orderConfigService);

        var backOrder = order.createBackOrder("CREATE-EMPLOYEE-ID",customerService,lookupService,orderConfigService);

        Assertions.assertNotNull(backOrder);
        Assertions.assertNull(backOrder.getDesiredShippingDate());
        Assertions.assertTrue(backOrder.isBackOrder());
    }

    @Test
    void shouldNotCancelWhenIsCancelled(){

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("CANCELLED","CANCELLED"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)));

        Mockito.when(orderConfigService.findBackOrderConfiguration()).thenReturn(Mono.just(TRUE));

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "CANCELLED", "CANCELLED", "123", "123",LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
            , null, null, "CANCELLED", null, "CANCELLED", "CANCELLED", "CREATE_EMPLOYEE"
            , null, null, null);

        try{
            order.cancel(new CancelOrderCommand("1233","employee-id","reason","2025-01-01 11:09:55"), List.of(order));
            Assertions.fail();
        }catch (Exception e){
            Assertions.assertEquals(DomainException.class,e.getClass());

            Assertions.assertEquals("Order is already cancelled",((DomainException) e).getUseCaseMessageType().getMessage());
        }

    }

    @Test
    void shouldNotCancelWhenIsNotOpen(){

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("CANCELLED","CANCELLED"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)));

        Mockito.when(orderConfigService.findBackOrderConfiguration()).thenReturn(Mono.just(TRUE));

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "COMPLETED", "COMPLETED", "123", "123",LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
            , null, null, "COMPLETED", null, "COMPLETED", "COMPLETED", "CREATE_EMPLOYEE"
            , null, null, null);

        try{
            order.cancel(new CancelOrderCommand("1233","employee-id","reason","2025-01-01 11:09:55") , List.of(order));
            Assertions.fail();
        }catch (Exception e){
            Assertions.assertEquals(DomainException.class,e.getClass());

            Assertions.assertEquals("Order is not open and cannot be cancelled",((DomainException) e).getUseCaseMessageType().getMessage());
        }

    }

    @Test
    void shouldCancelOrder(){

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("OPEN","OPEN"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)));

        Mockito.when(orderConfigService.findBackOrderConfiguration()).thenReturn(Mono.just(TRUE));

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "OPEN", "OPEN", "123", "123",LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
            , null, null, "COMPLETED", null, "OPEN", "OPEN", "CREATE_EMPLOYEE"
            , null, null, null);

        var response = order.cancel(new CancelOrderCommand("1233","employee-id","reason","2025-01-01 11:09:55"), List.of(order));

        Assertions.assertNotNull(response.getCancelDate());
        Assertions.assertEquals("reason",response.getCancelReason());
        Assertions.assertEquals("employee-id",response.getCancelEmployeeId());
        Assertions.assertEquals("CANCELLED",response.getOrderStatus().getOrderStatus());

    }

    @Test
    void shouldNotCancelWhenIsBackOrderIsCancelled(){

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("CANCELLED","CANCELLED"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)));

        Mockito.when(orderConfigService.findBackOrderConfiguration()).thenReturn(Mono.just(TRUE));

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "COMPLETED", "COMPLETED", "123", "123",LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
            , null, null, "COMPLETED", null, "COMPLETED", "COMPLETED", "CREATE_EMPLOYEE"
            , null, null, null);

        var backOrder = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "CANCELLED", "CANCELLED", "123", "123",LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
            , null, null, "CANCELLED", null, "CANCELLED", "CANCELLED", "CREATE_EMPLOYEE"
            , null, null, null);

        backOrder.setBackOrder(TRUE);

        try{
            order.cancel(new CancelOrderCommand("1233","employee-id","reason","2025-01-01 11:09:55"), List.of(order,backOrder));
            Assertions.fail();
        }catch (Exception e){
            Assertions.assertEquals(DomainException.class,e.getClass());

            Assertions.assertEquals("Order is already cancelled",((DomainException) e).getUseCaseMessageType().getMessage());
        }

    }

    @Test
    void shouldNotCancelWhenIsBackOrderIsNotOpen(){

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("IN_PROGRESS","IN_PROGRESS"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)
            , new Lookup(new LookupId("CANCELLED","CANCELLED"),"description",2,true)
        ));

        Mockito.when(orderConfigService.findBackOrderConfiguration()).thenReturn(Mono.just(TRUE));

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "COMPLETED", "COMPLETED", "123", "123",LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
            , null, null, "COMPLETED", null, "COMPLETED", "COMPLETED", "CREATE_EMPLOYEE"
            , null, null, null);

        var backOrder = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "IN_PROGRESS", "CANCELLED", "123", "123",LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
            , null, null, "CANCELLED", null, "IN_PROGRESS", "IN_PROGRESS", "CREATE_EMPLOYEE"
            , null, null, null);

        backOrder.setBackOrder(TRUE);

        try{
            order.cancel(new CancelOrderCommand("1233","employee-id","reason","2025-01-01 11:09:55"), List.of(order,backOrder));
            Assertions.fail();
        }catch (Exception e){
            Assertions.assertEquals(DomainException.class,e.getClass());
            Assertions.assertEquals("Order is not open and cannot be cancelled",((DomainException) e).getUseCaseMessageType().getMessage());
        }

    }

    @Test
    void shouldNotCancelWhenThereIsNotOrderToBeCancelled(){

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("IN_PROGRESS","IN_PROGRESS"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)
            , new Lookup(new LookupId("CANCELLED","CANCELLED"),"description",2,true)
        ));

        Mockito.when(orderConfigService.findBackOrderConfiguration()).thenReturn(Mono.just(TRUE));

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "COMPLETED", "COMPLETED", "123", "123",LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
            , null, null, "COMPLETED", null, "COMPLETED", "COMPLETED", "CREATE_EMPLOYEE"
            , null, null, null);

        try{
            order.cancel(new CancelOrderCommand("1233","employee-id","reason","2025-01-01 11:09:55"), null);
            Assertions.fail();
        }catch (Exception e){
            Assertions.assertEquals(DomainException.class,e.getClass());
            Assertions.assertEquals("There is no order to be cancelled",((DomainException) e).getUseCaseMessageType().getMessage());
        }

    }

    @Test
    void shouldCancelOrderBackOrder(){

        Mockito.when(lookupService.findAllByType(Mockito.anyString())).thenReturn(Flux.just(new Lookup(new LookupId("OPEN","OPEN"),"description",1,true)
            , new Lookup(new LookupId("COMPLETED","COMPLETED"),"description",2,true)));

        Mockito.when(orderConfigService.findBackOrderConfiguration()).thenReturn(Mono.just(TRUE));

        var order = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "OPEN", "OPEN", "123", "123",LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
            , null, null, "COMPLETED", null, "COMPLETED", "OPEN", "CREATE_EMPLOYEE"
            , null, null, null);


        var backOrder = new Order(customerService, lookupService, 1L, 123L, "EXT", "123"
            , "OPEN", "OPEN", "123", "123",LocalDate.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE)
            , null, null, "COMPLETED", null, "OPEN", "OPEN", "CREATE_EMPLOYEE"
            , null, null, null);

        backOrder.setBackOrder(TRUE);


        var response = order.cancel(new CancelOrderCommand("1233","employee-id","reason","2025-01-01 11:09:55"), List.of(order,backOrder));

        Assertions.assertNotNull(response.getCancelDate());
        Assertions.assertEquals("reason",response.getCancelReason());
        Assertions.assertEquals("employee-id",response.getCancelEmployeeId());
        Assertions.assertTrue(response.isBackOrder());
        Assertions.assertEquals("CANCELLED",response.getOrderStatus().getOrderStatus());

    }
}
