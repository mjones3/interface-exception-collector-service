package com.arcone.biopro.distribution.orderservice.domain.model;

import com.arcone.biopro.distribution.orderservice.domain.model.vo.*;
import com.arcone.biopro.distribution.orderservice.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.orderservice.domain.service.CustomerService;
import com.arcone.biopro.distribution.orderservice.domain.service.LookupService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;

@Getter
@EqualsAndHashCode
@ToString
public class Order implements Validatable {

    private Long id;
    private OrderNumber orderNumber;
    private OrderExternalId orderExternalId;
    private String locationCode;
    private ShipmentType shipmentType;
    private ShippingMethod shippingMethod;
    private OrderCustomer shippingCustomer;
    private OrderCustomer billingCustomer;
    private LocalDate desiredShippingDate;
    private Boolean willCallPickup;
    private String phoneNumber;
    private ProductCategory productCategory;
    private String comments;
    private OrderStatus orderStatus;
    private OrderPriority orderPriority;
    private String createEmployeeId;
    private ZonedDateTime createDate;
    private ZonedDateTime modificationDate;
    private ZonedDateTime deleteDate;
    private List<OrderItem> orderItems;

    public Order(
        CustomerService customerService,
        LookupService lookupService,
        Long id,
        Long orderNumber,
        String externalId,
        String locationCode,
        String shipmentType,
        String shippingMethod,
        String shippingCustomerCode,
        String billingCustomerCode,
        LocalDate desiredShippingDate,
        Boolean willCallPickup,
        String phoneNumber,
        String productCategory,
        String comments,
        String orderStatus,
        String orderPriority,
        String createEmployeeId,
        ZonedDateTime createDate,
        ZonedDateTime modificationDate,
        ZonedDateTime deleteDate,
        List<OrderItem> orderItems
    ) {
        this.id = id;
        this.orderNumber = new OrderNumber(orderNumber);
        this.orderExternalId = ofNullable(externalId)
            .map(OrderExternalId::new)
            .orElse(null);
        this.locationCode = locationCode;
        this.shipmentType = new ShipmentType(shipmentType,lookupService);
        this.shippingMethod = new ShippingMethod(shippingMethod,lookupService);
        this.shippingCustomer = ofNullable(shippingCustomerCode)
            .map(customerService::getCustomerByCode)
            .map(Mono::block)
            .map(customerDTO -> new OrderCustomer(customerDTO.code(), customerDTO.name()))
            .orElse(null);
        this.billingCustomer = ofNullable(billingCustomerCode)
            .map(customerService::getCustomerByCode)
            .map(Mono::block)
            .map(customerDTO -> new OrderCustomer(customerDTO.code(), customerDTO.name()))
            .orElse(null);
        this.desiredShippingDate = desiredShippingDate;
        this.willCallPickup = willCallPickup;
        this.phoneNumber = phoneNumber;
        this.productCategory = new ProductCategory(productCategory,lookupService);
        this.comments = comments;
        this.orderStatus = new OrderStatus(orderStatus,lookupService);
        this.orderPriority = new OrderPriority(orderPriority,lookupService);
        this.createEmployeeId = createEmployeeId;
        this.createDate = createDate;
        this.modificationDate = modificationDate;
        this.deleteDate = deleteDate;
        this.orderItems = orderItems;

        this.checkValid();
    }

    @Override
    public void checkValid() {
        if (this.orderNumber == null) {
            throw new IllegalArgumentException("orderNumber cannot be null");
        }
        if (this.locationCode == null || this.locationCode.isBlank()) {
            throw new IllegalArgumentException("locationCode cannot be null or blank");
        }
        if (this.shipmentType == null) {
            throw new IllegalArgumentException("shipmentType cannot be null");
        }
        if (this.shippingMethod == null) {
            throw new IllegalArgumentException("shippingMethod cannot be null");
        }
        if (this.shippingCustomer == null) {
            throw new IllegalArgumentException("shippingCustomer could not be found or it is null");
        }
        if (this.billingCustomer == null) {
            throw new IllegalArgumentException("billingCustomer could not be found or it is null");
        }
        if (this.desiredShippingDate == null) {
            throw new IllegalArgumentException("desiredShippingDate cannot be null");
        }
        if (this.productCategory == null) {
            throw new IllegalArgumentException("productCategory cannot be null");
        }
        if (this.orderStatus == null) {
            throw new IllegalArgumentException("orderStatus cannot be null");
        }
        if (this.orderPriority == null) {
            throw new IllegalArgumentException("orderPriority cannot be null");
        }
        if (this.createEmployeeId == null || this.createEmployeeId.isBlank()) {
            throw new IllegalArgumentException("createEmployeeId cannot be null or blank");
        }
        if (this.orderItems == null || this.orderItems.isEmpty()) {
            throw new IllegalArgumentException("orderItems cannot be null or empty");
        }
    }

    public Mono<Boolean> exists(final OrderRepository orderRepository) {
        return ofNullable(this.id)
            .map(id -> orderRepository.existsById(id, TRUE))
            .orElseGet(() -> Mono.just(FALSE));
    }

}
