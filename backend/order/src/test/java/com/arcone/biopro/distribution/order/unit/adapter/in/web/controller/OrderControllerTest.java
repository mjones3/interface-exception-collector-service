package com.arcone.biopro.distribution.order.unit.adapter.in.web.controller;

import com.arcone.biopro.distribution.order.adapter.in.web.controller.OrderController;
import com.arcone.biopro.distribution.order.application.dto.UseCaseResponseDTO;
import com.arcone.biopro.distribution.order.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.order.application.mapper.OrderItemMapper;
import com.arcone.biopro.distribution.order.application.mapper.OrderMapper;
import com.arcone.biopro.distribution.order.domain.model.Order;
import com.arcone.biopro.distribution.order.domain.model.vo.LabelStatus;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomer;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderExternalId;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderLocation;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderNumber;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderPriority;
import com.arcone.biopro.distribution.order.domain.model.vo.OrderStatus;
import com.arcone.biopro.distribution.order.domain.model.vo.ProductCategory;
import com.arcone.biopro.distribution.order.domain.model.vo.ShipmentType;
import com.arcone.biopro.distribution.order.domain.model.vo.ShippingMethod;
import com.arcone.biopro.distribution.order.domain.repository.LocationRepository;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
import com.arcone.biopro.distribution.order.domain.service.OrderService;
import com.arcone.biopro.distribution.order.domain.service.OrderShipmentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyLong;

@SpringJUnitConfig(classes = { OrderController.class, OrderMapper.class, OrderItemMapper.class, OrderShipmentService.class })
class OrderControllerTest {

    @Autowired
    OrderController orderController;

    @Autowired
    OrderMapper orderMapper;
    @MockBean
    OrderService orderService;
    @MockBean
    CustomerService customerService;
    @MockBean
    OrderConfigService orderConfigService;
    @MockBean
    LookupService lookupService;
    @MockBean
    OrderShipmentService orderShipmentService;
    @MockBean
    LocationRepository locationRepository;

    @Test
    public void shouldFindOrderById(){

        Order order = Mockito.mock(Order.class);
        Mockito.when(order.getOrderNumber()).thenReturn(new OrderNumber(1L));


        var orderLocation = Mockito.mock(OrderLocation.class);
        Mockito.when(orderLocation.getCode()).thenReturn("LOCATION_CODE");
        Mockito.when(order.getLocationFrom()).thenReturn(orderLocation);


        OrderExternalId orderExternalId = Mockito.mock(OrderExternalId.class);
        Mockito.when(orderExternalId.getOrderExternalId()).thenReturn("orderExternalId");
        Mockito.when(order.getOrderExternalId()).thenReturn(orderExternalId);

        ShipmentType shipmentType = Mockito.mock(ShipmentType.class);
        Mockito.when(shipmentType.getShipmentType()).thenReturn("shipmentType");
        Mockito.when(order.getShipmentType()).thenReturn(shipmentType);

        Mockito.when(order.getShippingMethod()).thenReturn(Mockito.mock(ShippingMethod.class));

        Mockito.when(order.getProductCategory()).thenReturn(Mockito.mock(ProductCategory.class));

        Mockito.when(order.getOrderStatus()).thenReturn(Mockito.mock(OrderStatus.class));
        Mockito.when(order.getOrderPriority()).thenReturn(Mockito.mock(OrderPriority.class));

        Mockito.when(order.getLabelStatus()).thenReturn(LabelStatus.LABELED());

        OrderCustomer customer = Mockito.mock(OrderCustomer.class);
        Mockito.when(order.getBillingCustomer()).thenReturn(customer);
        Mockito.when(order.getShippingCustomer()).thenReturn(customer);

        var useCaseResponse = Mockito.mock(UseCaseResponseDTO.class);
        Mockito.when(useCaseResponse.data()).thenReturn(order);

        Mockito.when(orderService.findUseCaseResponseById(anyLong())).thenReturn(Mono.just(useCaseResponse));

        StepVerifier.create(orderController.findOrderById(1L))
            .consumeNextWith(orderReportDTO -> {
                    Assertions.assertEquals(1L,  orderReportDTO.data().orderNumber());
                    Assertions.assertEquals("orderExternalId",  orderReportDTO.data().externalId());
                }
            )
            .verifyComplete();
    }

    @Test
    public void shouldNotFindById(){

        Mockito.when(orderService.findUseCaseResponseById(anyLong())).thenReturn(Mono.error(new DomainNotFoundForKeyException("TEST")));

        StepVerifier.create(orderController.findOrderById(2L))
            .expectError(DomainNotFoundForKeyException.class)
            .verify();

    }

}
