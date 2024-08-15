package com.arcone.biopro.distribution.order.domain.model;

import com.arcone.biopro.distribution.order.domain.repository.OrderRepository;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.domain.service.LookupService;
import com.arcone.biopro.distribution.order.domain.service.OrderConfigService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Optional.ofNullable;

@Getter
@EqualsAndHashCode
@ToString
public class Order implements Validatable {

    private Long id;
    private com.arcone.biopro.distribution.order.domain.model.vo.OrderNumber orderNumber;
    private com.arcone.biopro.distribution.order.domain.model.vo.OrderExternalId orderExternalId;
    private String locationCode;
    private com.arcone.biopro.distribution.order.domain.model.vo.ShipmentType shipmentType;
    private com.arcone.biopro.distribution.order.domain.model.vo.ShippingMethod shippingMethod;
    private com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomer shippingCustomer;
    private com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomer billingCustomer;
    private LocalDate desiredShippingDate;
    private Boolean willCallPickup;
    private String phoneNumber;
    private com.arcone.biopro.distribution.order.domain.model.vo.ProductCategory productCategory;
    private String comments;
    private com.arcone.biopro.distribution.order.domain.model.vo.OrderStatus orderStatus;
    private com.arcone.biopro.distribution.order.domain.model.vo.OrderPriority orderPriority;
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
        String desiredShippingDate,
        Boolean willCallPickup,
        String phoneNumber,
        String productCategory,
        String comments,
        String orderStatus,
        String orderPriority,
        String createEmployeeId,
        ZonedDateTime createDate,
        ZonedDateTime modificationDate,
        ZonedDateTime deleteDate
    ) {
        this.id = id;
        this.orderNumber = new com.arcone.biopro.distribution.order.domain.model.vo.OrderNumber(orderNumber);
        this.orderExternalId = new com.arcone.biopro.distribution.order.domain.model.vo.OrderExternalId(externalId);
        this.locationCode = locationCode;
        this.shipmentType = new com.arcone.biopro.distribution.order.domain.model.vo.ShipmentType(shipmentType, lookupService);
        this.shippingMethod = new com.arcone.biopro.distribution.order.domain.model.vo.ShippingMethod(shippingMethod, lookupService);
        this.shippingCustomer = new com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomer(shippingCustomerCode, customerService);
        this.billingCustomer = new com.arcone.biopro.distribution.order.domain.model.vo.OrderCustomer(billingCustomerCode, customerService);
        try {
            this.desiredShippingDate = LocalDate.parse(desiredShippingDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("desiredShippingDate is invalid");
        }
        this.willCallPickup = willCallPickup;
        this.phoneNumber = phoneNumber;
        this.productCategory = new com.arcone.biopro.distribution.order.domain.model.vo.ProductCategory(productCategory, lookupService);
        this.comments = comments;
        this.orderStatus = new com.arcone.biopro.distribution.order.domain.model.vo.OrderStatus(orderStatus, lookupService);
        this.orderPriority = new com.arcone.biopro.distribution.order.domain.model.vo.OrderPriority(orderPriority, lookupService);
        this.createEmployeeId = createEmployeeId;
        this.createDate = createDate;
        this.modificationDate = modificationDate;
        this.deleteDate = deleteDate;

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
        if (this.desiredShippingDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("desiredShippingDate cannot be in the past");
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
    }

    public void addItem(Long id, String productFamily, String bloodType, Integer quantity, String comments
        , ZonedDateTime createDate, ZonedDateTime modificationDate, OrderConfigService orderConfigService) {

        if (this.orderItems == null) {
            this.orderItems = new ArrayList<>();
        }

        this.orderItems.add(new OrderItem(id, this.id, productFamily, bloodType, quantity, comments, createDate
            , modificationDate, this.getProductCategory().getProductCategory(), orderConfigService));
    }

    public Mono<Boolean> exists(final OrderRepository orderRepository) {
        return ofNullable(this.id)
            .map(id -> orderRepository.existsById(id, TRUE))
            .orElseGet(() -> Mono.just(FALSE));
    }

}
